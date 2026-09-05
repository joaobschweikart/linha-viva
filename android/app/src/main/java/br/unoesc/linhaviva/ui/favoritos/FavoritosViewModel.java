package br.unoesc.linhaviva.ui.favoritos;

import android.app.Application;
import android.location.Location;

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
import java.util.Map;
import java.util.Set;

import br.unoesc.linhaviva.data.local.entity.AvisoEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity;
import br.unoesc.linhaviva.data.repository.RepositorioConteudo;
import br.unoesc.linhaviva.data.repository.RepositorioFavoritos;
import br.unoesc.linhaviva.data.repository.RepositorioLinhas;
import br.unoesc.linhaviva.data.repository.RepositorioPontos;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.data.repository.Sincronizador;
import br.unoesc.linhaviva.ui.linhas.ItemLinha;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.LocalizacaoLiveData;
import br.unoesc.linhaviva.util.CalculoPrevisao;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Preferencias;

public class FavoritosViewModel extends AndroidViewModel {

    private final RepositorioLinhas repositorioLinhas;
    private final RepositorioPontos repositorioPontos;
    private final RepositorioFavoritos repositorioFavoritos;
    private final RepositorioConteudo repositorioConteudo;
    private final Sincronizador sincronizador;
    private final Preferencias preferencias;

    private final MediatorLiveData<List<ItemLinha>> linhasFavoritas = new MediatorLiveData<>();
    private final MediatorLiveData<List<ItemPontoSalvo>> pontosSalvos = new MediatorLiveData<>();
    private final MutableLiveData<EstadoCarga> estado = new MutableLiveData<>(EstadoCarga.ocioso());

    private final LiveData<List<LinhaEntity>> linhas;
    private final LiveData<List<PontoEntity>> pontos;
    private final LiveData<List<String>> idsLinhas;
    private final LiveData<List<String>> idsPontos;
    private final LiveData<List<PrevisaoLinhaEntity>> previsoes;
    private final LiveData<List<AvisoEntity>> avisos;
    private final LocalizacaoLiveData localizacao;
    private final EstadoConexao conexao;

    private Location ultimaLocalizacao;

    public FavoritosViewModel(@NonNull Application application) {
        super(application);
        repositorioLinhas = new RepositorioLinhas(application);
        repositorioPontos = new RepositorioPontos(application);
        repositorioFavoritos = new RepositorioFavoritos(application);
        repositorioConteudo = new RepositorioConteudo(application);
        sincronizador = new Sincronizador(application);
        preferencias = new Preferencias(application);

        linhas = repositorioLinhas.observarLinhas();
        pontos = repositorioPontos.observarTodos();
        idsLinhas = repositorioFavoritos.observarIdsDeLinhas();
        idsPontos = repositorioFavoritos.observarIdsDePontos();
        previsoes = repositorioLinhas.observarPrevisoes();
        avisos = repositorioConteudo.observarAvisos();
        localizacao = new LocalizacaoLiveData(application);
        conexao = EstadoConexao.get(application);

        double[] guardada = preferencias.ultimaLocalizacao();
        if (guardada != null) {
            Location local = new Location("cache");
            local.setLatitude(guardada[0]);
            local.setLongitude(guardada[1]);
            ultimaLocalizacao = local;
        }

        linhasFavoritas.addSource(linhas, v -> montarLinhas());
        linhasFavoritas.addSource(idsLinhas, v -> montarLinhas());
        linhasFavoritas.addSource(previsoes, v -> montarLinhas());

        pontosSalvos.addSource(pontos, v -> montarPontos());
        pontosSalvos.addSource(idsPontos, v -> montarPontos());
        pontosSalvos.addSource(localizacao, local -> {
            ultimaLocalizacao = local;
            montarPontos();
        });
    }

    public LiveData<List<ItemLinha>> linhasFavoritas() {
        return linhasFavoritas;
    }

    public LiveData<List<ItemPontoSalvo>> pontosSalvos() {
        return pontosSalvos;
    }

    public LiveData<List<AvisoEntity>> avisos() {
        return avisos;
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

    public void atualizar() {
        estado.setValue(EstadoCarga.carregando());
        AppExecutors.get().io().execute(() -> {
            Resultado<Boolean> resultado = sincronizador.sincronizar(false);
            estado.postValue(resultado.sucesso ? EstadoCarga.pronto() : EstadoCarga.erro(resultado.erro));
        });
    }

    private void montarLinhas() {
        List<LinhaEntity> base = linhas.getValue();
        List<String> favoritas = idsLinhas.getValue();
        if (base == null || favoritas == null) return;

        Set<String> selecionadas = new HashSet<>(favoritas);
        Map<String, PrevisaoLinhaEntity> previsaoPorLinha = new HashMap<>();
        if (previsoes.getValue() != null) {
            for (PrevisaoLinhaEntity p : previsoes.getValue()) {
                PrevisaoLinhaEntity existente = previsaoPorLinha.get(p.linhaId);
                if (existente == null || p.minutos < existente.minutos) previsaoPorLinha.put(p.linhaId, p);
            }
        }

        AppExecutors.get().io().execute(() -> {
            List<ItemLinha> resultado = new ArrayList<>();
            for (LinhaEntity linha : base) {
                if (!selecionadas.contains(linha.id)) continue;
                PrevisaoLinhaEntity previsao = previsaoPorLinha.get(linha.id);
                String sentido = previsao != null ? previsao.sentido : "IDA";
                String programada = repositorioLinhas.proximaPartidaProgramada(linha.id, sentido);
                CalculoPrevisao.Resultado calculo = CalculoPrevisao.resolver(
                        previsao != null ? previsao.horaPrevista : null, programada);
                resultado.add(new ItemLinha(linha, sentido, calculo.minutos,
                        calculo.daPrevisao && previsao != null && previsao.tempoReal,
                        programada, true, Integer.MAX_VALUE));
            }
            Collections.sort(resultado, (a, b) -> a.linha.numero.compareTo(b.linha.numero));
            linhasFavoritas.postValue(resultado);
        });
    }

    private void montarPontos() {
        List<PontoEntity> base = pontos.getValue();
        List<String> favoritos = idsPontos.getValue();
        if (base == null || favoritos == null) return;

        Set<String> selecionados = new HashSet<>(favoritos);
        Location local = ultimaLocalizacao;

        List<ItemPontoSalvo> resultado = new ArrayList<>();
        for (PontoEntity ponto : base) {
            if (!selecionados.contains(ponto.id)) continue;
            int distancia = local == null ? -1 : GeoUtil.distanciaMetros(
                    local.getLatitude(), local.getLongitude(), ponto.latitude, ponto.longitude);
            resultado.add(new ItemPontoSalvo(ponto, distancia));
        }
        Collections.sort(resultado, (a, b) -> {
            if (a.distanciaMetros < 0 || b.distanciaMetros < 0) return 0;
            return Integer.compare(a.distanciaMetros, b.distanciaMetros);
        });
        pontosSalvos.setValue(resultado);
    }
}
