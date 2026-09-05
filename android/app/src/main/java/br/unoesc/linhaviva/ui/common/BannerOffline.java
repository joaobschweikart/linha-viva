package br.unoesc.linhaviva.ui.common;

import android.view.View;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.ViewBannerOfflineBinding;
import br.unoesc.linhaviva.util.Formatador;

/** Faixa ambar que identifica o modo offline e a validade dos dados exibidos (RNF05). */
public class BannerOffline {

    private final ViewBannerOfflineBinding binding;

    public BannerOffline(ViewBannerOfflineBinding binding) {
        this.binding = binding;
    }

    public void atualizar(boolean online, long ultimaSincronizacao) {
        if (online) {
            binding.getRoot().setVisibility(View.GONE);
            return;
        }
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.bannerOfflineTitulo.setText(R.string.estado_offline);
        binding.bannerOfflineDetalhe.setText(ultimaSincronizacao > 0
                ? binding.getRoot().getContext()
                        .getString(R.string.estado_offline_desde, Formatador.dataHoraDe(ultimaSincronizacao))
                : binding.getRoot().getContext().getString(R.string.estado_offline_detalhe));
    }
}
