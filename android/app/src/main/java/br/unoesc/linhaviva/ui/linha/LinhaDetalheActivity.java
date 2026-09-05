package br.unoesc.linhaviva.ui.linha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.HorarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.databinding.ActivityLinhaDetalheBinding;
import br.unoesc.linhaviva.ui.common.BannerOffline;
import br.unoesc.linhaviva.ui.common.EstadoView;
import br.unoesc.linhaviva.ui.common.MapaHelper;
import br.unoesc.linhaviva.ui.ponto.PontoActivity;
import br.unoesc.linhaviva.ui.tempo.TempoRealActivity;

/** Itinerario, horarios programados e visualizacao no mapa de uma linha (RF05). */
public class LinhaDetalheActivity extends AppCompatActivity {

    private static final String EXTRA_LINHA = "linhaId";

    private ActivityLinhaDetalheBinding binding;
    private LinhaDetalheViewModel viewModel;
    private ItinerarioAdapter itinerarioAdapter;
    private HorarioAdapter horarioAdapter;
    private EstadoView estadoHorarios;
    private BannerOffline bannerOffline;

    private boolean favorita;
    private boolean mapaDesenhado;

    public static void abrir(Context contexto, String linhaId) {
        Intent intent = new Intent(contexto, LinhaDetalheActivity.class);
        intent.putExtra(EXTRA_LINHA, linhaId);
        contexto.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLinhaDetalheBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String linhaId = getIntent().getStringExtra(EXTRA_LINHA);
        if (linhaId == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(LinhaDetalheViewModel.class);
        estadoHorarios = new EstadoView(binding.estadoHorarios);
        bannerOffline = new BannerOffline(binding.bannerOfflineDetalhe);

        configurarListas();
        configurarAbas();
        configurarAcoes();
        MapaHelper.configurar(binding.mapaLinha, 13.0);

        viewModel.iniciar(linhaId);
        observar();
    }

    private void configurarListas() {
        itinerarioAdapter = new ItinerarioAdapter(item ->
                PontoActivity.abrir(this, item.ponto.pontoId));
        binding.listaItinerario.setLayoutManager(new LinearLayoutManager(this));
        binding.listaItinerario.setAdapter(itinerarioAdapter);

        horarioAdapter = new HorarioAdapter();
        binding.listaHorarios.setLayoutManager(new GridLayoutManager(this, 4));
        binding.listaHorarios.setAdapter(horarioAdapter);
    }

    private void configurarAbas() {
        binding.abas.addTab(binding.abas.newTab().setText(R.string.aba_itinerario));
        binding.abas.addTab(binding.abas.newTab().setText(R.string.aba_horarios));
        binding.abas.addTab(binding.abas.newTab().setText(R.string.aba_mapa));

        binding.abas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab aba) {
                mostrarPainel(aba.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab aba) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab aba) {
            }
        });
    }

    private void mostrarPainel(int indice) {
        binding.painelItinerario.setVisibility(indice == 0 ? View.VISIBLE : View.GONE);
        binding.painelHorarios.setVisibility(indice == 1 ? View.VISIBLE : View.GONE);
        binding.mapaLinha.setVisibility(indice == 2 ? View.VISIBLE : View.GONE);
        if (indice == 2) desenharMapa();
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

        binding.seletorSentido.addOnButtonCheckedListener((grupo, marcado, ativo) -> {
            if (!ativo) return;
            viewModel.definirSentido(marcado == R.id.botaoSentidoVolta ? "VOLTA" : "IDA");
            mapaDesenhado = false;
        });

        binding.filtrosDia.setOnCheckedStateChangeListener((grupo, marcados) -> {
            if (marcados.isEmpty()) return;
            int id = marcados.get(0);
            if (id == R.id.chipSabado) viewModel.definirDiaTipo("SABADO");
            else if (id == R.id.chipDomingo) viewModel.definirDiaTipo("DOMINGO");
            else viewModel.definirDiaTipo("UTIL");
        });

        binding.botaoTempoReal.setOnClickListener(v -> {
            LinhaEntity linha = viewModel.linha().getValue();
            if (linha != null) {
                TempoRealActivity.abrir(this, linha.id, viewModel.sentidoAtual());
            }
        });
    }

    private void observar() {
        viewModel.linha().observe(this, linha -> {
            if (linha == null) return;
            binding.tituloLinha.setText(getString(R.string.titulo_linha, linha.numero));
            binding.subtituloLinha.setText(linha.nome);
            binding.intervaloLinha.setText(getString(R.string.intervalo_medio, linha.intervaloMin));

            binding.botaoSentidoIda.setText(linha.sentidoIda);
            binding.botaoSentidoVolta.setText(linha.sentidoVolta);
            binding.botaoSentidoVolta.setVisibility(linha.circular ? View.GONE : View.VISIBLE);
            if (binding.seletorSentido.getCheckedButtonId() == View.NO_ID) {
                binding.seletorSentido.check(R.id.botaoSentidoIda);
            }
        });

        viewModel.itinerario().observe(this, itens -> {
            itinerarioAdapter.submitList(itens);
            mapaDesenhado = false;
            if (binding.mapaLinha.getVisibility() == View.VISIBLE) desenharMapa();
        });

        viewModel.horarios().observe(this, this::exibirHorarios);

        viewModel.proximaPartida().observe(this, hora -> binding.proximaSaida.setText(
                hora == null ? getString(R.string.sem_previsao)
                        : getString(R.string.proxima_saida_terminal, hora)));

        viewModel.favorita().observe(this, marcada -> {
            favorita = Boolean.TRUE.equals(marcada);
            binding.botaoFavorito.setImageResource(favorita
                    ? R.drawable.ic_estrela_cheia : R.drawable.ic_estrela_contorno);
            binding.botaoFavorito.setContentDescription(
                    getString(favorita ? R.string.desfavoritar : R.string.favoritar));
        });

        viewModel.online().observe(this, online ->
                bannerOffline.atualizar(Boolean.TRUE.equals(online), viewModel.ultimaSincronizacao()));
    }

    private void exibirHorarios(List<HorarioEntity> horarios) {
        List<String> horas = new ArrayList<>();
        for (HorarioEntity horario : horarios) horas.add(horario.hora);

        boolean hoje = viewModel.diaTipo().getValue() != null
                && viewModel.diaTipo().getValue().equals(
                        br.unoesc.linhaviva.util.Formatador.diaTipoDeHoje());
        horarioAdapter.definirDestaque(hoje);
        horarioAdapter.submitList(horas);

        if (horas.isEmpty()) {
            estadoHorarios.mostrar(R.drawable.ic_relogio, R.string.nao_opera_neste_dia, (String) null);
        } else {
            estadoHorarios.esconder();
        }
    }

    private void desenharMapa() {
        if (mapaDesenhado) return;
        List<ItemItinerario> itens = viewModel.itinerario().getValue();
        if (itens == null || itens.isEmpty()) return;

        binding.mapaLinha.getOverlays().clear();
        List<GeoPoint> pontos = new ArrayList<>();
        for (ItemItinerario item : itens) {
            pontos.add(new GeoPoint(item.ponto.latitude, item.ponto.longitude));
        }

        Polyline tracado = MapaHelper.tracado(this, pontos, true);
        binding.mapaLinha.getOverlays().add(tracado);

        for (ItemItinerario item : itens) {
            GeoPoint posicao = new GeoPoint(item.ponto.latitude, item.ponto.longitude);
            Marker marcador = MapaHelper.marcador(binding.mapaLinha, posicao,
                    item.ponto.terminal ? R.drawable.marcador_terminal : R.drawable.marcador_ponto,
                    item.ponto.pontoNome, item.ponto.pontoId);
            marcador.setOnMarkerClickListener((m, mapa) -> {
                PontoActivity.abrir(this, (String) m.getRelatedObject());
                return true;
            });
            binding.mapaLinha.getOverlays().add(marcador);
        }

        MapaHelper.enquadrar(binding.mapaLinha, pontos, 100);
        binding.mapaLinha.invalidate();
        mapaDesenhado = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapaLinha.onResume();
        viewModel.atualizar();
    }

    @Override
    protected void onPause() {
        binding.mapaLinha.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mapaLinha.onDetach();
        binding.listaItinerario.setAdapter(null);
        binding.listaHorarios.setAdapter(null);
        super.onDestroy();
    }
}
