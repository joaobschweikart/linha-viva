package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "informacao")
public class InformacaoEntity {

    public static final String CATEGORIA_TARIFA = "TARIFA";
    public static final String CATEGORIA_TERMINAL = "TERMINAL";
    public static final String CATEGORIA_CONTATO = "CONTATO";

    @PrimaryKey
    @NonNull
    public String id = "";

    public String categoria;
    public String titulo;
    public String valor;
    public String descricao;
    public int ordem;
    public long atualizadoEm;
}
