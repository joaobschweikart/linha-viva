package br.unoesc.linhaviva.ui.common;

import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import br.unoesc.linhaviva.databinding.ViewEstadoBinding;

/** Controla os estados de lista vazia, erro e offline de uma tela. */
public class EstadoView {

    private final ViewEstadoBinding binding;

    public EstadoView(ViewEstadoBinding binding) {
        this.binding = binding;
    }

    public void esconder() {
        binding.getRoot().setVisibility(View.GONE);
    }

    public void mostrar(@DrawableRes int icone, @StringRes int titulo, @StringRes int mensagem) {
        binding.estadoIcone.setImageResource(icone);
        binding.estadoTitulo.setText(titulo);
        binding.estadoMensagem.setText(mensagem);
        binding.estadoMensagem.setVisibility(View.VISIBLE);
        binding.estadoAcao.setVisibility(View.GONE);
        binding.getRoot().setVisibility(View.VISIBLE);
    }

    public void mostrar(@DrawableRes int icone, @StringRes int titulo, String mensagem) {
        binding.estadoIcone.setImageResource(icone);
        binding.estadoTitulo.setText(titulo);
        binding.estadoMensagem.setText(mensagem);
        binding.estadoMensagem.setVisibility(mensagem == null ? View.GONE : View.VISIBLE);
        binding.estadoAcao.setVisibility(View.GONE);
        binding.getRoot().setVisibility(View.VISIBLE);
    }

    public void mostrarComAcao(@DrawableRes int icone, @StringRes int titulo, String mensagem,
                               @StringRes int textoAcao, Runnable acao) {
        mostrar(icone, titulo, mensagem);
        binding.estadoAcao.setText(textoAcao);
        binding.estadoAcao.setVisibility(View.VISIBLE);
        binding.estadoAcao.setOnClickListener(v -> acao.run());
    }
}
