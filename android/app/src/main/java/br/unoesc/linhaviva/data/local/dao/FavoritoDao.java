package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.FavoritoEntity;

@Dao
public interface FavoritoDao {

    @Query("SELECT * FROM favorito ORDER BY criadoEm DESC")
    LiveData<List<FavoritoEntity>> observarTodos();

    @Query("SELECT referenciaId FROM favorito WHERE tipo = :tipo ORDER BY criadoEm DESC")
    LiveData<List<String>> observarIds(String tipo);

    @Query("SELECT referenciaId FROM favorito WHERE tipo = :tipo ORDER BY criadoEm DESC")
    List<String> listarIds(String tipo);

    @Query("SELECT COUNT(*) FROM favorito WHERE tipo = :tipo AND referenciaId = :referenciaId")
    int existe(String tipo, String referenciaId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserir(FavoritoEntity favorito);

    @Query("DELETE FROM favorito WHERE tipo = :tipo AND referenciaId = :referenciaId")
    void remover(String tipo, String referenciaId);
}
