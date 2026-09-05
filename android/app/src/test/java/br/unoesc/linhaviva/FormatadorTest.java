package br.unoesc.linhaviva;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import br.unoesc.linhaviva.util.Formatador;

public class FormatadorTest {

    @Test
    public void converteHoraParaMinutosDoDia() {
        assertEquals(0, Formatador.horaParaMinutos("00:00"));
        assertEquals(485, Formatador.horaParaMinutos("08:05"));
        assertEquals(1439, Formatador.horaParaMinutos("23:59"));
    }

    @Test
    public void retornaNegativoParaHoraInvalida() {
        assertEquals(-1, Formatador.horaParaMinutos("abc"));
        assertEquals(-1, Formatador.horaParaMinutos(""));
    }

    @Test
    public void formataDistanciaEmMetrosEQuilometros() {
        assertEquals("120 m", Formatador.distancia(120));
        assertEquals("999 m", Formatador.distancia(999));
        assertEquals("1,0 km", Formatador.distancia(1000));
        assertEquals("2,5 km", Formatador.distancia(2540));
    }

    @Test
    public void estimaTempoDeCaminhadaComMinimoDeUmMinuto() {
        assertEquals("1 min", Formatador.minutosAPe(10));
        assertEquals("2 min", Formatador.minutosAPe(120));
        assertEquals("10 min", Formatador.minutosAPe(800));
    }

    @Test
    public void descreveTempoDecorridoDeFormaCurta() {
        long agora = System.currentTimeMillis();
        assertEquals("0 s", Formatador.tempoRelativo(agora));
        assertEquals("2 min", Formatador.tempoRelativo(agora - 125_000L));
        assertEquals("3 h", Formatador.tempoRelativo(agora - 3 * 3_600_000L));
    }
}
