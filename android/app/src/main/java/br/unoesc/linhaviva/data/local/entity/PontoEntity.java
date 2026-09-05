package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ponto")
public class PontoEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String nome;
    public String endereco;
    public String bairro;
    public double latitude;
    public double longitude;
    public boolean abrigo;
    public boolean acessivel;
    public boolean terminal;
    public long atualizadoEm;
}
