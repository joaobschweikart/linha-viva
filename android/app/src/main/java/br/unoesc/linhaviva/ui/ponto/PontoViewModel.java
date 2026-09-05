package br.unoesc.linhaviva.ui.ponto;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.data.repository.RepositorioFavoritos;
import br.unoesc.linhaviva.data.repository.RepositorioPontos;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.LocalizacaoLiveData;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Preferencias;

public class PontoViewModel extends AndroidViewModel {

    private final RepositorioPontos repositorioPontos;
    private final RepositorioFavoritos repositorioFavoritos;
    private final Preferencias preferencias;
    private final EstadoConexao conexao;
    private final LocalizacaoLiveData localizacao;

    private final MutableLiveData<EstadoCarga> estado = new MutableLiveData<>(EstadoCarga.ocioso());
    private final MediatorLiveData<Integer> distancia = new MediatorLiveData<>();

    private LiveData<PontoEntity> ponto;
    private LiveData<List<PrevisaoEntity>> previsoes;
    private LiveData<Boolean> favorito;
    private String pontoId;

    public PontoViewModel(@NonNull Application application) {
        super(application);
        repositorioPontos = new RepositorioPontos(application);
        repositorioFavoritos = new RepositorioFavoritos(application);
        preferencias = new Preferencias(application);
        conexao = EstadoConexao.get(application);
        localizacao = new LocalizacaoLiveData(application);
    }

    public void iniciar(String id) {
        if (pontoId != null) return;
        pontoId = id;

        ponto = repositorioPontos.observar(id);
        previsoes = repositorioPontos.observarPrevisoes(id);
        favorito = Transformations.map(repositorioFavoritos.observarIdsDePontos(),
                ids -> ids != null && ids.contains(id));

        distancia.addSource(ponto, p -> calcularDistancia());
        distancia.addSource(localizacao, l -> calcularDistancia());

        atualizar();
    }

    public LiveData<PontoEntity> ponto() {
        return ponto;
    }

    public LiveData<List<PrevisaoEntity>> previsoes() {
        return previsoes;
    }

    public LiveData<Boolean> favorito() {
        return favorito;
    }

    public LiveData<Integer> distancia() {
        return distancia;
    }

    public LiveData<EstadoCarga> estado() {
        return estado;
    }

    public LiveData<Boolean> online() {
        return conexao.online();
    }

    public long ultimaSincronizacao() {
        return preferencias.ultimaSincronizacao();
    }

    /** Confirma que o codigo lido corresponde a um ponto conhecido antes de navegar (RF13). */
    public void validarPonto(String id, androidx.core.util.Consumer<Boolean> resposta) {
        AppExecutors.get().io().execute(() -> {
            boolean existe = repositorioPontos.buscarSincrono(id) != null;
            AppExecutors.get().principal().execute(() -> resposta.accept(existe));
        });
    }

    public void alternarFavorito() {
        if (pontoId == null) return;
        AppExecutors.get().io().execute(() -> repositorioFavoritos.alternar("PONTO", pontoId));
    }

    public void atualizar() {
        if (pontoId == null) return;
        estado.setValue(EstadoCarga.carregando());
        AppExecutors.get().io().execute(() -> {
            Resultado<Boolean> resultado = repositorioPontos.atualizarPrevisoes(pontoId);
            estado.postValue(resultado.sucesso ? EstadoCarga.pronto() : EstadoCarga.erro(resultado.erro));
        });
    }

    private void calcularDistancia() {
        PontoEntity atual = ponto == null ? null : ponto.getValue();
        Location local = localizacao.getValue();
        if (atual == null) return;
        if (local == null) {
            double[] guardada = preferencias.ultimaLocalizacao();
            if (guardada == null) {
                distancia.setValue(null);
                return;
            }
            distancia.setValue(GeoUtil.distanciaMetros(
                    guardada[0], guardada[1], atual.latitude, atual.longitude));
            return;
        }
        distancia.setValue(GeoUtil.distanciaMetros(
                local.getLatitude(), local.getLongitude(), atual.latitude, atual.longitude));
    }
}
