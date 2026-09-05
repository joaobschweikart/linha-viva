package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/** Proxima partida conhecida de cada linha, exibida na listagem (RF03). */
@Entity(tableName = "previsao_linha", primaryKeys = {"linhaId", "sentido"})
public class PrevisaoLinhaEntity {

    @NonNull public String linhaId = "";
    @NonNull public String sentido = "IDA";

    public int minutos;
    public String horaPrevista;
    public boolean tempoReal;
    public String origem;
    public String prefixoVeiculo;
    public String pontoReferenciaId;
    public String pontoReferenciaNome;
    public long atualizadoEm;
}
