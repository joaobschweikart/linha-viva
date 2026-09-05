package br.unoesc.linhaviva.data.repository;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import br.unoesc.linhaviva.data.remote.dto.EnvelopeDto;
import br.unoesc.linhaviva.util.EstadoConexao;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Executa chamadas REST de forma sincrona. Todos os chamadores rodam em um
 * executor de background, nunca na Main Thread.
 */
final class ChamadaApi {

    private static final String TAG = "LinhaViva/API";

    private static EstadoConexao estado;

    private ChamadaApi() {
    }

    static void registrar(Context contexto) {
        if (estado == null) estado = EstadoConexao.get(contexto);
    }

    static <T> Resultado<T> executar(Call<EnvelopeDto<T>> chamada) {
        try {
            Response<EnvelopeDto<T>> resposta = chamada.execute();
            if (!resposta.isSuccessful()) {
                return Resultado.falha("Servidor respondeu " + resposta.code());
            }
            EnvelopeDto<T> corpo = resposta.body();
            if (corpo == null) {
                return Resultado.falha("Resposta vazia do servidor");
            }
            alcancou(true);
            return Resultado.ok(corpo.dados);
        } catch (IOException e) {
            Log.w(TAG, "Falha de rede: " + e.getMessage());
            alcancou(false);
            return Resultado.falha("Sem conexão com o servidor de dados");
        } catch (RuntimeException e) {
            Log.w(TAG, "Resposta inválida", e);
            return Resultado.falha("Resposta inválida do servidor");
        }
    }

    static void alcancou(boolean sucesso) {
        if (estado == null) return;
        if (sucesso) estado.registrarSucesso();
        else estado.registrarFalha();
    }
}
