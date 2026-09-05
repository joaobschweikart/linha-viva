package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;

@Dao
public interface ItinerarioDao {

    @Query("SELECT * FROM itinerario WHERE linhaId = :linhaId AND sentido = :sentido ORDER BY ordem")
    LiveData<List<ItinerarioEntity>> observar(String linhaId, String sentido);

    @Query("SELECT * FROM itinerario WHERE linhaId = :linhaId AND sentido = :sentido ORDER BY ordem")
    List<ItinerarioEntity> listar(String linhaId, String sentido);

    @Query("SELECT * FROM itinerario ORDER BY linhaId, sentido, ordem")
    LiveData<List<ItinerarioEntity>> observarTodos();

    @Query("SELECT * FROM itinerario ORDER BY linhaId, sentido, ordem")
    List<ItinerarioEntity> listarTodos();

    @Query("SELECT DISTINCT linhaId FROM itinerario WHERE pontoId = :pontoId")
    List<String> linhasNoPonto(String pontoId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirTodos(List<ItinerarioEntity> itens);

    @Query("DELETE FROM itinerario WHERE linhaId = :linhaId AND sentido = :sentido")
    void limpar(String linhaId, String sentido);
}
