package br.unoesc.linhaviva.ui.linhas;

import android.app.Application;
import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity;
import br.unoesc.linhaviva.data.repository.RepositorioFavoritos;
import br.unoesc.linhaviva.data.repository.RepositorioLinhas;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.data.repository.Sincronizador;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.LocalizacaoLiveData;
import br.unoesc.linhaviva.util.CalculoPrevisao;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Preferencias;

public class LinhasViewModel extends AndroidViewModel {

    public static final int FILTRO_TODAS = 0;
    public static final int FILTRO_FAVORITAS = 1;
    public static final int FILTRO_PROXIMAS = 2;
    public static final int FILTRO_ACESSIVEIS = 3;

    private static final int RAIO_PROXIMIDADE_M = 1200;

    private final RepositorioLinhas repositorioLinhas;
    private final RepositorioFavoritos repositorioFavoritos;
    private final Sincronizador sincronizador;
    private final Preferencias preferencias;

    private final MutableLiveData<String> busca = new MutableLiveData<>("");
    private final MutableLiveData<Integer> filtro = new MutableLiveData<>(FILTRO_TODAS);
    private final MutableLiveData<EstadoCarga> estado = new MutableLiveData<>(EstadoCarga.ocioso());
    private final MediatorLiveData<List<ItemLinha>> itens = new MediatorLiveData<>();

    private final LiveData<List<LinhaEntity>> linhas;
    private final LiveData<List<PrevisaoLinhaEntity>> previsoes;
    private final LiveData<List<String>> favoritas;
    private final LocalizacaoLiveData localizacao;
    private final EstadoConexao conexao;

    private Location ultimaLocalizacao;

    public LinhasViewModel(@NonNull Application application) {
        super(application);
        repositorioLinhas = new RepositorioLinhas(application);
        repositorioFavoritos = new RepositorioFavoritos(application);
        sincronizador = new Sincronizador(application);
        preferencias = new Preferencias(application);

        linhas = repositorioLinhas.observarLinhas();
        previsoes = repositorioLinhas.observarPrevisoes();
        favoritas = repositorioFavoritos.observarIdsDeLinhas();
        localizacao = new LocalizacaoLiveData(application);
        conexao = EstadoConexao.get(application);

        itens.addSource(linhas, v -> recomputar());
        itens.addSource(previsoes, v -> recomputar());
        itens.addSource(favoritas, v -> recomputar());
        itens.addSource(busca, v -> recomputar());
        itens.addSource(filtro, v -> recomputar());
        itens.addSource(localizacao, local -> {
            ultimaLocalizacao = local;
            if (filtro.getValue() != null && filtro.getValue() == FILTRO_PROXIMAS) recomputar();
        });
    }

