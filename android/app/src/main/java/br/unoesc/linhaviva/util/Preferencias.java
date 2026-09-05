package br.unoesc.linhaviva.util;

import android.content.Context;
import android.content.SharedPreferences;

import br.unoesc.linhaviva.BuildConfig;

public final class Preferencias {

    private static final String ARQUIVO = "linha_viva_prefs";
    private static final String CHAVE_URL_API = "url_api";
    private static final String CHAVE_CARGA_INICIAL = "carga_inicial_concluida";
    private static final String CHAVE_ULTIMA_SINC = "ultima_sincronizacao";
    private static final String CHAVE_LOCAL_LAT = "ultima_lat";
    private static final String CHAVE_LOCAL_LON = "ultima_lon";
    private static final String CHAVE_PERMISSAO_PEDIDA = "permissao_local_pedida";
    private static final String CHAVE_VERSAO_DADOS = "versao_dados";

    private final SharedPreferences prefs;

    public Preferencias(Context contexto) {
        prefs = contexto.getApplicationContext().getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public String urlApi() {
        return prefs.getString(CHAVE_URL_API, BuildConfig.API_BASE_URL);
    }

    public void definirUrlApi(String url) {
        prefs.edit().putString(CHAVE_URL_API, url).apply();
    }

    public String urlApiPadrao() {
        return BuildConfig.API_BASE_URL;
    }

    public void restaurarUrlPadrao() {
        prefs.edit().remove(CHAVE_URL_API).apply();
    }

    public boolean cargaInicialConcluida() {
        return prefs.getBoolean(CHAVE_CARGA_INICIAL, false);
    }

    public void marcarCargaInicialConcluida() {
        prefs.edit().putBoolean(CHAVE_CARGA_INICIAL, true).apply();
    }

    public long ultimaSincronizacao() {
        return prefs.getLong(CHAVE_ULTIMA_SINC, 0L);
    }

    public void registrarSincronizacao(long instante) {
        prefs.edit().putLong(CHAVE_ULTIMA_SINC, instante).apply();
    }

    public void guardarUltimaLocalizacao(double latitude, double longitude) {
        prefs.edit()
                .putFloat(CHAVE_LOCAL_LAT, (float) latitude)
                .putFloat(CHAVE_LOCAL_LON, (float) longitude)
                .apply();
    }

    public double[] ultimaLocalizacao() {
        if (!prefs.contains(CHAVE_LOCAL_LAT)) return null;
        return new double[]{prefs.getFloat(CHAVE_LOCAL_LAT, 0f), prefs.getFloat(CHAVE_LOCAL_LON, 0f)};
    }

    public String versaoDados() {
        return prefs.getString(CHAVE_VERSAO_DADOS, null);
    }

    public void definirVersaoDados(String versao) {
        prefs.edit().putString(CHAVE_VERSAO_DADOS, versao).apply();
    }

    public boolean permissaoLocalJaPedida() {
        return prefs.getBoolean(CHAVE_PERMISSAO_PEDIDA, false);
    }

    public void marcarPermissaoLocalPedida() {
        prefs.edit().putBoolean(CHAVE_PERMISSAO_PEDIDA, true).apply();
    }
}
