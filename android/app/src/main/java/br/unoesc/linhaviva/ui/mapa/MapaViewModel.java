package br.unoesc.linhaviva.ui.mapa;

import android.app.Application;
import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.data.repository.RepositorioFavoritos;
import br.unoesc.linhaviva.data.repository.RepositorioLinhas;
import br.unoesc.linhaviva.data.repository.RepositorioPontos;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.LocalizacaoLiveData;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Preferencias;

public class MapaViewModel extends AndroidViewModel {

    public static final int CAMADA_LINHAS_PROXIMAS = 0;
    public static final int CAMADA_PONTOS = 1;
    public static final int CAMADA_TERMINAIS = 2;

    private static final int RAIO_BUSCA_M = 1500;
    private static final int LIMITE_PONTOS = 25;

    private final RepositorioPontos repositorioPontos;
    private final RepositorioLinhas repositorioLinhas;
    private final RepositorioFavoritos repositorioFavoritos;
    private final Preferencias preferencias;

    private final MutableLiveData<Integer> camada = new MutableLiveData<>(CAMADA_LINHAS_PROXIMAS);
    private final MutableLiveData<String> busca = new MutableLiveData<>("");
    private final MutableLiveData<String> pontoSelecionado = new MutableLiveData<>();
    private final MutableLiveData<EstadoCarga> estado = new MutableLiveData<>(EstadoCarga.ocioso());
    private final MediatorLiveData<DadosMapa> dados = new MediatorLiveData<>();

    private final LiveData<List<PontoEntity>> pontos;
    private final LiveData<List<String>> pontosFavoritos;
    private final LiveData<List<PrevisaoEntity>> previsoes;
    private final LocalizacaoLiveData localizacao;
    private final EstadoConexao conexao;

    private Location ultimaLocalizacao;
    private String ultimoPontoConsultado;

    public MapaViewModel(@NonNull Application application) {
        super(application);
        repositorioPontos = new RepositorioPontos(application);
        repositorioLinhas = new RepositorioLinhas(application);
        repositorioFavoritos = new RepositorioFavoritos(application);
        preferencias = new Preferencias(application);

        pontos = repositorioPontos.observarTodos();
        pontosFavoritos = repositorioFavoritos.observarIdsDePontos();
        localizacao = new LocalizacaoLiveData(application);
        conexao = EstadoConexao.get(application);
        previsoes = Transformations.switchMap(pontoSelecionado, id ->
                id == null ? new MutableLiveData<>(new ArrayList<>())
                        : repositorioPontos.observarPrevisoes(id));

        double[] guardada = preferencias.ultimaLocalizacao();
        if (guardada != null) {
            Location local = new Location("cache");
            local.setLatitude(guardada[0]);
            local.setLongitude(guardada[1]);
            ultimaLocalizacao = local;
        }

        dados.addSource(pontos, v -> recomputar());
        dados.addSource(camada, v -> recomputar());
        dados.addSource(busca, v -> recomputar());
        dados.addSource(localizacao, local -> {
            ultimaLocalizacao = local;
            recomputar();
        });
    }

    public LiveData<DadosMapa> dados() {
        return dados;
    }

    public LiveData<List<PrevisaoEntity>> previsoes() {
        return previsoes;
    }

    public LiveData<List<String>> pontosFavoritos() {
        return pontosFavoritos;
    }

    public LiveData<Location> localizacao() {
        return localizacao;
    }

    public LiveData<Boolean> online() {
        return conexao.online();
    }

    public LiveData<EstadoCarga> estado() {
        return estado;
    }

    public LiveData<String> pontoSelecionado() {
        return pontoSelecionado;
    }

    public long ultimaSincronizacao() {
        return preferencias.ultimaSincronizacao();
    }

    public boolean provedorAtivo() {
        return localizacao.provedorAtivo();
    }

    public void definirCamada(int nova) {
        if (camada.getValue() == null || camada.getValue() != nova) camada.setValue(nova);
    }

    public void definirBusca(String termo) {
        if (!TextUtils.equals(termo, busca.getValue())) busca.setValue(termo);
    }

    public void selecionarPonto(String pontoId) {
        if (pontoId == null || pontoId.equals(pontoSelecionado.getValue())) return;
        pontoSelecionado.setValue(pontoId);
        atualizarPrevisoes(pontoId);
    }

