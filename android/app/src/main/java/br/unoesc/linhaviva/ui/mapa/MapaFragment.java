package br.unoesc.linhaviva.ui.mapa;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.databinding.FragmentMapaBinding;
import br.unoesc.linhaviva.ui.common.BannerOffline;
import br.unoesc.linhaviva.ui.common.EstadoView;
import br.unoesc.linhaviva.ui.common.MapaHelper;
import br.unoesc.linhaviva.ui.common.PrevisaoAdapter;
import br.unoesc.linhaviva.ui.linha.LinhaDetalheActivity;
import br.unoesc.linhaviva.ui.ponto.PontoActivity;
import br.unoesc.linhaviva.util.Formatador;
import br.unoesc.linhaviva.util.LocalizacaoLiveData;
import br.unoesc.linhaviva.util.Preferencias;

/** Tela principal: mapa com a posicao do usuario, pontos proximos e previsoes (RF02, RF06). */
public class MapaFragment extends Fragment {

    private FragmentMapaBinding binding;
    private MapaViewModel viewModel;
    private PrevisaoAdapter adaptador;
    private EstadoView estadoView;
    private BannerOffline bannerOffline;
    private Preferencias preferencias;

    private Marker marcadorUsuario;
    private final List<Overlay> sobreposicoesDinamicas = new ArrayList<>();
    private String pontoAtual;
    private boolean favoritoAtual;
    private boolean enquadrouInicial;

    private final ActivityResultLauncher<String[]> pedidoLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                preferencias.marcarPermissaoLocalPedida();
                boolean concedida = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                atualizarAvisoDePermissao();
                if (concedida) {
                    reiniciarObservacaoDeLocalizacao();
                } else {
                    Snackbar.make(binding.getRoot(), R.string.permissao_local_negada,
                            Snackbar.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MapaViewModel.class);
        preferencias = new Preferencias(requireContext());
        estadoView = new EstadoView(binding.estadoMapa);
        bannerOffline = new BannerOffline(binding.bannerOfflineMapa);

        MapaHelper.configurar(binding.mapa, 14.5);
        configurarLista();
        configurarBusca();
        configurarControles();
        atualizarAvisoDePermissao();
        observar();
    }

    private void configurarLista() {
        adaptador = new PrevisaoAdapter(previsao ->
                LinhaDetalheActivity.abrir(requireContext(), previsao.linhaId));
        binding.listaPrevisoesMapa.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaPrevisoesMapa.setAdapter(adaptador);
    }

