package br.unoesc.linhaviva.ui.mais;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;
import br.unoesc.linhaviva.data.remote.ClienteApi;
import br.unoesc.linhaviva.data.repository.RepositorioConteudo;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.data.repository.Sincronizador;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Preferencias;

public class MaisViewModel extends AndroidViewModel {

    private final RepositorioConteudo repositorioConteudo;
    private final Sincronizador sincronizador;
    private final Preferencias preferencias;
    private final EstadoConexao conexao;

    private final MutableLiveData<EstadoCarga> estado = new MutableLiveData<>(EstadoCarga.ocioso());
    private final MutableLiveData<String> urlApi = new MutableLiveData<>();
    private final MutableLiveData<Long> ultimaSincronizacao = new MutableLiveData<>();

    public MaisViewModel(@NonNull Application application) {
        super(application);
        repositorioConteudo = new RepositorioConteudo(application);
        sincronizador = new Sincronizador(application);
        preferencias = new Preferencias(application);
        conexao = EstadoConexao.get(application);

        urlApi.setValue(preferencias.urlApi());
        ultimaSincronizacao.setValue(preferencias.ultimaSincronizacao());
    }

    public LiveData<List<InformacaoEntity>> informacoesDe(String categoria) {
        return repositorioConteudo.observarInformacoes(categoria);
    }

    public LiveData<EstadoCarga> estado() {
        return estado;
    }

    public LiveData<String> urlApi() {
        return urlApi;
    }

    public LiveData<Long> ultimaSincronizacao() {
        return ultimaSincronizacao;
    }

    public LiveData<Boolean> online() {
        return conexao.online();
    }

    public String urlPadrao() {
        return preferencias.urlApiPadrao();
    }

    public boolean definirUrlApi(String url) {
        String normalizada = url == null ? "" : url.trim();
        if (!normalizada.endsWith("/")) normalizada = normalizada + "/";
        if (!ClienteApi.urlValida(normalizada)) return false;

        preferencias.definirUrlApi(normalizada);
        ClienteApi.invalidar();
        urlApi.setValue(normalizada);
        sincronizar(true);
        return true;
    }

    public void restaurarUrlPadrao() {
        preferencias.restaurarUrlPadrao();
        ClienteApi.invalidar();
        urlApi.setValue(preferencias.urlApi());
        sincronizar(true);
    }

    public void sincronizar(boolean forcar) {
        estado.setValue(EstadoCarga.carregando());
        AppExecutors.get().io().execute(() -> {
            Resultado<Boolean> resultado = sincronizador.sincronizar(forcar);
            ultimaSincronizacao.postValue(preferencias.ultimaSincronizacao());
            estado.postValue(resultado.sucesso ? EstadoCarga.pronto() : EstadoCarga.erro(resultado.erro));
        });
    }
}
