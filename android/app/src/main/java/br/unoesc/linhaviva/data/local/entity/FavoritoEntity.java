package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "favorito", primaryKeys = {"tipo", "referenciaId"})
public class FavoritoEntity {

    public static final String TIPO_LINHA = "LINHA";
    public static final String TIPO_PONTO = "PONTO";

    @NonNull public String tipo = TIPO_LINHA;
    @NonNull public String referenciaId = "";
    public String apelido;
    public long criadoEm;

    public FavoritoEntity() {
    }

    public static FavoritoEntity de(String tipo, String referenciaId) {
        FavoritoEntity f = new FavoritoEntity();
        f.tipo = tipo;
        f.referenciaId = referenciaId;
        f.criadoEm = System.currentTimeMillis();
        return f;
    }
}
