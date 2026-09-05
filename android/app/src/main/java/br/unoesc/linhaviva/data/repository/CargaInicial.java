package br.unoesc.linhaviva.data.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import br.unoesc.linhaviva.data.Mapeador;
import br.unoesc.linhaviva.data.local.BancoLocal;
import br.unoesc.linhaviva.data.remote.dto.AvisoDto;
import br.unoesc.linhaviva.data.remote.dto.HorarioDto;
import br.unoesc.linhaviva.data.remote.dto.InformacaoDto;
import br.unoesc.linhaviva.data.remote.dto.ItinerarioDto;
import br.unoesc.linhaviva.data.remote.dto.LinhaDto;
import br.unoesc.linhaviva.data.remote.dto.PontoDto;

/**
 * Popula o banco local com os dados embarcados em assets/seed na primeira execucao,
 * garantindo que o aplicativo abra util mesmo sem nunca ter alcancado a API (RNF05).
 */
public final class CargaInicial {

    private static final String TAG = "LinhaViva/Seed";
    private static final String PASTA = "seed/";

    private final Context contexto;
    private final BancoLocal banco;
    private final Gson gson = new Gson();

    public CargaInicial(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.banco = BancoLocal.get(contexto);
    }

    public boolean bancoVazio() {
        return banco.linhaDao().total() == 0;
    }

    public void executarSeNecessario() {
        if (!bancoVazio()) return;
        executar();
    }

    public void executar() {
        long instante = System.currentTimeMillis();
        try {
            List<LinhaDto> linhas = ler("linhas.json", new TypeToken<List<LinhaDto>>() {
            }.getType());
            List<PontoDto> pontos = ler("pontos.json", new TypeToken<List<PontoDto>>() {
            }.getType());
            List<ItinerarioDto> itinerarios = ler("itinerarios.json", new TypeToken<List<ItinerarioDto>>() {
            }.getType());
            List<HorarioDto> horarios = ler("horarios.json", new TypeToken<List<HorarioDto>>() {
            }.getType());
            List<AvisoDto> avisos = ler("avisos.json", new TypeToken<List<AvisoDto>>() {
            }.getType());
            List<InformacaoDto> informacoes = ler("informacoes.json", new TypeToken<List<InformacaoDto>>() {
            }.getType());

            banco.runInTransaction(() -> {
                banco.linhaDao().inserirTodas(Mapeador.linhas(linhas, instante));
                banco.pontoDao().inserirTodos(Mapeador.pontos(pontos, instante));
                banco.itinerarioDao().inserirTodos(Mapeador.itinerarios(itinerarios));
                banco.horarioDao().inserirTodos(Mapeador.horarios(horarios));
                banco.avisoDao().inserirTodos(Mapeador.avisos(avisos, instante));
                banco.informacaoDao().inserirTodas(Mapeador.informacoes(informacoes, instante));
            });
            Log.i(TAG, "Carga inicial concluída: " + linhas.size() + " linhas, " + pontos.size() + " pontos");
        } catch (IOException | RuntimeException e) {
            Log.e(TAG, "Falha ao carregar dados embarcados", e);
        }
    }

    private <T> T ler(String arquivo, Type tipo) throws IOException {
        try (Reader leitor = new InputStreamReader(
                contexto.getAssets().open(PASTA + arquivo), StandardCharsets.UTF_8)) {
            return gson.fromJson(leitor, tipo);
        }
    }
}
