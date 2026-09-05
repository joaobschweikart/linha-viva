package br.unoesc.linhaviva.ui.linha;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import br.unoesc.linhaviva.data.local.entity.HorarioEntity;
import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.remote.dto.VeiculoDto;
import br.unoesc.linhaviva.data.repository.RepositorioFavoritos;
import br.unoesc.linhaviva.data.repository.RepositorioLinhas;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.Formatador;
import br.unoesc.linhaviva.util.EstadoConexao;
import br.unoesc.linhaviva.util.Preferencias;

public class LinhaDetalheViewModel extends AndroidViewModel {

    private final RepositorioLinhas repositorioLinhas;
    private final RepositorioFavoritos repositorioFavoritos;
    private final Preferencias preferencias;
    private final EstadoConexao conexao;

    private final MutableLiveData<String> sentido = new MutableLiveData<>("IDA");
    private final MutableLiveData<String> diaTipo = new MutableLiveData<>(Formatador.diaTipoDeHoje());
    private final MutableLiveData<String> pontoDoVeiculo = new MutableLiveData<>();
    private final MutableLiveData<String> proximaPartida = new MutableLiveData<>();
    private final MutableLiveData<EstadoCarga> estado = new MutableLiveData<>(EstadoCarga.ocioso());
    private final MediatorLiveData<List<ItemItinerario>> itinerario = new MediatorLiveData<>();

    private LiveData<LinhaEntity> linha;
    private LiveData<List<ItinerarioEntity>> itinerarioBruto;
    private LiveData<List<HorarioEntity>> horarios;
    private LiveData<Boolean> favorita;
    private String linhaId;

    public LinhaDetalheViewModel(@NonNull Application application) {
        super(application);
        repositorioLinhas = new RepositorioLinhas(application);
        repositorioFavoritos = new RepositorioFavoritos(application);
        preferencias = new Preferencias(application);
        conexao = EstadoConexao.get(application);
    }

    public void iniciar(String id) {
        if (linhaId != null) return;
        linhaId = id;

        linha = repositorioLinhas.observarLinha(id);
        itinerarioBruto = Transformations.switchMap(sentido,
                s -> repositorioLinhas.observarItinerario(id, s));
        horarios = Transformations.switchMap(diaTipo, dia -> {
            String s = sentido.getValue() == null ? "IDA" : sentido.getValue();
            return repositorioLinhas.observarHorarios(id, s, dia);
        });
        favorita = Transformations.map(repositorioFavoritos.observarIdsDeLinhas(),
                ids -> ids != null && ids.contains(id));

        itinerario.addSource(itinerarioBruto, v -> montarItinerario());
        itinerario.addSource(proximaPartida, v -> montarItinerario());
        itinerario.addSource(pontoDoVeiculo, v -> montarItinerario());

        recalcularProximaPartida();
    }

    public LiveData<LinhaEntity> linha() {
        return linha;
    }

    public LiveData<List<ItemItinerario>> itinerario() {
        return itinerario;
    }

    public LiveData<List<HorarioEntity>> horarios() {
        return horarios;
    }

    public LiveData<Boolean> favorita() {
        return favorita;
    }

    public LiveData<String> proximaPartida() {
        return proximaPartida;
    }

    public LiveData<EstadoCarga> estado() {
        return estado;
    }

    public LiveData<Boolean> online() {
        return conexao.online();
    }

    public LiveData<String> sentido() {
        return sentido;
    }

    public LiveData<String> diaTipo() {
        return diaTipo;
    }

    public long ultimaSincronizacao() {
        return preferencias.ultimaSincronizacao();
    }

    public String sentidoAtual() {
        return sentido.getValue() == null ? "IDA" : sentido.getValue();
    }

    public void definirSentido(String novo) {
        if (novo.equals(sentido.getValue())) return;
        sentido.setValue(novo);
        String dia = diaTipo.getValue();
        diaTipo.setValue(dia);
        recalcularProximaPartida();
    }

    public void definirDiaTipo(String novo) {
        if (!novo.equals(diaTipo.getValue())) diaTipo.setValue(novo);
    }

    public void alternarFavorito() {
        AppExecutors.get().io().execute(() -> repositorioFavoritos.alternar("LINHA", linhaId));
    }

    public void atualizar() {
        if (linhaId == null) return;
        estado.setValue(EstadoCarga.carregando());
        String s = sentidoAtual();
        AppExecutors.get().io().execute(() -> {
            Resultado<Boolean> resultado = repositorioLinhas.atualizarLinha(linhaId, s);
            estado.postValue(resultado.sucesso ? EstadoCarga.pronto() : EstadoCarga.erro(resultado.erro));
            recalcularProximaPartidaSincrono(s);

            Resultado<VeiculoDto> veiculo = repositorioLinhas.buscarVeiculo(linhaId, s);
            pontoDoVeiculo.postValue(veiculo.sucesso && veiculo.dados != null
                    ? veiculo.dados.proximoPontoId : null);
        });
    }

    private void recalcularProximaPartida() {
        String s = sentidoAtual();
        AppExecutors.get().io().execute(() -> recalcularProximaPartidaSincrono(s));
    }

    private void recalcularProximaPartidaSincrono(String s) {
        proximaPartida.postValue(repositorioLinhas.proximaPartidaProgramada(linhaId, s));
    }

    private void montarItinerario() {
        List<ItinerarioEntity> base = itinerarioBruto.getValue();
        if (base == null || base.isEmpty()) {
            itinerario.setValue(new ArrayList<>());
            return;
        }

        String partida = proximaPartida.getValue();
        int baseMin = partida != null ? Formatador.horaParaMinutos(partida) : -1;
        String veiculoEm = pontoDoVeiculo.getValue();
        int agora = Formatador.minutosDoDiaAgora();

        List<ItemItinerario> lista = new ArrayList<>();
        for (int i = 0; i < base.size(); i++) {
            ItinerarioEntity item = base.get(i);
            String hora = "--:--";
            boolean jaPassou = false;
            if (baseMin >= 0) {
                int minutos = (baseMin + item.tempoAcumuladoMin) % (24 * 60);
                hora = String.format(java.util.Locale.US, "%02d:%02d", minutos / 60, minutos % 60);
                jaPassou = minutos < agora;
            }
            lista.add(new ItemItinerario(item, hora, i == 0, i == base.size() - 1,
                    jaPassou, item.pontoId.equals(veiculoEm)));
        }
        itinerario.setValue(lista);
    }
}
