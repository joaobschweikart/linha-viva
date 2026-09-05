package br.unoesc.linhaviva.ui.ponto;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.util.GeoPoint;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.databinding.ActivityPontoBinding;
import br.unoesc.linhaviva.databinding.DialogoCodigoPontoBinding;
import br.unoesc.linhaviva.ui.common.BannerOffline;
import br.unoesc.linhaviva.ui.common.EstadoView;
import br.unoesc.linhaviva.ui.common.MapaHelper;
import br.unoesc.linhaviva.ui.common.PrevisaoAdapter;
import br.unoesc.linhaviva.ui.linha.LinhaDetalheActivity;
import br.unoesc.linhaviva.util.Formatador;

/** Detalhes de um ponto de parada e proximas partidas (RF06, RF13, RF14). */
public class PontoActivity extends AppCompatActivity {

    private static final String EXTRA_PONTO = "pontoId";

    private ActivityPontoBinding binding;
    private PontoViewModel viewModel;
    private PrevisaoAdapter adaptador;
    private EstadoView estadoView;
    private BannerOffline bannerOffline;
    private LeitorQrCode leitorQrCode;

    private boolean favorito;
    private boolean mapaCentralizado;

    public static void abrir(Context contexto, String pontoId) {
        Intent intent = new Intent(contexto, PontoActivity.class);
        intent.putExtra(EXTRA_PONTO, pontoId);
        contexto.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPontoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String pontoId = resolverPontoId();
        if (pontoId == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(PontoViewModel.class);
        estadoView = new EstadoView(binding.estadoPonto);
        bannerOffline = new BannerOffline(binding.bannerOfflinePonto);
        leitorQrCode = new LeitorQrCode(this, this::abrirPontoLido);

        MapaHelper.configurarEstatico(binding.mapaPonto, 16.5);
        configurarLista();
        configurarAcoes();

        viewModel.iniciar(pontoId);
        observar();
    }

    /** Aceita tanto a navegacao interna quanto o deep link linhaviva://ponto/<id> do QR Code. */
    private String resolverPontoId() {
        String extra = getIntent().getStringExtra(EXTRA_PONTO);
        if (extra != null) return extra;

        Uri dados = getIntent().getData();
        if (dados == null) return null;
        String caminho = dados.getLastPathSegment();
        return caminho != null ? caminho.toUpperCase(java.util.Locale.ROOT) : null;
    }

    private void configurarLista() {
        adaptador = new PrevisaoAdapter(previsao ->
                LinhaDetalheActivity.abrir(this, previsao.linhaId));
        binding.listaPrevisoes.setLayoutManager(new LinearLayoutManager(this));
        binding.listaPrevisoes.setAdapter(adaptador);
    }

    private void configurarAcoes() {
        binding.botaoVoltar.setOnClickListener(v -> finish());
        binding.botaoFavorito.setOnClickListener(v -> {
            viewModel.alternarFavorito();
            Snackbar.make(binding.getRoot(),
                            favorito ? R.string.favorito_removido : R.string.favorito_adicionado,
                            Snackbar.LENGTH_LONG)
                    .setAction(R.string.desfazer, b -> viewModel.alternarFavorito())
                    .show();
        });

        binding.atualizar.setColorSchemeResources(R.color.azul_primario);
        binding.atualizar.setOnRefreshListener(() -> viewModel.atualizar());
        binding.blocoQrCode.setOnClickListener(v -> leitorQrCode.iniciar(this::pedirCodigoManual));
        binding.botaoVerNoMapa.setOnClickListener(v -> abrirNoMapa());
    }

    private void observar() {
        viewModel.ponto().observe(this, this::exibirPonto);

        viewModel.previsoes().observe(this, previsoes -> {
            adaptador.submitList(previsoes);
            if (previsoes.isEmpty()) {
                estadoView.mostrar(R.drawable.ic_relogio, R.string.vazio_previsoes, (String) null);
                binding.pontoIndicadorLive.setVisibility(View.GONE);
                binding.rotuloOrigemDados.setText(R.string.sem_previsao);
            } else {
                estadoView.esconder();
                boolean aoVivo = false;
                long atualizadoEm = 0L;
                for (int i = 0; i < previsoes.size(); i++) {
                    if (previsoes.get(i).tempoReal) aoVivo = true;
                    atualizadoEm = Math.max(atualizadoEm, previsoes.get(i).atualizadoEm);
                }
                binding.pontoIndicadorLive.setVisibility(aoVivo ? View.VISIBLE : View.GONE);
                binding.rotuloOrigemDados.setText(aoVivo
                        ? getString(R.string.origem_tempo_real)
                        : getString(R.string.origem_programado));
                binding.rodapePonto.setText(atualizadoEm > 0
                        ? getString(R.string.atualizado_em, Formatador.dataHoraDe(atualizadoEm))
                        : getString(R.string.aviso_previsao_estimada));
            }
        });

        viewModel.favorito().observe(this, marcado -> {
            favorito = Boolean.TRUE.equals(marcado);
            binding.botaoFavorito.setImageResource(favorito
                    ? R.drawable.ic_estrela_cheia : R.drawable.ic_estrela_contorno);
            binding.botaoFavorito.setContentDescription(
                    getString(favorito ? R.string.desfavoritar : R.string.favoritar));
        });

        viewModel.distancia().observe(this, metros -> {
            if (metros == null) {
                binding.distanciaPonto.setText(R.string.distancia_indisponivel);
            } else if (metros <= Formatador.RAIO_NO_PONTO_M) {
                binding.distanciaPonto.setText(R.string.voce_esta_no_ponto);
            } else {
                binding.distanciaPonto.setText(getString(R.string.distancia_a_pe,
                        Formatador.distancia(metros), Formatador.minutosAPe(metros)));
            }
        });

        viewModel.estado().observe(this, estado ->
                binding.atualizar.setRefreshing(estado.estaCarregando()));

        viewModel.online().observe(this, online ->
                bannerOffline.atualizar(Boolean.TRUE.equals(online), viewModel.ultimaSincronizacao()));
    }

    private void exibirPonto(PontoEntity ponto) {
        if (ponto == null) return;
        binding.tituloPonto.setText(getString(R.string.titulo_ponto,
                ponto.id.startsWith("P") ? ponto.id.substring(1) : ponto.id));
        binding.subtituloPonto.setText(getString(R.string.ponto_endereco_bairro,
                ponto.endereco, ponto.bairro));
        binding.abrigoPonto.setText(ponto.abrigo ? R.string.abrigo_coberto : R.string.sem_abrigo);
        binding.acessibilidadePonto.setText(ponto.acessivel
                ? R.string.ponto_acessivel : R.string.ponto_sem_acessibilidade);

        if (!mapaCentralizado) {
            mapaCentralizado = true;
            GeoPoint posicao = new GeoPoint(ponto.latitude, ponto.longitude);
            binding.mapaPonto.getController().setCenter(posicao);
            binding.mapaPonto.getOverlays().add(MapaHelper.marcador(binding.mapaPonto, posicao,
                    ponto.terminal ? R.drawable.marcador_terminal : R.drawable.marcador_ponto_selecionado,
                    ponto.nome, ponto.id));
            binding.mapaPonto.invalidate();
        }
    }

    private void abrirNoMapa() {
        PontoEntity ponto = viewModel.ponto().getValue();
        if (ponto == null) return;
        Uri geo = Uri.parse(String.format(java.util.Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                ponto.latitude, ponto.longitude, ponto.latitude, ponto.longitude, ponto.nome));
        Intent intent = new Intent(Intent.ACTION_VIEW, geo);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(binding.getRoot(), R.string.sem_app_de_mapas, Snackbar.LENGTH_LONG).show();
        }
    }

    private void pedirCodigoManual() {
        DialogoCodigoPontoBinding dialogo =
                DialogoCodigoPontoBinding.inflate(getLayoutInflater());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.qr_titulo)
                .setView(dialogo.getRoot())
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.abrir_ponto, (d, w) ->
                        abrirPontoLido(textoDe(dialogo.campoCodigo)))
                .show();
    }

    private String textoDe(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void abrirPontoLido(String conteudo) {
        if (conteudo == null || conteudo.isEmpty()) return;
        String id = conteudo.contains("/")
                ? conteudo.substring(conteudo.lastIndexOf('/') + 1).toUpperCase(java.util.Locale.ROOT)
                : conteudo.toUpperCase(java.util.Locale.ROOT);

        viewModel.validarPonto(id, existe -> {
            if (Boolean.TRUE.equals(existe)) {
                abrir(this, id);
            } else {
                Snackbar.make(binding.getRoot(), R.string.qr_codigo_invalido,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapaPonto.onResume();
    }

    @Override
    protected void onPause() {
        binding.mapaPonto.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mapaPonto.onDetach();
        binding.listaPrevisoes.setAdapter(null);
        super.onDestroy();
    }
}
