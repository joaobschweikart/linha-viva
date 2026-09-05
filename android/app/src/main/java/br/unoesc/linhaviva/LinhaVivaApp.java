package br.unoesc.linhaviva;

import android.app.Application;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

import br.unoesc.linhaviva.data.repository.CargaInicial;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Notificacoes;

public class LinhaVivaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        configurarMapa();
        Notificacoes.criarCanais(this);
        EstadoConexao.preparar(this);
        AppExecutors.get().io().execute(() -> new CargaInicial(this).executarSeNecessario());
    }

    private void configurarMapa() {
        IConfigurationProvider configuracao = Configuration.getInstance();
        configuracao.load(this, PreferenceManager.getDefaultSharedPreferences(this));
        configuracao.setUserAgentValue(getPackageName());
        configuracao.setOsmdroidBasePath(getExternalCacheDir() != null ? getExternalCacheDir() : getCacheDir());
        configuracao.setOsmdroidTileCache(new java.io.File(configuracao.getOsmdroidBasePath(), "tiles"));
    }
}
