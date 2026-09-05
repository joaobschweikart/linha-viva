package br.unoesc.linhaviva.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

/**
 * Fornece a localizacao do usuario apenas enquanto alguma tela observa este LiveData.
 * Ao perder o ultimo observador o GPS e liberado, evitando consumo em segundo plano (RNF02).
 */
public class LocalizacaoLiveData extends LiveData<Location> implements LocationListener {

    private static final long INTERVALO_MS = 15_000L;
    private static final float DESLOCAMENTO_M = 30f;

    private final Context contexto;
    private final LocationManager gerenciador;
    private final Preferencias preferencias;

    public LocalizacaoLiveData(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.gerenciador = (LocationManager) this.contexto.getSystemService(Context.LOCATION_SERVICE);
        this.preferencias = new Preferencias(this.contexto);
    }

    public static boolean temPermissao(Context contexto) {
        return ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean provedorAtivo() {
        if (gerenciador == null) return false;
        return gerenciador.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || gerenciador.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onActive() {
        if (gerenciador == null || !temPermissao(contexto)) return;
        try {
            solicitar(LocationManager.NETWORK_PROVIDER);
            solicitar(LocationManager.GPS_PROVIDER);
            Location ultima = ultimaConhecida();
            if (ultima != null) setValue(ultima);
        } catch (SecurityException ignorado) {
            // permissao revogada entre a checagem e a chamada
        }
    }

    @Override
    protected void onInactive() {
        if (gerenciador != null) gerenciador.removeUpdates(this);
    }

    private void solicitar(String provedor) throws SecurityException {
        if (gerenciador.isProviderEnabled(provedor)) {
            gerenciador.requestLocationUpdates(provedor, INTERVALO_MS, DESLOCAMENTO_M, this);
        }
    }

    private Location ultimaConhecida() throws SecurityException {
        Location gps = gerenciador.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? gerenciador.getLastKnownLocation(LocationManager.GPS_PROVIDER) : null;
        Location rede = gerenciador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ? gerenciador.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) : null;
        if (gps == null) return rede;
        if (rede == null) return gps;
        return gps.getTime() >= rede.getTime() ? gps : rede;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        preferencias.guardarUltimaLocalizacao(location.getLatitude(), location.getLongitude());
        setValue(location);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        onActive();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
