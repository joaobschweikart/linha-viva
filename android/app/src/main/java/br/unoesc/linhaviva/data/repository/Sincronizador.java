package br.unoesc.linhaviva.data.repository;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import br.unoesc.linhaviva.data.Mapeador;
import br.unoesc.linhaviva.data.local.BancoLocal;
import br.unoesc.linhaviva.data.remote.ApiLinhaViva;
import br.unoesc.linhaviva.data.remote.ClienteApi;
import br.unoesc.linhaviva.data.remote.dto.AvisoDto;
import br.unoesc.linhaviva.data.remote.dto.HorarioDto;
import br.unoesc.linhaviva.data.remote.dto.InformacaoDto;
import br.unoesc.linhaviva.data.remote.dto.ItinerarioDto;
import br.unoesc.linhaviva.data.remote.dto.LinhaDto;
import br.unoesc.linhaviva.data.remote.dto.PontoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoLinhaDto;
import br.unoesc.linhaviva.data.remote.dto.VersaoDto;
import br.unoesc.linhaviva.util.MonitorConectividade;
import br.unoesc.linhaviva.util.Preferencias;
import retrofit2.Response;

/**
 * Sincronizacao com a API REST. Roda sempre em background e so grava no banco
 * local quando a resposta chega completa, preservando os dados anteriores em
 * caso de falha de rede.
 */
public class Sincronizador {

    private static final String TAG = "LinhaViva/Sync";

    private final Context contexto;
    private final BancoLocal banco;
    private final Preferencias preferencias;

    public Sincronizador(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.banco = BancoLocal.get(contexto);
        this.preferencias = new Preferencias(contexto);
    }

    public Resultado<Boolean> sincronizar(boolean forcarCatalogo) {
        if (!MonitorConectividade.temConexao(contexto)) {
            ChamadaApi.registrar(contexto);
            ChamadaApi.alcancou(false);
            return Resultado.falha("Sem conexão com a internet");
        }

        ChamadaApi.registrar(contexto);
        ApiLinhaViva api = ClienteApi.get(contexto);
        long instante = System.currentTimeMillis();

        String versaoRemota;
        try {
            Response<VersaoDto> resposta = api.versao().execute();
            if (!resposta.isSuccessful() || resposta.body() == null) {
                ChamadaApi.alcancou(false);
                return Resultado.falha("Servidor de dados indisponível");
            }
            versaoRemota = resposta.body().versaoDados;
            ChamadaApi.alcancou(true);
        } catch (IOException e) {
            ChamadaApi.alcancou(false);
            return Resultado.falha("Sem conexão com o servidor de dados");
        } catch (RuntimeException e) {
            return Resultado.falha("Resposta inválida do servidor");
        }

        boolean catalogoMudou = forcarCatalogo || !versaoRemota.equals(preferencias.versaoDados());
        if (catalogoMudou) {
            Resultado<Boolean> catalogo = sincronizarCatalogo(api, instante);
            if (!catalogo.sucesso) return catalogo;
            preferencias.definirVersaoDados(versaoRemota);
        } else {
            Log.i(TAG, "Catálogo já atualizado (versão " + versaoRemota + "), baixando apenas o volátil");
        }

        sincronizarConteudo(api, instante);
        sincronizarPrevisoesDasLinhas(api, instante);

        preferencias.registrarSincronizacao(instante);
        preferencias.marcarCargaInicialConcluida();
        return Resultado.ok(catalogoMudou);
    }

    private Resultado<Boolean> sincronizarCatalogo(ApiLinhaViva api, long instante) {
        Resultado<List<LinhaDto>> linhas = ChamadaApi.executar(api.linhas());
        if (!linhas.sucesso) return Resultado.falha(linhas.erro);

        Resultado<List<PontoDto>> pontos = ChamadaApi.executar(api.pontos());
        if (!pontos.sucesso) return Resultado.falha(pontos.erro);

        Resultado<List<ItinerarioDto>> itinerarios = ChamadaApi.executar(api.itinerarios());
        if (!itinerarios.sucesso) return Resultado.falha(itinerarios.erro);

        Resultado<List<HorarioDto>> horarios = ChamadaApi.executar(api.horarios());
        if (!horarios.sucesso) return Resultado.falha(horarios.erro);

        banco.runInTransaction(() -> {
            banco.linhaDao().inserirTodas(Mapeador.linhas(linhas.dados, instante));
            banco.pontoDao().inserirTodos(Mapeador.pontos(pontos.dados, instante));
            banco.itinerarioDao().inserirTodos(Mapeador.itinerarios(itinerarios.dados));
            banco.horarioDao().inserirTodos(Mapeador.horarios(horarios.dados));
        });
        Log.i(TAG, "Catálogo sincronizado: " + linhas.dados.size() + " linhas");
        return Resultado.ok(true);
    }

    private void sincronizarConteudo(ApiLinhaViva api, long instante) {
        Resultado<List<AvisoDto>> avisos = ChamadaApi.executar(api.avisos());
        if (avisos.sucesso && avisos.dados != null) {
            banco.runInTransaction(() -> {
                banco.avisoDao().limpar();
                banco.avisoDao().inserirTodos(Mapeador.avisos(avisos.dados, instante));
            });
        }

        Resultado<List<InformacaoDto>> informacoes = ChamadaApi.executar(api.informacoes());
        if (informacoes.sucesso && informacoes.dados != null) {
            banco.runInTransaction(() -> {
                banco.informacaoDao().limpar();
                banco.informacaoDao().inserirTodas(Mapeador.informacoes(informacoes.dados, instante));
            });
        }
    }

    public Resultado<Boolean> sincronizarPrevisoesDasLinhas() {
        if (!MonitorConectividade.temConexao(contexto)) {
            return Resultado.falha("Sem conexão com a internet");
        }
        return sincronizarPrevisoesDasLinhas(ClienteApi.get(contexto), System.currentTimeMillis());
    }

    private Resultado<Boolean> sincronizarPrevisoesDasLinhas(ApiLinhaViva api, long instante) {
        Resultado<List<PrevisaoLinhaDto>> previsoes = ChamadaApi.executar(api.previsoesDasLinhas());
        if (!previsoes.sucesso || previsoes.dados == null) {
            return Resultado.falha(previsoes.erro != null ? previsoes.erro : "Sem previsões disponíveis");
        }
        java.util.List<br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity> entidades =
                new java.util.ArrayList<>();
        for (PrevisaoLinhaDto dto : previsoes.dados) {
            entidades.add(Mapeador.paraEntidade(dto, instante));
        }
        banco.runInTransaction(() -> {
            banco.previsaoDao().limparDeLinhas();
            banco.previsaoDao().inserirDeLinhas(entidades);
        });
        return Resultado.ok(true);
    }
}