    public LiveData<List<ItemLinha>> itens() {
        return itens;
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

    public void definirBusca(String termo) {
        if (!TextUtils.equals(termo, busca.getValue())) busca.setValue(termo);
    }

    public void definirFiltro(int novo) {
        if (filtro.getValue() == null || filtro.getValue() != novo) filtro.setValue(novo);
    }

    public int filtroAtual() {
        return filtro.getValue() == null ? FILTRO_TODAS : filtro.getValue();
    }

    public void alternarFavorito(String linhaId) {
        AppExecutors.get().io().execute(() ->
                repositorioFavoritos.alternar("LINHA", linhaId));
    }

    public void atualizar() {
        estado.setValue(EstadoCarga.carregando());
        AppExecutors.get().io().execute(() -> {
            Resultado<Boolean> resultado = sincronizador.sincronizar(false);
            estado.postValue(resultado.sucesso
                    ? EstadoCarga.pronto()
                    : EstadoCarga.erro(resultado.erro));
        });
    }

    private void recomputar() {
        List<LinhaEntity> base = linhas.getValue();
        if (base == null) return;

        String termo = busca.getValue() == null ? "" : busca.getValue().trim().toLowerCase(Locale.ROOT);
        int filtroAtivo = filtroAtual();
        Set<String> favoritos = new HashSet<>(favoritas.getValue() == null
                ? Collections.emptyList() : favoritas.getValue());
        Map<String, PrevisaoLinhaEntity> previsaoPorLinha = new HashMap<>();
        if (previsoes.getValue() != null) {
            for (PrevisaoLinhaEntity p : previsoes.getValue()) {
                PrevisaoLinhaEntity existente = previsaoPorLinha.get(p.linhaId);
                if (existente == null || p.minutos < existente.minutos) previsaoPorLinha.put(p.linhaId, p);
            }
        }
        Location local = ultimaLocalizacao;

        AppExecutors.get().io().execute(() ->
                itens.postValue(montar(base, termo, filtroAtivo, favoritos, previsaoPorLinha, local)));
    }

    private List<ItemLinha> montar(List<LinhaEntity> base, String termo, int filtroAtivo,
                                   Set<String> favoritos, Map<String, PrevisaoLinhaEntity> previsaoPorLinha,
                                   Location local) {
        Map<String, String> textoDosPontos = new HashMap<>();
        Map<String, Integer> distanciaPorLinha = new HashMap<>();

        for (ItinerarioEntity item : repositorioLinhas.itinerariosParaBusca()) {
            String acumulado = textoDosPontos.get(item.linhaId);
            String trecho = (item.pontoNome + " " + item.bairro).toLowerCase(Locale.ROOT);
            textoDosPontos.put(item.linhaId, acumulado == null ? trecho : acumulado + " " + trecho);

            if (local != null) {
                int distancia = GeoUtil.distanciaMetros(
                        local.getLatitude(), local.getLongitude(), item.latitude, item.longitude);
                Integer menor = distanciaPorLinha.get(item.linhaId);
                if (menor == null || distancia < menor) distanciaPorLinha.put(item.linhaId, distancia);
            }
        }

        List<ItemLinha> resultado = new ArrayList<>();
        for (LinhaEntity linha : base) {
            if (!termo.isEmpty() && !corresponde(linha, termo, textoDosPontos.get(linha.id))) continue;

            boolean favorita = favoritos.contains(linha.id);
            int distancia = distanciaPorLinha.containsKey(linha.id)
                    ? distanciaPorLinha.get(linha.id) : Integer.MAX_VALUE;

            if (filtroAtivo == FILTRO_FAVORITAS && !favorita) continue;
            if (filtroAtivo == FILTRO_ACESSIVEIS && !linha.acessivel) continue;
            if (filtroAtivo == FILTRO_PROXIMAS && distancia > RAIO_PROXIMIDADE_M) continue;

            PrevisaoLinhaEntity previsao = previsaoPorLinha.get(linha.id);
            String sentido = previsao != null ? previsao.sentido : "IDA";
            String horaProgramada = repositorioLinhas.proximaPartidaProgramada(linha.id, sentido);
            CalculoPrevisao.Resultado calculo = CalculoPrevisao.resolver(
                    previsao != null ? previsao.horaPrevista : null, horaProgramada);

            resultado.add(new ItemLinha(linha, sentido, calculo.minutos,
                    calculo.daPrevisao && previsao != null && previsao.tempoReal,
                    horaProgramada, favorita, distancia));
        }

        if (filtroAtivo == FILTRO_PROXIMAS) {
            Collections.sort(resultado, (a, b) -> Integer.compare(a.distanciaMetros, b.distanciaMetros));
        } else {
            Collections.sort(resultado, (a, b) -> a.linha.numero.compareTo(b.linha.numero));
        }
        return resultado;
    }

    private boolean corresponde(LinhaEntity linha, String termo, String textoDosPontos) {
        return linha.numero.toLowerCase(Locale.ROOT).contains(termo)
                || linha.nome.toLowerCase(Locale.ROOT).contains(termo)
                || (linha.sentidoIda != null && linha.sentidoIda.toLowerCase(Locale.ROOT).contains(termo))
                || (linha.sentidoVolta != null && linha.sentidoVolta.toLowerCase(Locale.ROOT).contains(termo))
                || (textoDosPontos != null && textoDosPontos.contains(termo));
    }
}