    public void alternarFavoritoDoPonto() {
        String id = pontoSelecionado.getValue();
        if (id == null) return;
        AppExecutors.get().io().execute(() -> repositorioFavoritos.alternar("PONTO", id));
    }

    public void atualizarPrevisoes(String pontoId) {
        if (pontoId == null) return;
        ultimoPontoConsultado = pontoId;
        estado.setValue(EstadoCarga.carregando());
        AppExecutors.get().io().execute(() -> {
            Resultado<Boolean> resultado = repositorioPontos.atualizarPrevisoes(pontoId);
            estado.postValue(resultado.sucesso ? EstadoCarga.pronto() : EstadoCarga.erro(resultado.erro));
        });
    }

    public void recarregarPrevisoes() {
        atualizarPrevisoes(ultimoPontoConsultado != null
                ? ultimoPontoConsultado : pontoSelecionado.getValue());
    }

    private void recomputar() {
        List<PontoEntity> base = pontos.getValue();
        if (base == null || base.isEmpty()) return;

        int camadaAtiva = camada.getValue() == null ? CAMADA_LINHAS_PROXIMAS : camada.getValue();
        String termo = busca.getValue() == null ? "" : busca.getValue().trim().toLowerCase(Locale.ROOT);
        Location local = ultimaLocalizacao;

        AppExecutors.get().io().execute(() -> {
            DadosMapa montado = montar(base, camadaAtiva, termo, local);
            dados.postValue(montado);
            if (montado.maisProximo != null && pontoSelecionado.getValue() == null) {
                AppExecutors.get().principal().execute(() ->
                        selecionarPonto(montado.maisProximo.ponto.id));
            }
        });
    }

    private DadosMapa montar(List<PontoEntity> base, int camadaAtiva, String termo, Location local) {
        double latitude = local != null ? local.getLatitude() : GeoUtil.CHAPECO_LAT;
        double longitude = local != null ? local.getLongitude() : GeoUtil.CHAPECO_LON;

        List<RepositorioPontos.PontoProximo> proximos =
                repositorioPontos.proximosDe(latitude, longitude, RAIO_BUSCA_M, LIMITE_PONTOS);
        RepositorioPontos.PontoProximo maisProximo = proximos.isEmpty() ? null : proximos.get(0);

        List<PontoEntity> visiveis = new ArrayList<>();
        Map<String, List<ItinerarioEntity>> tracados = new LinkedHashMap<>();

        if (camadaAtiva == CAMADA_TERMINAIS) {
            for (PontoEntity ponto : base) if (ponto.terminal) visiveis.add(ponto);
        } else if (camadaAtiva == CAMADA_PONTOS) {
            visiveis.addAll(base);
        } else {
            Set<String> idsProximos = new HashSet<>();
            for (RepositorioPontos.PontoProximo p : proximos) idsProximos.add(p.ponto.id);

            Set<String> linhasProximas = new HashSet<>();
            for (ItinerarioEntity item : repositorioLinhas.itinerariosParaBusca()) {
                if (idsProximos.contains(item.pontoId)) linhasProximas.add(item.linhaId);
            }
            for (String linhaId : linhasProximas) {
                List<ItinerarioEntity> tracado = repositorioLinhas.itinerarioSincrono(linhaId, "IDA");
                if (!tracado.isEmpty()) tracados.put(linhaId, tracado);
            }
            for (RepositorioPontos.PontoProximo p : proximos) visiveis.add(p.ponto);
        }

        if (!termo.isEmpty()) {
            List<PontoEntity> filtrados = new ArrayList<>();
            for (PontoEntity ponto : (visiveis.isEmpty() ? base : visiveis)) {
                if (correspondeBusca(ponto, termo)) filtrados.add(ponto);
            }
            if (filtrados.isEmpty()) {
                for (PontoEntity ponto : base) if (correspondeBusca(ponto, termo)) filtrados.add(ponto);
            }
            visiveis = filtrados;
            tracados.clear();
        }

        return new DadosMapa(visiveis, tracados, maisProximo, local != null);
    }

    private boolean correspondeBusca(PontoEntity ponto, String termo) {
        return ponto.nome.toLowerCase(Locale.ROOT).contains(termo)
                || (ponto.endereco != null && ponto.endereco.toLowerCase(Locale.ROOT).contains(termo))
                || (ponto.bairro != null && ponto.bairro.toLowerCase(Locale.ROOT).contains(termo));
    }
}
