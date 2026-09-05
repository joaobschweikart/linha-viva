package br.unoesc.linhaviva.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import br.unoesc.linhaviva.data.local.dao.AvisoDao;
import br.unoesc.linhaviva.data.local.dao.FavoritoDao;
import br.unoesc.linhaviva.data.local.dao.HorarioDao;
import br.unoesc.linhaviva.data.local.dao.InformacaoDao;
import br.unoesc.linhaviva.data.local.dao.ItinerarioDao;
import br.unoesc.linhaviva.data.local.dao.LinhaDao;
import br.unoesc.linhaviva.data.local.dao.PontoDao;
import br.unoesc.linhaviva.data.local.dao.PrevisaoDao;
import br.unoesc.linhaviva.data.local.entity.AvisoEntity;
import br.unoesc.linhaviva.data.local.entity.FavoritoEntity;
import br.unoesc.linhaviva.data.local.entity.HorarioEntity;
import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;
import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity;

@Database(
        entities = {
                LinhaEntity.class,
                PontoEntity.class,
                ItinerarioEntity.class,
                HorarioEntity.class,
                PrevisaoEntity.class,
                PrevisaoLinhaEntity.class,
                FavoritoEntity.class,
                AvisoEntity.class,
                InformacaoEntity.class
        },
        version = 1,
        exportSchema = false)
public abstract class BancoLocal extends RoomDatabase {

    private static final String NOME = "linha-viva.db";
    private static volatile BancoLocal instancia;

    public abstract LinhaDao linhaDao();

    public abstract PontoDao pontoDao();

    public abstract ItinerarioDao itinerarioDao();

    public abstract HorarioDao horarioDao();

    public abstract PrevisaoDao previsaoDao();

    public abstract FavoritoDao favoritoDao();

    public abstract AvisoDao avisoDao();

    public abstract InformacaoDao informacaoDao();

    public static BancoLocal get(Context contexto) {
        if (instancia == null) {
            synchronized (BancoLocal.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                                    contexto.getApplicationContext(), BancoLocal.class, NOME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instancia;
    }
}
