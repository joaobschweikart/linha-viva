package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;

@Dao
public interface InformacaoDao {

    @Query("SELECT * FROM informacao ORDER BY categoria, ordem")
    LiveData<List<InformacaoEntity>> observarTodas();

    @Query("SELECT * FROM informacao WHERE categoria = :categoria ORDER BY ordem")
    LiveData<List<InformacaoEntity>> observarPorCategoria(String categoria);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirTodas(List<InformacaoEntity> informacoes);

    @Query("DELETE FROM informacao")
    void limpar();
}
