package br.unoesc.linhaviva.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity;

@Dao
public interface PrevisaoDao {

    @Query("SELECT * FROM previsao WHERE pontoId = :pontoId ORDER BY minutos")
    LiveData<List<PrevisaoEntity>> observarDoPonto(String pontoId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirDoPonto(List<PrevisaoEntity> previsoes);

    @Query("DELETE FROM previsao WHERE pontoId = :pontoId")
    void limparPonto(String pontoId);

    @Query("SELECT * FROM previsao_linha")
    LiveData<List<PrevisaoLinhaEntity>> observarDeLinhas();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserirDeLinhas(List<PrevisaoLinhaEntity> previsoes);

    @Query("DELETE FROM previsao_linha")
    void limparDeLinhas();
}
