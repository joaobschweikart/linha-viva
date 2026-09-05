package br.unoesc.linhaviva.ui.tempo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.remote.dto.VeiculoDto;
import br.unoesc.linhaviva.databinding.ActivityTempoRealBinding;
import br.unoesc.linhaviva.ui.common.BannerOffline;
import br.unoesc.linhaviva.ui.common.MapaHelper;
import br.unoesc.linhaviva.ui.ponto.PontoActivity;
import br.unoesc.linhaviva.util.Formatador;
import br.unoesc.linhaviva.util.GeoUtil;
import br.unoesc.linhaviva.util.Notificacoes;
import br.unoesc.linhaviva.util.Preferencias;

/** Acompanhamento do veiculo no mapa com previsao de chegada ao ponto (RF07, RF09). */
public class TempoRealActivity extends AppCompatActivity {

    private static final String EXTRA_LINHA = "linhaId";
    private static final String EXTRA_SENTIDO = "sentido";

    private ActivityTempoRealBinding binding;
    private TempoRealViewModel viewModel;
    private BannerOffline bannerOffline;
    private Preferencias preferencias;

    private Marker marcadorVeiculo;
    private Marker marcadorPonto;
    private boolean rotaDesenhada;
    private boolean favorita;
    private boolean avisoJaEnviado;

