package br.unoesc.linhaviva.util;

import androidx.annotation.Nullable;

/**
 * Resolve quantos minutos faltam para uma partida.
 *
 * Os minutos vêm sempre do horário, nunca do valor recebido do servidor: uma
 * previsão guardada há dez minutos não pode ser reapresentada como atual. Quando
 * a previsão em cache já venceu, o cálculo cai para a tabela de horários
 * programados, que existe no banco local e funciona sem rede (RNF12).
 */
public final class CalculoPrevisao {

    /** Além desta janela a partida deixa de ser útil como "próxima". */
    private static final int JANELA_MAXIMA_MIN = 180;
    private static final int TOLERANCIA_ATRASO_MIN = 2;

    public static class Resultado {
        @Nullable public final Integer minutos;
        public final boolean daPrevisao;

        Resultado(@Nullable Integer minutos, boolean daPrevisao) {
            this.minutos = minutos;
            this.daPrevisao = daPrevisao;
        }
    }

    private CalculoPrevisao() {
    }

    @Nullable
    public static Integer minutosAte(@Nullable String hora) {
        if (hora == null) return null;
        int alvo = Formatador.horaParaMinutos(hora);
        if (alvo < 0) return null;

        int diferenca = alvo - Formatador.minutosDoDiaAgora();
        if (diferenca < -TOLERANCIA_ATRASO_MIN) return null;
        if (diferenca > JANELA_MAXIMA_MIN) return null;
        return Math.max(0, diferenca);
    }

    public static Resultado resolver(@Nullable String horaPrevista, @Nullable String horaProgramada) {
        Integer daPrevisao = minutosAte(horaPrevista);
        if (daPrevisao != null) return new Resultado(daPrevisao, true);
        return new Resultado(minutosAte(horaProgramada), false);
    }
}
