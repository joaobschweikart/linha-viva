package br.unoesc.linhaviva.ui.favoritos;

import android.os.Bundle;
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
import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;
import br.unoesc.linhaviva.databinding.FragmentFavoritosBinding;
import br.unoesc.linhaviva.databinding.ItemInformacaoBinding;
import br.unoesc.linhaviva.ui.common.BannerOffline;
import br.unoesc.linhaviva.ui.common.EstadoView;
import br.unoesc.linhaviva.ui.linha.LinhaDetalheActivity;
import br.unoesc.linhaviva.ui.linhas.ItemLinha;
import br.unoesc.linhaviva.ui.linhas.LinhaAdapter;
import br.unoesc.linhaviva.ui.mais.ListaInfoActivity;
import br.unoesc.linhaviva.ui.ponto.PontoActivity;

/** Atalhos do usuario: linhas e pontos favoritos, avisos e informacoes uteis (RF08, RF11, RF12). */
public class FavoritosFragment extends Fragment {

    private FragmentFavoritosBinding binding;
    private FavoritosViewModel viewModel;
    private LinhaAdapter linhaAdapter;
    private PontoSalvoAdapter pontoAdapter;
    private AvisoAdapter avisoAdapter;
    private EstadoView estadoView;
    private BannerOffline bannerOffline;

    private boolean temLinhas;
    private boolean temPontos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(FavoritosViewModel.class);
        estadoView = new EstadoView(binding.estadoFavoritos);
        bannerOffline = new BannerOffline(binding.bannerOfflineFavoritos);

        configurarListas();
        configurarAtalhos();

        binding.atualizar.setColorSchemeResources(R.color.azul_primario);
        binding.atualizar.setOnRefreshListener(() -> viewModel.atualizar());

        observar();
    }

    private void configurarListas() {
        linhaAdapter = new LinhaAdapter(item ->
                LinhaDetalheActivity.abrir(requireContext(), item.linha.id));
        binding.listaLinhasFavoritas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaLinhasFavoritas.setAdapter(linhaAdapter);

        pontoAdapter = new PontoSalvoAdapter(item ->
                PontoActivity.abrir(requireContext(), item.ponto.id));
        binding.listaPontosSalvos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaPontosSalvos.setAdapter(pontoAdapter);

        avisoAdapter = new AvisoAdapter();
        binding.listaAvisos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaAvisos.setAdapter(avisoAdapter);
    }

    private void configurarAtalhos() {
        atalho(binding.atalhoTarifas, R.drawable.ic_tarifa, R.string.info_tarifas,
                InformacaoEntity.CATEGORIA_TARIFA);
        atalho(binding.atalhoTerminais, R.drawable.ic_terminal, R.string.info_terminais,
                InformacaoEntity.CATEGORIA_TERMINAL);
        atalho(binding.atalhoContato, R.drawable.ic_telefone, R.string.info_contato,
                InformacaoEntity.CATEGORIA_CONTATO);
    }

    private void atalho(ItemInformacaoBinding item, int icone, int titulo, String categoria) {
        item.iconeInformacao.setImageResource(icone);
        item.tituloInformacao.setText(titulo);
        item.getRoot().setOnClickListener(v ->
                ListaInfoActivity.abrir(requireContext(), categoria, getString(titulo)));
    }

    private void observar() {
        viewModel.linhasFavoritas().observe(getViewLifecycleOwner(), itens -> {
            temLinhas = !itens.isEmpty();
            linhaAdapter.submitList(itens);
            atualizarSecoes(itens);
        });

        viewModel.pontosSalvos().observe(getViewLifecycleOwner(), itens -> {
            temPontos = !itens.isEmpty();
            pontoAdapter.submitList(itens);
            binding.tituloPontosSalvos.setVisibility(temPontos ? View.VISIBLE : View.GONE);
            binding.listaPontosSalvos.setVisibility(temPontos ? View.VISIBLE : View.GONE);
            atualizarEstadoVazio();
        });

        viewModel.avisos().observe(getViewLifecycleOwner(), avisos -> {
            avisoAdapter.submitList(avisos);
            boolean temAvisos = !avisos.isEmpty();
            binding.tituloAvisos.setVisibility(temAvisos ? View.VISIBLE : View.GONE);
            binding.listaAvisos.setVisibility(temAvisos ? View.VISIBLE : View.GONE);
        });

        viewModel.estado().observe(getViewLifecycleOwner(), estado ->
                binding.atualizar.setRefreshing(estado.estaCarregando()));

        viewModel.online().observe(getViewLifecycleOwner(), online ->
                bannerOffline.atualizar(Boolean.TRUE.equals(online), viewModel.ultimaSincronizacao()));
    }

    private void atualizarSecoes(List<ItemLinha> itens) {
        binding.tituloLinhasFavoritas.setVisibility(itens.isEmpty() ? View.GONE : View.VISIBLE);
        binding.listaLinhasFavoritas.setVisibility(itens.isEmpty() ? View.GONE : View.VISIBLE);
        atualizarEstadoVazio();
    }

    private void atualizarEstadoVazio() {
        if (temLinhas || temPontos) {
            estadoView.esconder();
        } else {
            estadoView.mostrar(R.drawable.ic_estrela_contorno, R.string.vazio_favoritos,
                    getString(R.string.vazio_favoritos_dica));
        }
    }

    @Override
    public void onDestroyView() {
        binding.listaLinhasFavoritas.setAdapter(null);
        binding.listaPontosSalvos.setAdapter(null);
        binding.listaAvisos.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
