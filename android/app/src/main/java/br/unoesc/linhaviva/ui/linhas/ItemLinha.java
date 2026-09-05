package br.unoesc.linhaviva.ui.linhas;

import androidx.annotation.Nullable;

import java.util.Objects;

import br.unoesc.linhaviva.data.local.entity.LinhaEntity;

/** Linha pronta para exibicao, com previsao resolvida e marca de favorito. */
public class ItemLinha {

    public final LinhaEntity linha;
    public final String sentido;
    @Nullable public final Integer minutos;
    public final boolean tempoReal;
    @Nullable public final String horaProgramada;
    public final boolean favorita;
    public final int distanciaMetros;

    public ItemLinha(LinhaEntity linha, String sentido, @Nullable Integer minutos, boolean tempoReal,
                     @Nullable String horaProgramada, boolean favorita, int distanciaMetros) {
        this.linha = linha;
        this.sentido = sentido;
        this.minutos = minutos;
        this.tempoReal = tempoReal;
        this.horaProgramada = horaProgramada;
        this.favorita = favorita;
        this.distanciaMetros = distanciaMetros;
    }

    public boolean mesmoConteudo(ItemLinha outro) {
        return Objects.equals(minutos, outro.minutos)
                && tempoReal == outro.tempoReal
                && favorita == outro.favorita
                && Objects.equals(horaProgramada, outro.horaProgramada)
                && Objects.equals(sentido, outro.sentido);
    }
}
