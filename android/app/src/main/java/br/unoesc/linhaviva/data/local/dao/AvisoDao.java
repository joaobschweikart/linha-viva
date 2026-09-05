package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.AvisoEntity;

@Dao
public interface AvisoDao {

    @Query("SELECT * FROM aviso ORDER BY publicadoEm DESC")
    LiveData<List<AvisoEntity>> observarTodos();

    @Query("SELECT * FROM aviso WHERE linhaId = :linhaId ORDER BY publicadoEm DESC")
    LiveData<List<AvisoEntity>> observarPorLinha(String linhaId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirTodos(List<AvisoEntity> avisos);

    @Query("DELETE FROM aviso")
    void limpar();
}
