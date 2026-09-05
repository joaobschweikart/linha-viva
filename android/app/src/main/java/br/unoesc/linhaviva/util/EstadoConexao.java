package br.unoesc.linhaviva.util;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

/**
 * Estado de conexão do aplicativo, compartilhado por todas as telas.
 *
 * Combina dois sinais: o transporte de rede informado pelo sistema e o resultado
 * real das últimas chamadas à API. Ter rede e não alcançar o servidor de dados é,
 * para o usuário, o mesmo que estar offline — e as duas situações precisam exibir
 * a mesma faixa com a data da última atualização (RNF05).
 */
public final class EstadoConexao {

    private static volatile EstadoConexao instancia;

    private final MonitorConectividade transporte;
    private final MutableLiveData<Boolean> servidorAlcancavel = new MutableLiveData<>(true);
    private final MediatorLiveData<Boolean> online = new MediatorLiveData<>();

    private EstadoConexao(Context contexto) {
        transporte = new MonitorConectividade(contexto);
        online.postValue(MonitorConectividade.temConexao(contexto));
        // addSource exige a Main Thread e este singleton pode nascer em um executor.
        AppExecutors.get().principal().execute(() -> {
            online.addSource(transporte, v -> combinar());
            online.addSource(servidorAlcancavel, v -> combinar());
        });
    }

    public static EstadoConexao get(Context contexto) {
        if (instancia == null) {
            synchronized (EstadoConexao.class) {
                if (instancia == null) instancia = new EstadoConexao(contexto);
            }
        }
        return instancia;
    }

    public LiveData<Boolean> online() {
        return online;
    }

    public void registrarSucesso() {
        if (!Boolean.TRUE.equals(servidorAlcancavel.getValue())) servidorAlcancavel.postValue(true);
    }

    public void registrarFalha() {
        if (!Boolean.FALSE.equals(servidorAlcancavel.getValue())) servidorAlcancavel.postValue(false);
    }

    private void combinar() {
        boolean novo = Boolean.TRUE.equals(transporte.getValue())
                && Boolean.TRUE.equals(servidorAlcancavel.getValue());
        if (!Objects.equals(online.getValue(), novo)) online.setValue(novo);
    }

    /** Criado cedo, na Main Thread, para que chamadas em background só recuperem a instância. */
    public static void preparar(Context contexto) {
        get(contexto);
    }
}
