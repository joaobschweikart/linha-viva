package br.unoesc.linhaviva.util;

import android.content.Context;

import br.unoesc.linhaviva.R;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formatador {

    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", BR);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", BR);

    private Formatador() {
    }

    /** Abaixo deste raio o usuário é considerado no próprio ponto. */
    public static final int RAIO_NO_PONTO_M = 40;

    public static String distancia(int metros) {
        if (metros < 1000) return metros + " m";
        return String.format(BR, "%.1f km", metros / 1000.0);
    }

    public static String minutosAPe(int metros) {
        return GeoUtil.minutosAPe(metros) + " min";
    }

    public static String previsao(Context contexto, int minutos) {
        if (minutos <= 0) return contexto.getString(R.string.chegando);
        return contexto.getString(R.string.minutos_curto, minutos);
    }

    public static String horaDe(long instanteMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(instanteMillis), ZoneId.systemDefault())
                .format(HORA);
    }

    public static String dataHoraDe(long instanteMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(instanteMillis), ZoneId.systemDefault())
                .format(DATA_HORA);
    }

    public static String tempoRelativo(long instanteMillis) {
        long segundos = Math.max(0, (System.currentTimeMillis() - instanteMillis) / 1000);
        if (segundos < 60) return segundos + " s";
        long minutos = segundos / 60;
        if (minutos < 60) return minutos + " min";
        long horas = minutos / 60;
        if (horas < 24) return horas + " h";
        return (horas / 24) + " d";
    }

    public static String lotacao(Context contexto, String codigo) {
        if (codigo == null) return "";
        switch (codigo) {
            case "ALTA": return contexto.getString(R.string.lotacao_alta);
            case "MEDIA": return contexto.getString(R.string.lotacao_media);
            default: return contexto.getString(R.string.lotacao_baixa);
        }
    }

    public static String rotuloDiaTipo(Context contexto, String diaTipo) {
        switch (diaTipo) {
            case "SABADO": return contexto.getString(R.string.dia_sabado);
            case "DOMINGO": return contexto.getString(R.string.dia_domingo);
            default: return contexto.getString(R.string.dia_util);
        }
    }

    public static String diaTipoDeHoje() {
        switch (ZonedDateTime.now().getDayOfWeek()) {
            case SATURDAY: return "SABADO";
            case SUNDAY: return "DOMINGO";
            default: return "UTIL";
        }
    }

    public static int minutosDoDiaAgora() {
        ZonedDateTime agora = ZonedDateTime.now();
        return agora.getHour() * 60 + agora.getMinute();
    }

    public static int horaParaMinutos(String hora) {
        try {
            String[] partes = hora.split(":");
            return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
        } catch (RuntimeException e) {
            return -1;
        }
    }
}
