package br.unoesc.linhaviva.data.repository;

import androidx.annotation.Nullable;

public final class Resultado<T> {

    @Nullable public final T dados;
    @Nullable public final String erro;
    public final boolean sucesso;

    private Resultado(@Nullable T dados, @Nullable String erro, boolean sucesso) {
        this.dados = dados;
        this.erro = erro;
        this.sucesso = sucesso;
    }

    public static <T> Resultado<T> ok(@Nullable T dados) {
        return new Resultado<>(dados, null, true);
    }

    public static <T> Resultado<T> falha(String erro) {
        return new Resultado<>(null, erro, false);
    }
}
