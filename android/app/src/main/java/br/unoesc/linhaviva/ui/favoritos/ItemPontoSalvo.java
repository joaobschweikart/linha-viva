package br.unoesc.linhaviva.ui.favoritos;

import br.unoesc.linhaviva.data.local.entity.PontoEntity;

public class ItemPontoSalvo {

    public final PontoEntity ponto;
    public final int distanciaMetros;

    public ItemPontoSalvo(PontoEntity ponto, int distanciaMetros) {
        this.ponto = ponto;
        this.distanciaMetros = distanciaMetros;
    }
}
