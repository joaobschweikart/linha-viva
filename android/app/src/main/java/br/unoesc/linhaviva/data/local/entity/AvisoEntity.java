package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "aviso")
public class AvisoEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String linhaId;
    public String severidade;
    public String titulo;
    public String descricao;
    public String publicadoEm;
    public long atualizadoEm;
}
