package br.unoesc.linhaviva.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import br.unoesc.linhaviva.data.Mapeador;
import br.unoesc.linhaviva.data.local.BancoLocal;
import br.unoesc.linhaviva.data.local.entity.HorarioEntity;
import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity;
import br.unoesc.linhaviva.data.remote.ApiLinhaViva;
import br.unoesc.linhaviva.data.remote.ClienteApi;
import br.unoesc.linhaviva.data.remote.dto.HorarioDto;
import br.unoesc.linhaviva.data.remote.dto.ItinerarioDto;
import br.unoesc.linhaviva.data.remote.dto.VeiculoDto;
import br.unoesc.linhaviva.util.Formatador;
import br.unoesc.linhaviva.util.MonitorConectividade;

public class RepositorioLinhas {

    private final Context contexto;
    private final BancoLocal banco;

    public RepositorioLinhas(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.banco = BancoLocal.get(contexto);
    }

    public LiveData<List<LinhaEntity>> observarLinhas() {
        return banco.linhaDao().observarTodas();
    }

    public LiveData<LinhaEntity> observarLinha(String linhaId) {
        return banco.linhaDao().observar(linhaId);
    }

    public LiveData<List<PrevisaoLinhaEntity>> observarPrevisoes() {
        return banco.previsaoDao().observarDeLinhas();
    }

    public LiveData<List<ItinerarioEntity>> observarItinerario(String linhaId, String sentido) {
        return banco.itinerarioDao().observar(linhaId, sentido);
    }

    public LiveData<List<HorarioEntity>> observarHorarios(String linhaId, String sentido, String diaTipo) {
        return banco.horarioDao().observar(linhaId, sentido, diaTipo);
    }

    /** Itinerarios completos, usados para busca por bairro/ponto e para proximidade. */
    public List<ItinerarioEntity> itinerariosParaBusca() {
        return banco.itinerarioDao().listarTodos();
    }

    public List<ItinerarioEntity> itinerarioSincrono(String linhaId, String sentido) {
        return banco.itinerarioDao().listar(linhaId, sentido);
    }

    /** Proxima partida programada, calculada a partir do banco local (funciona offline). */
    public String proximaPartidaProgramada(String linhaId, String sentido) {
        String diaTipo = Formatador.diaTipoDeHoje();
        int agora = Formatador.minutosDoDiaAgora();
        String horaAtual = String.format(java.util.Locale.US, "%02d:%02d", agora / 60, agora % 60);
        String proxima = banco.horarioDao().proximaPartida(linhaId, sentido, diaTipo, horaAtual);
        if (proxima != null) return proxima;
        List<HorarioEntity> doDia = banco.horarioDao().listar(linhaId, sentido, diaTipo);
        return doDia.isEmpty() ? null : doDia.get(0).hora;
    }

    /** Atualiza itinerario e horarios de uma linha especifica (tela de detalhe). */
    public Resultado<Boolean> atualizarLinha(String linhaId, String sentido) {
        if (!MonitorConectividade.temConexao(contexto)) {
            ChamadaApi.registrar(contexto);
            ChamadaApi.alcancou(false);
            return Resultado.falha("Sem conexão com a internet");
        }
        ChamadaApi.registrar(contexto);
        ApiLinhaViva api = ClienteApi.get(contexto);

        Resultado<List<ItinerarioDto>> itinerario = ChamadaApi.executar(api.itinerario(linhaId, sentido));
        if (!itinerario.sucesso) return Resultado.falha(itinerario.erro);

        Resultado<List<HorarioDto>> horarios = ChamadaApi.executar(api.horarios(linhaId, sentido));
        if (!horarios.sucesso) return Resultado.falha(horarios.erro);

        banco.runInTransaction(() -> {
            banco.itinerarioDao().limpar(linhaId, sentido);
            banco.itinerarioDao().inserirTodos(Mapeador.itinerarios(itinerario.dados));
            banco.horarioDao().limpar(linhaId, sentido);
            banco.horarioDao().inserirTodos(Mapeador.horarios(horarios.dados));
        });
        return Resultado.ok(true);
    }

    /** Posicao do veiculo. Nao e persistida: sem rede, a tela cai para o horario programado (RNF12). */
    public Resultado<VeiculoDto> buscarVeiculo(String linhaId, String sentido) {
        if (!MonitorConectividade.temConexao(contexto)) {
            ChamadaApi.registrar(contexto);
            ChamadaApi.alcancou(false);
            return Resultado.falha("Sem conexão com a internet");
        }
        ChamadaApi.registrar(contexto);
        return ChamadaApi.executar(ClienteApi.get(contexto).veiculo(linhaId, sentido));
    }
}
