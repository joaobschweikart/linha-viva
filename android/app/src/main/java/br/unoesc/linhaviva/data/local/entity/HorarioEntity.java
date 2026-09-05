package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(tableName = "horario",
        primaryKeys = {"linhaId", "sentido", "diaTipo", "hora"},
        indices = {@Index("linhaId")})
public class HorarioEntity {

    @NonNull public String linhaId = "";
    @NonNull public String sentido = "IDA";
    @NonNull public String diaTipo = "UTIL";
    @NonNull public String hora = "00:00";
}
