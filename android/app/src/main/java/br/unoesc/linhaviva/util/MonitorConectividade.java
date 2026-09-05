package br.unoesc.linhaviva.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * Disponibilidade de transporte de rede. Todos os retornos do sistema reavaliam a
 * rede ativa: publicar o estado de uma rede isolada faria a interface oscilar
 * durante a troca entre Wi-Fi e dados móveis.
 */
public class MonitorConectividade extends LiveData<Boolean> {

    private final ConnectivityManager gerenciador;

    private final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            reavaliar();
        }

        @Override
        public void onLost(@NonNull Network network) {
            reavaliar();
        }

        @Override
        public void onUnavailable() {
            reavaliar();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network,
                                          @NonNull NetworkCapabilities capacidades) {
            reavaliar();
        }
    };

    public MonitorConectividade(Context contexto) {
        gerenciador = (ConnectivityManager) contexto.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        postValue(temConexao(gerenciador));
    }

    public static boolean temConexao(ConnectivityManager gerenciador) {
        if (gerenciador == null) return false;
        Network ativa = gerenciador.getActiveNetwork();
        if (ativa == null) return false;
        NetworkCapabilities capacidades = gerenciador.getNetworkCapabilities(ativa);
        return capacidades != null
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public static boolean temConexao(Context contexto) {
        return temConexao((ConnectivityManager) contexto.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    private void reavaliar() {
        postValue(temConexao(gerenciador));
    }

    @Override
    protected void onActive() {
        setValue(temConexao(gerenciador));
        NetworkRequest requisicao = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        try {
            gerenciador.registerNetworkCallback(requisicao, callback);
        } catch (RuntimeException ignorado) {
            // callback ja registrado
        }
    }

    @Override
    protected void onInactive() {
        try {
            gerenciador.unregisterNetworkCallback(callback);
        } catch (IllegalArgumentException ignorado) {
            // callback ja removido
        }
    }
}