    public static void abrir(Context contexto, String linhaId, String sentido) {
        Intent intent = new Intent(contexto, TempoRealActivity.class);
        intent.putExtra(EXTRA_LINHA, linhaId);
        intent.putExtra(EXTRA_SENTIDO, sentido);
        contexto.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTempoRealBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String linhaId = getIntent().getStringExtra(EXTRA_LINHA);
        if (linhaId == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(TempoRealViewModel.class);
        bannerOffline = new BannerOffline(binding.bannerOfflineTempo);
        preferencias = new Preferencias(this);

        MapaHelper.configurar(binding.mapaTempoReal, 14.0);
        configurarAcoes();

        viewModel.iniciar(linhaId, getIntent().getStringExtra(EXTRA_SENTIDO));
        observar();
    }

    private void configurarAcoes() {
        binding.botaoVoltar.setOnClickListener(v -> finish());
        binding.botaoFavorito.setOnClickListener(v -> {
            viewModel.alternarFavorito();
            Snackbar.make(binding.getRoot(),
                            favorita ? R.string.favorito_removido : R.string.favorito_adicionado,
                            Snackbar.LENGTH_LONG)
                    .setAction(R.string.desfazer, b -> viewModel.alternarFavorito())
                    .show();
        });
        binding.botaoAvisar.setOnClickListener(v -> {
            viewModel.alternarAviso();
            avisoJaEnviado = false;
        });
        binding.botaoVerPonto.setOnClickListener(v -> {
            PontoEntity ponto = viewModel.pontoDeReferencia().getValue();
            if (ponto != null) PontoActivity.abrir(this, ponto.id);
        });
    }

    private void observar() {
        viewModel.linha().observe(this, linha -> {
            if (linha == null) return;
            binding.tituloTempoReal.setText(getString(R.string.titulo_tempo_real,
                    linha.numero, linha.destinoDoSentido(viewModel.sentido())));
            binding.acessibilidadeVeiculo.setText(linha.acessivel
                    ? R.string.veiculo_acessivel : R.string.veiculo_sem_acessibilidade);
        });

        viewModel.itinerario().observe(this, this::desenharRota);
        viewModel.pontoDeReferencia().observe(this, this::desenharPontoDeReferencia);
        viewModel.veiculo().observe(this, this::exibirVeiculo);
        viewModel.paradasRestantes().observe(this, this::avaliarAviso);

        viewModel.atualizadoEm().observe(this, instante -> {
            if (instante == null || instante == 0L) return;
            binding.statusTempoReal.setText(getString(R.string.ao_vivo_em,
                    getString(R.string.ao_vivo), Formatador.tempoRelativo(instante)));
        });

        viewModel.avisoAtivo().observe(this, ativo -> {
            boolean marcado = Boolean.TRUE.equals(ativo);
            binding.botaoAvisar.setText(marcado
                    ? getString(R.string.aviso_ativo)
                    : getResources().getQuantityString(R.plurals.avisar_em_paradas_plural,
                            TempoRealViewModel.PARADAS_PARA_AVISO, TempoRealViewModel.PARADAS_PARA_AVISO));
            binding.botaoAvisar.setIconResource(marcado ? R.drawable.ic_check : R.drawable.ic_sino);
        });

        viewModel.erro().observe(this, mensagem -> {
            if (mensagem == null) return;
            exibirSemTempoReal();
        });

        viewModel.online().observe(this, online -> {
            bannerOffline.atualizar(Boolean.TRUE.equals(online), preferencias.ultimaSincronizacao());
            if (!Boolean.TRUE.equals(online)) exibirSemTempoReal();
        });
    }

    private void desenharRota(List<ItinerarioEntity> pontos) {
        if (rotaDesenhada || pontos == null || pontos.isEmpty()) return;
        rotaDesenhada = true;

        List<GeoPoint> geo = new ArrayList<>();
        for (ItinerarioEntity item : pontos) geo.add(new GeoPoint(item.latitude, item.longitude));

        Polyline tracado = MapaHelper.tracado(this, geo, true);
        binding.mapaTempoReal.getOverlays().add(tracado);

        for (ItinerarioEntity item : pontos) {
            binding.mapaTempoReal.getOverlays().add(MapaHelper.marcador(binding.mapaTempoReal,
                    new GeoPoint(item.latitude, item.longitude),
                    item.terminal ? R.drawable.marcador_terminal : R.drawable.marcador_ponto,
                    item.pontoNome, item.pontoId));
        }
        MapaHelper.enquadrar(binding.mapaTempoReal, geo, 110);
        binding.mapaTempoReal.invalidate();
    }

    private void desenharPontoDeReferencia(PontoEntity ponto) {
        if (ponto == null) return;
        GeoPoint posicao = new GeoPoint(ponto.latitude, ponto.longitude);
        if (marcadorPonto == null) {
            marcadorPonto = MapaHelper.marcador(binding.mapaTempoReal, posicao,
                    R.drawable.marcador_ponto_selecionado, ponto.nome, ponto.id);
            binding.mapaTempoReal.getOverlays().add(marcadorPonto);
        } else {
            marcadorPonto.setPosition(posicao);
        }
        binding.mapaTempoReal.invalidate();
    }

    private void exibirVeiculo(VeiculoDto veiculo) {
        if (veiculo == null) {
            exibirSemTempoReal();
            return;
        }

        binding.pontoAoVivo.setVisibility(View.VISIBLE);
        binding.rotuloChegada.setText(R.string.chega_ao_seu_ponto_em);
        binding.blocoLotacao.setVisibility(View.VISIBLE);
        binding.valorLotacao.setText(Formatador.lotacao(this, veiculo.lotacao));
        binding.prefixoVeiculo.setText(getString(R.string.prefixo_veiculo, veiculo.prefixo));
        binding.rodapeTempoReal.setText(R.string.aviso_tempo_real_estimado);

        GeoPoint posicao = new GeoPoint(veiculo.latitude, veiculo.longitude);
        if (marcadorVeiculo == null) {
            marcadorVeiculo = MapaHelper.marcador(binding.mapaTempoReal, posicao,
                    R.drawable.marcador_veiculo, veiculo.prefixo, veiculo.linhaId);
            binding.mapaTempoReal.getOverlays().add(marcadorVeiculo);
        } else {
            marcadorVeiculo.setPosition(posicao);
        }
        binding.mapaTempoReal.invalidate();

        PontoEntity referencia = viewModel.pontoDeReferencia().getValue();
        if (referencia == null) return;

        int metros = GeoUtil.distanciaMetros(veiculo.latitude, veiculo.longitude,
                referencia.latitude, referencia.longitude);
        binding.distanciaVeiculo.setText(getString(R.string.separador_distancia,
                Formatador.distancia(metros)));

        Integer paradas = viewModel.paradasRestantes().getValue();
        int minutos = paradas != null ? Math.max(1, paradas * 3) : Math.max(1, metros / 400);
        binding.tempoChegada.setText(Formatador.previsao(this, minutos));
    }

    /** Sem dado ao vivo o app degrada para o horario programado, sem exibir erro (RNF12). */
    private void exibirSemTempoReal() {
        binding.pontoAoVivo.setVisibility(View.GONE);
        binding.statusTempoReal.setText(R.string.sem_tempo_real);
        binding.rotuloChegada.setText(R.string.proxima_partida_programada);
        binding.blocoLotacao.setVisibility(View.GONE);
        binding.distanciaVeiculo.setText("");
        binding.prefixoVeiculo.setText(R.string.veiculo_nao_identificado);
        binding.rodapeTempoReal.setText(R.string.aviso_sem_rastreamento);

        PontoEntity referencia = viewModel.pontoDeReferencia().getValue();
        List<ItinerarioEntity> pontos = viewModel.itinerarioOuVazio();
        if (referencia == null || pontos.isEmpty()) {
            binding.tempoChegada.setText(R.string.sem_previsao);
            return;
        }
        binding.tempoChegada.setText(R.string.consulte_horarios);
    }

    private void avaliarAviso(Integer paradas) {
        if (paradas == null || !Boolean.TRUE.equals(viewModel.avisoAtivo().getValue())) return;
        if (avisoJaEnviado || paradas > TempoRealViewModel.PARADAS_PARA_AVISO) return;

        PontoEntity referencia = viewModel.pontoDeReferencia().getValue();
        if (referencia == null || viewModel.linha().getValue() == null) return;

        avisoJaEnviado = true;
        Intent retorno = new Intent(this, TempoRealActivity.class);
        retorno.putExtra(EXTRA_LINHA, viewModel.linha().getValue().id);
        retorno.putExtra(EXTRA_SENTIDO, viewModel.sentido());

        Notificacoes.avisarAproximacao(this,
                getString(R.string.notificacao_aproximacao_titulo, viewModel.linha().getValue().numero),
                getResources().getQuantityString(R.plurals.notificacao_aproximacao_paradas,
                        paradas, paradas, referencia.nome),
                retorno);
        Snackbar.make(binding.getRoot(), R.string.aviso_disparado, Snackbar.LENGTH_LONG).show();
        viewModel.consumirAviso();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapaTempoReal.onResume();
        viewModel.retomar();
    }

    @Override
    protected void onPause() {
        viewModel.pausar();
        binding.mapaTempoReal.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mapaTempoReal.onDetach();
        super.onDestroy();
    }
}
