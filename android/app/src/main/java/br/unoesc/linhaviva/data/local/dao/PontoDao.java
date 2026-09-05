package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.PontoEntity;

@Dao
public interface PontoDao {

    @Query("SELECT * FROM ponto ORDER BY nome")
    LiveData<List<PontoEntity>> observarTodos();

    @Query("SELECT * FROM ponto ORDER BY nome")
    List<PontoEntity> listarTodos();

    @Query("SELECT * FROM ponto WHERE id = :id")
    LiveData<PontoEntity> observar(String id);

    @Query("SELECT * FROM ponto WHERE id = :id")
    PontoEntity buscar(String id);

    @Query("SELECT * FROM ponto WHERE id IN (:ids)")
    LiveData<List<PontoEntity>> observarPorIds(List<String> ids);

    @Query("SELECT COUNT(*) FROM ponto")
    int total();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirTodos(List<PontoEntity> pontos);
}
