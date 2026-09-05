package br.unoesc.linhaviva.ui.linhas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.FragmentLinhasBinding;
import br.unoesc.linhaviva.ui.common.BannerOffline;
import br.unoesc.linhaviva.ui.common.EstadoView;
import br.unoesc.linhaviva.ui.linha.LinhaDetalheActivity;
import br.unoesc.linhaviva.ui.main.MainActivity;
import br.unoesc.linhaviva.util.EstadoCarga;

public class LinhasFragment extends Fragment {

    private FragmentLinhasBinding binding;
    private LinhasViewModel viewModel;
    private LinhaAdapter adaptador;
    private EstadoView estadoView;
    private BannerOffline bannerOffline;
    private boolean online = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLinhasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(LinhasViewModel.class);
        estadoView = new EstadoView(binding.estadoLinhas);
        bannerOffline = new BannerOffline(binding.bannerOfflineLinhas);

        binding.subtituloLinhas.setText(R.string.subtitulo_linhas);
        configurarLista();
        configurarBusca();
        configurarFiltros();

        binding.atualizar.setColorSchemeResources(R.color.azul_primario);
        binding.atualizar.setOnRefreshListener(() -> viewModel.atualizar());
        binding.botaoAvisos.setOnClickListener(v -> abrirFavoritos());

        observar();
    }

    private void configurarLista() {
        adaptador = new LinhaAdapter(item -> LinhaDetalheActivity.abrir(requireContext(), item.linha.id));
        binding.listaLinhas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaLinhas.setAdapter(adaptador);
        binding.listaLinhas.setHasFixedSize(false);
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

    private void configurarFiltros() {
        binding.filtrosLinhas.setOnCheckedStateChangeListener((grupo, marcados) -> {
            if (marcados.isEmpty()) return;
            int id = marcados.get(0);
            if (id == R.id.chipFavoritas) viewModel.definirFiltro(LinhasViewModel.FILTRO_FAVORITAS);
            else if (id == R.id.chipProximas) viewModel.definirFiltro(LinhasViewModel.FILTRO_PROXIMAS);
            else if (id == R.id.chipAcessiveis) viewModel.definirFiltro(LinhasViewModel.FILTRO_ACESSIVEIS);
            else viewModel.definirFiltro(LinhasViewModel.FILTRO_TODAS);
        });
    }

    private void observar() {
        viewModel.itens().observe(getViewLifecycleOwner(), this::exibir);

        viewModel.online().observe(getViewLifecycleOwner(), conectado -> {
            online = Boolean.TRUE.equals(conectado);
            bannerOffline.atualizar(online, viewModel.ultimaSincronizacao());
            if (online && viewModel.ultimaSincronizacao() == 0L) viewModel.atualizar();
        });

        viewModel.estado().observe(getViewLifecycleOwner(), estado -> {
            binding.atualizar.setRefreshing(estado.estaCarregando());
            if (estado.falhou() && adaptador.getItemCount() == 0) {
                estadoView.mostrarComAcao(R.drawable.ic_offline, R.string.erro_rede_titulo,
                        getString(R.string.erro_rede_detalhe), R.string.tentar_novamente,
                        () -> viewModel.atualizar());
            }
        });
    }

    private void exibir(List<ItemLinha> itens) {
        adaptador.submitList(itens);
        if (itens.isEmpty()) {
            if (viewModel.filtroAtual() == LinhasViewModel.FILTRO_FAVORITAS) {
                estadoView.mostrar(R.drawable.ic_estrela_contorno, R.string.vazio_favoritos,
                        getString(R.string.vazio_favoritos_dica));
            } else if (viewModel.filtroAtual() == LinhasViewModel.FILTRO_PROXIMAS) {
                estadoView.mostrar(R.drawable.ic_localizacao, R.string.vazio_linhas_proximas,
                        getString(R.string.vazio_linhas_proximas_dica));
            } else {
                estadoView.mostrar(R.drawable.ic_busca, R.string.vazio_linhas,
                        getString(R.string.vazio_linhas_dica));
            }
        } else {
            estadoView.esconder();
        }
    }

    private void abrirFavoritos() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).findViewById(R.id.navegacaoInferior);
            ((com.google.android.material.bottomnavigation.BottomNavigationView)
                    getActivity().findViewById(R.id.navegacaoInferior))
                    .setSelectedItemId(R.id.nav_favoritos);
        }
    }

    @Override
    public void onDestroyView() {
        binding.listaLinhas.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
