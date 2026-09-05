package br.unoesc.linhaviva.ui.linha;

import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;

public class ItemItinerario {

    public final ItinerarioEntity ponto;
    public final String hora;
    public final boolean primeiro;
    public final boolean ultimo;
    public final boolean jaPassou;
    public final boolean comVeiculo;

    public ItemItinerario(ItinerarioEntity ponto, String hora, boolean primeiro, boolean ultimo,
                          boolean jaPassou, boolean comVeiculo) {
        this.ponto = ponto;
        this.hora = hora;
        this.primeiro = primeiro;
        this.ultimo = ultimo;
        this.jaPassou = jaPassou;
        this.comVeiculo = comVeiculo;
    }
}
