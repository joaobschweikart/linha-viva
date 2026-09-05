package br.unoesc.linhaviva.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.unoesc.linhaviva.data.Mapeador;
import br.unoesc.linhaviva.data.local.BancoLocal;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.data.remote.ClienteApi;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoDto;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.MonitorConectividade;

public class RepositorioPontos {

    /** Ponto com a distancia ate o usuario, calculada localmente. */
    public static class PontoProximo {
        public final PontoEntity ponto;
        public final int distanciaMetros;

        PontoProximo(PontoEntity ponto, int distanciaMetros) {
            this.ponto = ponto;
            this.distanciaMetros = distanciaMetros;
        }
    }

    private final Context contexto;
    private final BancoLocal banco;

    public RepositorioPontos(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.banco = BancoLocal.get(contexto);
    }

    public LiveData<List<PontoEntity>> observarTodos() {
        return banco.pontoDao().observarTodos();
    }

    public LiveData<PontoEntity> observar(String pontoId) {
        return banco.pontoDao().observar(pontoId);
    }

    public PontoEntity buscarSincrono(String pontoId) {
        return banco.pontoDao().buscar(pontoId);
    }

    public LiveData<List<PrevisaoEntity>> observarPrevisoes(String pontoId) {
        return banco.previsaoDao().observarDoPonto(pontoId);
    }

    public List<String> linhasNoPonto(String pontoId) {
        return banco.itinerarioDao().linhasNoPonto(pontoId);
    }

    /**
     * Ordena os pontos por proximidade usando o banco local, sem consultar a rede.
     * Evita consumo desnecessario de dados a cada movimento do usuario (RNF06).
     */
    public List<PontoProximo> proximosDe(double latitude, double longitude, int raioMetros, int limite) {
        List<PontoProximo> lista = new ArrayList<>();
        for (PontoEntity ponto : banco.pontoDao().listarTodos()) {
            int distancia = GeoUtil.distanciaMetros(latitude, longitude, ponto.latitude, ponto.longitude);
            if (distancia <= raioMetros) lista.add(new PontoProximo(ponto, distancia));
        }
        Collections.sort(lista, (a, b) -> Integer.compare(a.distanciaMetros, b.distanciaMetros));
        return lista.size() > limite ? new ArrayList<>(lista.subList(0, limite)) : lista;
    }

    public Resultado<Boolean> atualizarPrevisoes(String pontoId) {
        if (!MonitorConectividade.temConexao(contexto)) {
            ChamadaApi.registrar(contexto);
            ChamadaApi.alcancou(false);
            return Resultado.falha("Sem conexão com a internet");
        }
        ChamadaApi.registrar(contexto);
        Resultado<List<PrevisaoDto>> resposta =
                ChamadaApi.executar(ClienteApi.get(contexto).previsoesDoPonto(pontoId, 8));
        if (!resposta.sucesso || resposta.dados == null) {
            return Resultado.falha(resposta.erro != null ? resposta.erro : "Sem previsões disponíveis");
        }

        long instante = System.currentTimeMillis();
        List<PrevisaoEntity> entidades = new ArrayList<>();
        for (PrevisaoDto dto : resposta.dados) {
            entidades.add(Mapeador.paraEntidade(dto, pontoId, instante));
        }
        banco.runInTransaction(() -> {
            banco.previsaoDao().limparPonto(pontoId);
            banco.previsaoDao().inserirDoPonto(entidades);
        });
        return Resultado.ok(true);
    }
}
