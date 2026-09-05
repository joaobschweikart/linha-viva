package br.unoesc.linhaviva;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import br.unoesc.linhaviva.data.remote.dto.AvisoDto;
import br.unoesc.linhaviva.data.remote.dto.EnvelopeDto;
import br.unoesc.linhaviva.data.remote.dto.HorarioDto;
import br.unoesc.linhaviva.data.remote.dto.InformacaoDto;
import br.unoesc.linhaviva.data.remote.dto.ItinerarioDto;
import br.unoesc.linhaviva.data.remote.dto.LinhaDto;
import br.unoesc.linhaviva.data.remote.dto.PontoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoLinhaDto;
import br.unoesc.linhaviva.data.remote.dto.VeiculoDto;
import br.unoesc.linhaviva.data.remote.dto.VersaoDto;

/**
 * Verifica que os DTOs do aplicativo continuam compatíveis com as respostas reais
 * da API. As fixtures foram capturadas do servidor em api/server.js.
 */
public class ContratoApiTest {

    private final Gson gson = new Gson();

    private <T> T ler(String arquivo, Type tipo) throws Exception {
        InputStream entrada = getClass().getClassLoader()
                .getResourceAsStream("fixtures/" + arquivo);
        assertNotNull("fixture ausente: " + arquivo, entrada);
        try (Reader leitor = new InputStreamReader(entrada, StandardCharsets.UTF_8)) {
            return gson.fromJson(leitor, tipo);
        }
    }

    @Test
    public void versaoTrazAssinaturaDoCatalogo() throws Exception {
        VersaoDto versao = ler("versao.json", VersaoDto.class);
        assertNotNull(versao.versaoDados);
        assertFalse(versao.versaoDados.isEmpty());
        assertTrue(versao.linhas > 0);
        assertTrue(versao.pontos > 0);
    }

    @Test
    public void linhasTrazemCamposUsadosNaListagem() throws Exception {
        EnvelopeDto<List<LinhaDto>> envelope = ler("linhas.json",
                new TypeToken<EnvelopeDto<List<LinhaDto>>>() {
                }.getType());

        assertNotNull(envelope.fonte);
        assertNotNull(envelope.dados);
        assertFalse(envelope.dados.isEmpty());

        LinhaDto linha = envelope.dados.get(0);
        assertNotNull(linha.id);
        assertNotNull(linha.numero);
        assertNotNull(linha.nome);
        assertNotNull(linha.sentidoIda);
        assertNotNull(linha.sentidoVolta);
        assertTrue(linha.intervaloMin > 0);
        assertTrue(linha.duracaoMin > 0);
    }

    @Test
    public void pontosTrazemCoordenadasEAcessibilidade() throws Exception {
        EnvelopeDto<List<PontoDto>> envelope = ler("pontos.json",
                new TypeToken<EnvelopeDto<List<PontoDto>>>() {
                }.getType());

        assertFalse(envelope.dados.isEmpty());
        for (PontoDto ponto : envelope.dados) {
            assertNotNull(ponto.id);
            assertNotNull(ponto.nome);
            assertNotNull(ponto.bairro);
            assertTrue("latitude fora de Chapecó: " + ponto.latitude,
                    ponto.latitude < -26.5 && ponto.latitude > -27.6);
            assertTrue("longitude fora de Chapecó: " + ponto.longitude,
                    ponto.longitude < -52.0 && ponto.longitude > -53.2);
        }
    }

    @Test
    public void itinerariosVemOrdenadosComTempoAcumulado() throws Exception {
        EnvelopeDto<List<ItinerarioDto>> envelope = ler("itinerarios.json",
                new TypeToken<EnvelopeDto<List<ItinerarioDto>>>() {
                }.getType());

        assertFalse(envelope.dados.isEmpty());
        String linhaAtual = null;
        String sentidoAtual = null;
        int ordemAnterior = -1;
        int tempoAnterior = -1;

        for (ItinerarioDto item : envelope.dados) {
            boolean novoTrecho = !item.linhaId.equals(linhaAtual) || !item.sentido.equals(sentidoAtual);
            if (novoTrecho) {
                linhaAtual = item.linhaId;
                sentidoAtual = item.sentido;
                ordemAnterior = -1;
                tempoAnterior = -1;
            }
            assertEquals("ordem deve ser sequencial", ordemAnterior + 1, item.ordem);
            assertTrue("tempo acumulado deve crescer", item.tempoAcumuladoMin >= tempoAnterior);
            assertNotNull(item.pontoId);
            assertNotNull(item.pontoNome);
            ordemAnterior = item.ordem;
            tempoAnterior = item.tempoAcumuladoMin;
        }
    }

