package br.unoesc.linhaviva.data.remote;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import br.unoesc.linhaviva.BuildConfig;
import br.unoesc.linhaviva.util.Preferencias;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Constroi o cliente REST. A URL base vem das preferencias, permitindo trocar o
 * servidor sem recompilar o aplicativo (tela Mais > Servidor de dados).
 */
public final class ClienteApi {

    private static volatile ApiLinhaViva api;
    private static volatile String urlEmUso;

    private ClienteApi() {
    }

    public static ApiLinhaViva get(Context contexto) {
        String url = new Preferencias(contexto).urlApi();
        ApiLinhaViva atual = api;
        if (atual != null && url.equals(urlEmUso)) return atual;

        synchronized (ClienteApi.class) {
            if (api == null || !url.equals(urlEmUso)) {
                api = criar(url);
                urlEmUso = url;
            }
            return api;
        }
    }

    public static void invalidar() {
        synchronized (ClienteApi.class) {
            api = null;
            urlEmUso = null;
        }
    }

    private static ApiLinhaViva criar(String urlBase) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(log)
                .build();

        return new Retrofit.Builder()
                .baseUrl(urlBase)
                .client(http)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiLinhaViva.class);
    }

    public static boolean urlValida(String url) {
        return url != null
                && (url.startsWith("http://") || url.startsWith("https://"))
                && url.endsWith("/")
                && url.length() > 9;
    }
}
