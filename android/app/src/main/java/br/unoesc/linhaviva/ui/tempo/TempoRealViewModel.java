package br.unoesc.linhaviva.ui.tempo;

import android.app.Application;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.remote.dto.VeiculoDto;
import br.unoesc.linhaviva.data.repository.RepositorioFavoritos;
import br.unoesc.linhaviva.data.repository.RepositorioLinhas;
import br.unoesc.linhaviva.data.repository.RepositorioPontos;
import br.unoesc.linhaviva.data.repository.Resultado;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.LocalizacaoLiveData;
import br.unoesc.linhaviva.util.EstadoConexao;

/**
 * Acompanhamento do veiculo. A consulta so ocorre enquanto a tela esta visivel
 * e em intervalo fixo, evitando polling continuo em segundo plano (RNF03).
 */
public class TempoRealViewModel extends AndroidViewModel {

    public static final int PARADAS_PARA_AVISO = 2;
    private static final long INTERVALO_MS = 15_000L;

    private final RepositorioLinhas repositorioLinhas;
    private final RepositorioPontos repositorioPontos;
    private final RepositorioFavoritos repositorioFavoritos;
    private final EstadoConexao conexao;
    private final LocalizacaoLiveData localizacao;

    private final MutableLiveData<VeiculoDto> veiculo = new MutableLiveData<>();
    private final MutableLiveData<List<ItinerarioEntity>> itinerario = new MutableLiveData<>();
    private final MutableLiveData<PontoEntity> pontoDeReferencia = new MutableLiveData<>();
    private final MutableLiveData<Integer> paradasRestantes = new MutableLiveData<>();
    private final MutableLiveData<Long> atualizadoEm = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> avisoAtivo = new MutableLiveData<>(false);
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private final Handler agenda = new Handler(Looper.getMainLooper());
    private final Runnable ciclo = new Runnable() {
        @Override
        public void run() {
            consultar();
            agenda.postDelayed(this, INTERVALO_MS);
        }
    };

    private LiveData<LinhaEntity> linha;
    private LiveData<Boolean> favorita;
    private String linhaId;
    private String sentido;
    private boolean ativo;

    public TempoRealViewModel(@NonNull Application application) {
        super(application);
        repositorioLinhas = new RepositorioLinhas(application);
        repositorioPontos = new RepositorioPontos(application);
        repositorioFavoritos = new RepositorioFavoritos(application);
        conexao = EstadoConexao.get(application);
        localizacao = new LocalizacaoLiveData(application);
    }

    public void iniciar(String id, String sentidoInicial) {
        if (linhaId != null) return;
        linhaId = id;
        sentido = sentidoInicial;
        linha = repositorioLinhas.observarLinha(id);
        favorita = Transformations.map(repositorioFavoritos.observarIdsDeLinhas(),
                ids -> ids != null && ids.contains(id));

        AppExecutors.get().io().execute(() -> {
            List<ItinerarioEntity> pontos = repositorioLinhas.itinerarioSincrono(linhaId, sentido);
            itinerario.postValue(pontos);
            pontoDeReferencia.postValue(escolherPontoDeReferencia(pontos));
        });
    }

    public LiveData<LinhaEntity> linha() {
        return linha;
    }

    public LiveData<Boolean> favorita() {
        return favorita;
    }

    public LiveData<VeiculoDto> veiculo() {
        return veiculo;
    }

    public LiveData<List<ItinerarioEntity>> itinerario() {
        return itinerario;
    }

    public LiveData<PontoEntity> pontoDeReferencia() {
        return pontoDeReferencia;
    }

    public LiveData<Integer> paradasRestantes() {
        return paradasRestantes;
    }

    public LiveData<Long> atualizadoEm() {
        return atualizadoEm;
    }

    public LiveData<Boolean> avisoAtivo() {
        return avisoAtivo;
    }

    public LiveData<String> erro() {
        return erro;
    }

    public LiveData<Boolean> online() {
        return conexao.online();
    }

    public String sentido() {
        return sentido;
    }

    public void alternarAviso() {
        avisoAtivo.setValue(!Boolean.TRUE.equals(avisoAtivo.getValue()));
    }

    public void consumirAviso() {
        avisoAtivo.setValue(false);
    }

    public void alternarFavorito() {
        AppExecutors.get().io().execute(() -> repositorioFavoritos.alternar("LINHA", linhaId));
    }

    public void retomar() {
        if (ativo) return;
        ativo = true;
        agenda.post(ciclo);
    }

    public void pausar() {
        ativo = false;
        agenda.removeCallbacks(ciclo);
    }

    private void consultar() {
        if (linhaId == null) return;
        AppExecutors.get().io().execute(() -> {
            Resultado<VeiculoDto> resultado = repositorioLinhas.buscarVeiculo(linhaId, sentido);
            if (resultado.sucesso) {
                veiculo.postValue(resultado.dados);
                atualizadoEm.postValue(System.currentTimeMillis());
                erro.postValue(null);
                paradasRestantes.postValue(calcularParadasRestantes(resultado.dados));
            } else {
                erro.postValue(resultado.erro);
            }
        });
    }

    private Integer calcularParadasRestantes(VeiculoDto dados) {
        List<ItinerarioEntity> pontos = itinerario.getValue();
        PontoEntity referencia = pontoDeReferencia.getValue();
        if (dados == null || pontos == null || referencia == null) return null;

        int ordemVeiculo = -1;
        int ordemReferencia = -1;
        for (ItinerarioEntity item : pontos) {
            if (item.pontoId.equals(dados.proximoPontoId)) ordemVeiculo = item.ordem;
            if (item.pontoId.equals(referencia.id)) ordemReferencia = item.ordem;
        }
        if (ordemVeiculo < 0 || ordemReferencia < 0) return null;
        return Math.max(0, ordemReferencia - ordemVeiculo);
    }

    /** Usa o ponto do itinerario mais proximo do usuario; sem GPS, o proximo terminal. */
    private PontoEntity escolherPontoDeReferencia(List<ItinerarioEntity> pontos) {
        if (pontos == null || pontos.isEmpty()) return null;
        Location local = localizacao.getValue();

        ItinerarioEntity escolhido = pontos.get(pontos.size() / 2);
        if (local != null) {
            int menor = Integer.MAX_VALUE;
            for (ItinerarioEntity item : pontos) {
                int distancia = GeoUtil.distanciaMetros(local.getLatitude(), local.getLongitude(),
                        item.latitude, item.longitude);
                if (distancia < menor) {
                    menor = distancia;
                    escolhido = item;
                }
            }
        }
        return repositorioPontos.buscarSincrono(escolhido.pontoId);
    }

    public List<ItinerarioEntity> itinerarioOuVazio() {
        List<ItinerarioEntity> atual = itinerario.getValue();
        return atual == null ? new ArrayList<>() : atual;
    }

    @Override
    protected void onCleared() {
        pausar();
        super.onCleared();
    }
}
