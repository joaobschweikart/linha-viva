package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

/** Cache das ultimas previsoes recebidas, usado para o modo offline (RNF05). */
@Entity(tableName = "previsao",
        primaryKeys = {"pontoId", "linhaId", "sentido"},
        indices = {@Index("pontoId")})
public class PrevisaoEntity {

    @NonNull public String pontoId = "";
    @NonNull public String linhaId = "";
    @NonNull public String sentido = "IDA";

    public String linhaNumero;
    public String linhaNome;
    public String sentidoDescricao;
    public int minutos;
    public String horaPrevista;
    public String horaProgramada;
    public boolean tempoReal;
    public String origem;
    public String prefixoVeiculo;
    public boolean acessivel;
    public String lotacao;
    public long atualizadoEm;
}
