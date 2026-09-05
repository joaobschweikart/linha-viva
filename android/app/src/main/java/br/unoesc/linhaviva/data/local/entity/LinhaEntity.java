package br.unoesc.linhaviva.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "linha")
public class LinhaEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String numero;
    public String nome;
    public String cor;
    public boolean circular;
    public boolean acessivel;
    public String empresa;
    public String sentidoIda;
    public String sentidoVolta;
    public String primeiraPartida;
    public String ultimaPartida;
    public int intervaloMin;
    public int totalPontos;
    public int duracaoMin;
    public long atualizadoEm;

    public String descricaoSentido(String sentido) {
        return "VOLTA".equals(sentido) ? sentidoVolta : sentidoIda;
    }

    /** Só o destino do sentido, para cabeçalhos com pouco espaço. */
    public String destinoDoSentido(String sentido) {
        String descricao = descricaoSentido(sentido);
        if (descricao == null) return nome;
        int seta = descricao.lastIndexOf('\u2192');
        return seta < 0 ? descricao : descricao.substring(seta + 1).trim();
    }
}
