package br.unoesc.linhaviva.util;

import android.location.Location;

public final class GeoUtil {

    public static final double CHAPECO_LAT = -27.1004;
    public static final double CHAPECO_LON = -52.6152;

    private static final double VELOCIDADE_PE_M_POR_MIN = 80.0;

    private GeoUtil() {
    }

    public static int distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        float[] resultado = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultado);
        return Math.round(resultado[0]);
    }

    public static int minutosAPe(int metros) {
        return Math.max(1, (int) Math.round(metros / VELOCIDADE_PE_M_POR_MIN));
    }
}
