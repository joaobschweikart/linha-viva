package br.unoesc.linhaviva.ui.mapa;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.repository.RepositorioPontos;

/** Conjunto pronto para desenhar no mapa, montado sempre em background. */
public class DadosMapa {

    public final List<PontoEntity> pontosVisiveis;
    public final Map<String, List<ItinerarioEntity>> tracados;
    @Nullable public final RepositorioPontos.PontoProximo maisProximo;
    public final boolean temLocalizacao;

    public DadosMapa(List<PontoEntity> pontosVisiveis,
                     Map<String, List<ItinerarioEntity>> tracados,
                     @Nullable RepositorioPontos.PontoProximo maisProximo,
                     boolean temLocalizacao) {
        this.pontosVisiveis = pontosVisiveis;
        this.tracados = tracados;
        this.maisProximo = maisProximo;
        this.temLocalizacao = temLocalizacao;
    }
}
