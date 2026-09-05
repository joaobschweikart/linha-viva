package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.LinhaEntity;

@Dao
public interface LinhaDao {

    @Query("SELECT * FROM linha ORDER BY numero")
    LiveData<List<LinhaEntity>> observarTodas();

    @Query("SELECT * FROM linha ORDER BY numero")
    List<LinhaEntity> listarTodas();

    @Query("SELECT * FROM linha WHERE id = :id")
    LiveData<LinhaEntity> observar(String id);

    @Query("SELECT * FROM linha WHERE id = :id")
    LinhaEntity buscar(String id);

    @Query("SELECT COUNT(*) FROM linha")
    int total();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirTodas(List<LinhaEntity> linhas);

    @Query("DELETE FROM linha WHERE id NOT IN (:idsMantidos)")
    void removerAusentes(List<String> idsMantidos);
}
