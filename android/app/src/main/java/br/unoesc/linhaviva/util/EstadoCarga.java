package br.unoesc.linhaviva.util;

import androidx.annotation.Nullable;

public final class EstadoCarga {

    public enum Fase { OCIOSO, CARREGANDO, PRONTO, ERRO }

    public final Fase fase;
    @Nullable public final String mensagem;

    private EstadoCarga(Fase fase, @Nullable String mensagem) {
        this.fase = fase;
        this.mensagem = mensagem;
    }

    public static EstadoCarga ocioso() {
        return new EstadoCarga(Fase.OCIOSO, null);
    }

    public static EstadoCarga carregando() {
        return new EstadoCarga(Fase.CARREGANDO, null);
    }

    public static EstadoCarga pronto() {
        return new EstadoCarga(Fase.PRONTO, null);
    }

    public static EstadoCarga erro(String mensagem) {
        return new EstadoCarga(Fase.ERRO, mensagem);
    }

    public boolean estaCarregando() {
        return fase == Fase.CARREGANDO;
    }

    public boolean falhou() {
        return fase == Fase.ERRO;
    }
}
