package br.unoesc.linhaviva.ui.mais;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import br.unoesc.linhaviva.BuildConfig;
import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;
import br.unoesc.linhaviva.databinding.DialogoServidorBinding;
import br.unoesc.linhaviva.databinding.FragmentMaisBinding;
import br.unoesc.linhaviva.databinding.ItemInformacaoBinding;
import br.unoesc.linhaviva.util.EstadoCarga;
import br.unoesc.linhaviva.util.Formatador;

/** Informacoes uteis, configuracao do servidor de dados e estado da sincronizacao. */
public class MaisFragment extends Fragment {

    private FragmentMaisBinding binding;
    private MaisViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMaisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MaisViewModel.class);

        atalho(binding.itemTarifas, R.drawable.ic_tarifa, R.string.info_tarifas,
                InformacaoEntity.CATEGORIA_TARIFA);
        atalho(binding.itemTerminais, R.drawable.ic_terminal, R.string.info_terminais,
                InformacaoEntity.CATEGORIA_TERMINAL);
        atalho(binding.itemContato, R.drawable.ic_telefone, R.string.info_contato,
                InformacaoEntity.CATEGORIA_CONTATO);

        binding.blocoServidor.setOnClickListener(v -> abrirDialogoDoServidor());
        binding.blocoSincronizar.setOnClickListener(v -> viewModel.sincronizar(true));
        binding.valorVersao.setText(getString(R.string.versao_completa, BuildConfig.VERSION_NAME));

        observar();
    }

    private void atalho(ItemInformacaoBinding item, int icone, int titulo, String categoria) {
        item.iconeInformacao.setImageResource(icone);
        item.tituloInformacao.setText(titulo);
        item.getRoot().setOnClickListener(v ->
                ListaInfoActivity.abrir(requireContext(), categoria, getString(titulo)));
    }

    private void observar() {
        viewModel.urlApi().observe(getViewLifecycleOwner(), binding.valorServidor::setText);

        viewModel.ultimaSincronizacao().observe(getViewLifecycleOwner(), instante ->
                binding.valorUltimaSincronizacao.setText(instante == null || instante == 0L
                        ? getString(R.string.nunca_sincronizado)
                        : getString(R.string.ultima_sincronizacao, Formatador.dataHoraDe(instante))));

        viewModel.estado().observe(getViewLifecycleOwner(), estado -> {
            binding.progressoSincronizacao.setVisibility(
                    estado.estaCarregando() ? View.VISIBLE : View.GONE);
            binding.iconeSincronizar.setVisibility(estado.estaCarregando() ? View.INVISIBLE : View.VISIBLE);

            if (estado.fase == EstadoCarga.Fase.PRONTO) {
                Snackbar.make(binding.getRoot(), R.string.sincronizacao_ok, Snackbar.LENGTH_SHORT).show();
            } else if (estado.falhou() && estado.mensagem != null) {
                Snackbar.make(binding.getRoot(), estado.mensagem, Snackbar.LENGTH_LONG)
                        .setAction(R.string.tentar_novamente, v -> viewModel.sincronizar(true))
                        .show();
            }
        });

        viewModel.online().observe(getViewLifecycleOwner(), online -> {
            boolean conectado = Boolean.TRUE.equals(online);
            binding.valorConexao.setText(conectado ? R.string.app_online : R.string.app_offline);
            binding.iconeConexao.setImageResource(conectado
                    ? R.drawable.ic_check : R.drawable.ic_offline);
            binding.iconeConexao.setColorFilter(ContextCompat.getColor(requireContext(),
                    conectado ? R.color.verde_tempo_real : R.color.ambar_aviso));
        });
    }

    private void abrirDialogoDoServidor() {
        DialogoServidorBinding dialogo = DialogoServidorBinding.inflate(getLayoutInflater());
        dialogo.campoUrl.setText(viewModel.urlApi().getValue());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.servidor_dados)
                .setView(dialogo.getRoot())
                .setNeutralButton(R.string.restaurar_url_padrao, (d, w) -> viewModel.restaurarUrlPadrao())
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.salvar, (d, w) -> {
                    String url = dialogo.campoUrl.getText() == null
                            ? "" : dialogo.campoUrl.getText().toString();
                    if (!viewModel.definirUrlApi(url)) {
                        Snackbar.make(binding.getRoot(), R.string.url_invalida,
                                Snackbar.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
