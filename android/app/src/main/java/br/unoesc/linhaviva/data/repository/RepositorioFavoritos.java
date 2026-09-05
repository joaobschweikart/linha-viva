package br.unoesc.linhaviva.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import br.unoesc.linhaviva.data.local.BancoLocal;
import br.unoesc.linhaviva.data.local.entity.FavoritoEntity;

public class RepositorioFavoritos {

    private final BancoLocal banco;

    public RepositorioFavoritos(Context contexto) {
        this.banco = BancoLocal.get(contexto);
    }

    public LiveData<List<String>> observarIdsDeLinhas() {
        return banco.favoritoDao().observarIds(FavoritoEntity.TIPO_LINHA);
    }

    public LiveData<List<String>> observarIdsDePontos() {
        return banco.favoritoDao().observarIds(FavoritoEntity.TIPO_PONTO);
    }

    public List<String> idsDePontos() {
        return banco.favoritoDao().listarIds(FavoritoEntity.TIPO_PONTO);
    }

    /** Retorna true quando o item passou a ser favorito. */
    public boolean alternar(String tipo, String referenciaId) {
        boolean jaEra = banco.favoritoDao().existe(tipo, referenciaId) > 0;
        if (jaEra) {
            banco.favoritoDao().remover(tipo, referenciaId);
        } else {
            banco.favoritoDao().inserir(FavoritoEntity.de(tipo, referenciaId));
        }
        return !jaEra;
    }
}
