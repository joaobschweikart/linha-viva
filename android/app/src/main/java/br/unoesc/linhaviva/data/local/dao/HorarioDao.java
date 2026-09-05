package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.HorarioEntity;

@Dao
public interface HorarioDao {

    @Query("SELECT * FROM horario WHERE linhaId = :linhaId AND sentido = :sentido AND diaTipo = :diaTipo ORDER BY hora")
    LiveData<List<HorarioEntity>> observar(String linhaId, String sentido, String diaTipo);

    @Query("SELECT * FROM horario WHERE linhaId = :linhaId AND sentido = :sentido AND diaTipo = :diaTipo ORDER BY hora")
    List<HorarioEntity> listar(String linhaId, String sentido, String diaTipo);

    @Query("SELECT * FROM horario WHERE diaTipo = :diaTipo ORDER BY linhaId, hora")
    List<HorarioEntity> listarPorDia(String diaTipo);

    @Query("SELECT hora FROM horario WHERE linhaId = :linhaId AND sentido = :sentido AND diaTipo = :diaTipo AND hora >= :horaMinima ORDER BY hora LIMIT 1")
    String proximaPartida(String linhaId, String sentido, String diaTipo, String horaMinima);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirTodos(List<HorarioEntity> horarios);

    @Query("DELETE FROM horario WHERE linhaId = :linhaId AND sentido = :sentido")
    void limpar(String linhaId, String sentido);
}
