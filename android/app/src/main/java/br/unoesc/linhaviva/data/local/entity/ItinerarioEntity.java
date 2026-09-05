package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(tableName = "itinerario",
        primaryKeys = {"linhaId", "sentido", "ordem"},
        indices = {@Index("pontoId"), @Index("linhaId")})
public class ItinerarioEntity {

    @NonNull public String linhaId = "";
    @NonNull public String sentido = "IDA";
    public int ordem;

    public String pontoId;
    public String pontoNome;
    public String bairro;
    public double latitude;
    public double longitude;
    public boolean terminal;
    public int tempoAcumuladoMin;
}