    @Test
    public void horariosUsamFormatoDeVinteQuatroHoras() throws Exception {
        EnvelopeDto<List<HorarioDto>> envelope = ler("horarios.json",
                new TypeToken<EnvelopeDto<List<HorarioDto>>>() {
                }.getType());

        assertFalse(envelope.dados.isEmpty());
        for (HorarioDto horario : envelope.dados) {
            assertTrue("hora inválida: " + horario.hora, horario.hora.matches("^\\d{2}:\\d{2}$"));
            assertTrue("dia inválido: " + horario.diaTipo,
                    horario.diaTipo.equals("UTIL")
                            || horario.diaTipo.equals("SABADO")
                            || horario.diaTipo.equals("DOMINGO"));
        }
    }

    @Test
    public void previsoesDeLinhasIdentificamAOrigemDoDado() throws Exception {
        EnvelopeDto<List<PrevisaoLinhaDto>> envelope = ler("previsoes-linhas.json",
                new TypeToken<EnvelopeDto<List<PrevisaoLinhaDto>>>() {
                }.getType());

        assertFalse(envelope.dados.isEmpty());
        for (PrevisaoLinhaDto previsao : envelope.dados) {
            assertNotNull(previsao.linhaId);
            assertNotNull(previsao.origem);
            assertNotNull(previsao.pontoReferenciaId);
            assertTrue("origem desconhecida: " + previsao.origem,
                    previsao.origem.equals("SIMULADO_TEMPO_REAL")
                            || previsao.origem.equals("HORARIO_PROGRAMADO")
                            || previsao.origem.equals("SEM_OPERACAO"));
        }
    }

    @Test
    public void previsoesDoPontoTrazemHoraPrevistaEProgramada() throws Exception {
        EnvelopeDto<List<PrevisaoDto>> envelope = ler("previsoes-ponto.json",
                new TypeToken<EnvelopeDto<List<PrevisaoDto>>>() {
                }.getType());

        assertNotNull(envelope.observacao);
        for (PrevisaoDto previsao : envelope.dados) {
            assertNotNull(previsao.linhaNumero);
            assertNotNull(previsao.linhaNome);
            assertNotNull(previsao.horaPrevista);
            assertNotNull(previsao.horaProgramada);
            assertTrue(previsao.minutos >= 0);
        }
    }

    @Test
    public void veiculoDeclaraQueAPosicaoESimulada() throws Exception {
        EnvelopeDto<VeiculoDto> envelope = ler("veiculo.json",
                new TypeToken<EnvelopeDto<VeiculoDto>>() {
                }.getType());

        if (Boolean.FALSE.equals(envelope.emOperacao)) return;

        VeiculoDto veiculo = envelope.dados;
        assertNotNull(veiculo);
        assertEquals("SIMULADO", veiculo.origem);
        assertNotNull(veiculo.prefixo);
        assertNotNull(veiculo.proximoPontoId);
        assertTrue(veiculo.progresso >= 0.0 && veiculo.progresso <= 1.0);
    }

    @Test
    public void avisosEInformacoesEstaoCategorizados() throws Exception {
        EnvelopeDto<List<AvisoDto>> avisos = ler("avisos.json",
                new TypeToken<EnvelopeDto<List<AvisoDto>>>() {
                }.getType());
        assertFalse(avisos.dados.isEmpty());
        for (AvisoDto aviso : avisos.dados) {
            assertNotNull(aviso.id);
            assertNotNull(aviso.titulo);
            assertNotNull(aviso.severidade);
        }

        EnvelopeDto<List<InformacaoDto>> informacoes = ler("informacoes.json",
                new TypeToken<EnvelopeDto<List<InformacaoDto>>>() {
                }.getType());
        assertFalse(informacoes.dados.isEmpty());
        for (InformacaoDto info : informacoes.dados) {
            assertTrue("categoria inesperada: " + info.categoria,
                    info.categoria.equals("TARIFA")
                            || info.categoria.equals("TERMINAL")
                            || info.categoria.equals("CONTATO"));
        }
    }

    @Test
    public void respostasIdentificamOsDadosComoFicticios() throws Exception {
        EnvelopeDto<List<LinhaDto>> envelope = ler("linhas.json",
                new TypeToken<EnvelopeDto<List<LinhaDto>>>() {
                }.getType());
        assertTrue("a API deve declarar que os dados são fictícios",
                envelope.fonte.toLowerCase().contains("fictícios"));
    }
}