    private void configurarBusca() {
        binding.campoBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String termo = s.toString();
                viewModel.definirBusca(termo);
                binding.botaoLimparBusca.setVisibility(termo.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        binding.botaoLimparBusca.setOnClickListener(v -> binding.campoBusca.setText(""));
    }

    private void configurarControles() {
        binding.filtrosMapa.setOnCheckedStateChangeListener((grupo, marcados) -> {
            if (marcados.isEmpty()) return;
            int id = marcados.get(0);
            if (id == R.id.chipPontos) viewModel.definirCamada(MapaViewModel.CAMADA_PONTOS);
            else if (id == R.id.chipTerminais) viewModel.definirCamada(MapaViewModel.CAMADA_TERMINAIS);
            else viewModel.definirCamada(MapaViewModel.CAMADA_LINHAS_PROXIMAS);
        });

        binding.botaoMinhaLocalizacao.setOnClickListener(v -> centralizarNoUsuario());
        binding.botaoCamadas.setOnClickListener(v -> alternarCamadaPeloBotao());
        binding.botaoPermitirLocal.setOnClickListener(v -> pedirPermissao());
        binding.botaoFavoritarPonto.setOnClickListener(v -> {
            viewModel.alternarFavoritoDoPonto();
            Snackbar.make(binding.getRoot(),
                    favoritoAtual ? R.string.favorito_removido : R.string.favorito_adicionado,
                    Snackbar.LENGTH_SHORT).show();
        });
        binding.blocoPontoProximo.setOnClickListener(v -> {
            if (pontoAtual != null) PontoActivity.abrir(requireContext(), pontoAtual);
        });
    }

    private void observar() {
        viewModel.dados().observe(getViewLifecycleOwner(), this::desenhar);

        viewModel.previsoes().observe(getViewLifecycleOwner(), previsoes -> {
            adaptador.submitList(previsoes);
            if (previsoes.isEmpty()) {
                estadoView.mostrar(R.drawable.ic_relogio, R.string.vazio_previsoes, (String) null);
            } else {
                estadoView.esconder();
            }
        });

        viewModel.pontoSelecionado().observe(getViewLifecycleOwner(), id -> pontoAtual = id);

        viewModel.pontosFavoritos().observe(getViewLifecycleOwner(), favoritos -> {
            favoritoAtual = pontoAtual != null && favoritos.contains(pontoAtual);
            binding.botaoFavoritarPonto.setImageResource(favoritoAtual
                    ? R.drawable.ic_estrela_cheia : R.drawable.ic_estrela_contorno);
        });

        viewModel.localizacao().observe(getViewLifecycleOwner(), this::posicionarUsuario);

        viewModel.online().observe(getViewLifecycleOwner(), online -> {
            bannerOffline.atualizar(Boolean.TRUE.equals(online), viewModel.ultimaSincronizacao());
            binding.rodapeMapa.setText(Boolean.TRUE.equals(online)
                    ? R.string.aviso_previsao_estimada : R.string.estado_offline_detalhe);
            if (Boolean.TRUE.equals(online)) viewModel.recarregarPrevisoes();
        });
    }

    private void desenhar(DadosMapa dados) {
        if (binding == null) return;

        for (Overlay overlay : sobreposicoesDinamicas) binding.mapa.getOverlays().remove(overlay);
        sobreposicoesDinamicas.clear();

        List<GeoPoint> paraEnquadrar = new ArrayList<>();

        for (List<ItinerarioEntity> tracado : dados.tracados.values()) {
            List<GeoPoint> pontos = new ArrayList<>();
            for (ItinerarioEntity item : tracado) pontos.add(new GeoPoint(item.latitude, item.longitude));
            Polyline linha = MapaHelper.tracado(requireContext(), pontos, false);
            binding.mapa.getOverlays().add(0, linha);
            sobreposicoesDinamicas.add(linha);
        }

        for (PontoEntity ponto : dados.pontosVisiveis) {
            GeoPoint posicao = new GeoPoint(ponto.latitude, ponto.longitude);
            paraEnquadrar.add(posicao);
            int icone = ponto.id.equals(pontoAtual)
                    ? R.drawable.marcador_ponto_selecionado
                    : (ponto.terminal ? R.drawable.marcador_terminal : R.drawable.marcador_ponto);
            Marker marcador = MapaHelper.marcador(binding.mapa, posicao, icone, ponto.nome, ponto.id);
            marcador.setOnMarkerClickListener((m, mapa) -> {
                viewModel.selecionarPonto((String) m.getRelatedObject());
                mapa.getController().animateTo(m.getPosition());
                return true;
            });
            binding.mapa.getOverlays().add(marcador);
            sobreposicoesDinamicas.add(marcador);
        }

        if (dados.maisProximo != null) {
            binding.nomePontoProximo.setText(dados.maisProximo.ponto.nome);
            binding.distanciaPontoProximo.setText(
                    dados.maisProximo.distanciaMetros <= Formatador.RAIO_NO_PONTO_M
                            ? getString(R.string.voce_esta_no_ponto)
                            : getString(R.string.distancia_a_pe,
                                    Formatador.distancia(dados.maisProximo.distanciaMetros),
                                    Formatador.minutosAPe(dados.maisProximo.distanciaMetros)));
            binding.blocoPontoProximo.setContentDescription(getString(R.string.cd_item_ponto,
                    dados.maisProximo.ponto.nome, binding.distanciaPontoProximo.getText().toString()));
        } else {
            binding.nomePontoProximo.setText(R.string.vazio_pontos_proximos);
            binding.distanciaPontoProximo.setText("");
        }

        if (!enquadrouInicial && !paraEnquadrar.isEmpty()) {
            enquadrouInicial = true;
            MapaHelper.enquadrar(binding.mapa, paraEnquadrar, 120);
        }
        binding.mapa.invalidate();
    }

    private void posicionarUsuario(Location local) {
        if (local == null || binding == null) return;
        GeoPoint posicao = new GeoPoint(local.getLatitude(), local.getLongitude());
        if (marcadorUsuario == null) {
            marcadorUsuario = MapaHelper.marcador(binding.mapa, posicao,
                    R.drawable.marcador_usuario, getString(R.string.cd_minha_localizacao), null);
            binding.mapa.getOverlays().add(marcadorUsuario);
        } else {
            marcadorUsuario.setPosition(posicao);
        }
        binding.mapa.invalidate();
    }

    private void centralizarNoUsuario() {
        if (!LocalizacaoLiveData.temPermissao(requireContext())) {
            pedirPermissao();
            return;
        }
        if (!viewModel.provedorAtivo()) {
            Snackbar.make(binding.getRoot(), R.string.gps_desligado, Snackbar.LENGTH_LONG).show();
            return;
        }
        Location local = viewModel.localizacao().getValue();
        if (local == null) {
            Snackbar.make(binding.getRoot(), R.string.obtendo_localizacao, Snackbar.LENGTH_SHORT).show();
            return;
        }
        binding.mapa.getController().animateTo(new GeoPoint(local.getLatitude(), local.getLongitude()));
        binding.mapa.getController().setZoom(16.5);
    }

    private void alternarCamadaPeloBotao() {
        int marcado = binding.filtrosMapa.getCheckedChipId();
        if (marcado == R.id.chipLinhasProximas) binding.chipPontos.setChecked(true);
        else if (marcado == R.id.chipPontos) binding.chipTerminais.setChecked(true);
        else binding.chipLinhasProximas.setChecked(true);
    }

    private void pedirPermissao() {
        pedidoLocalizacao.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void reiniciarObservacaoDeLocalizacao() {
        viewModel.localizacao().removeObservers(getViewLifecycleOwner());
        viewModel.localizacao().observe(getViewLifecycleOwner(), this::posicionarUsuario);
    }

    private void atualizarAvisoDePermissao() {
        boolean temPermissao = LocalizacaoLiveData.temPermissao(requireContext());
        binding.avisoPermissao.setVisibility(temPermissao ? View.GONE : View.VISIBLE);
        if (temPermissao && !viewModel.provedorAtivo()) {
            binding.avisoPermissao.setVisibility(View.VISIBLE);
            binding.avisoPermissaoDetalhe.setText(R.string.gps_desligado);
            binding.botaoPermitirLocal.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapa.onResume();
        atualizarAvisoDePermissao();
    }

    @Override
    public void onPause() {
        binding.mapa.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        binding.mapa.onDetach();
        binding.listaPrevisoesMapa.setAdapter(null);
        sobreposicoesDinamicas.clear();
        marcadorUsuario = null;
        binding = null;
        super.onDestroyView();
    }
}
