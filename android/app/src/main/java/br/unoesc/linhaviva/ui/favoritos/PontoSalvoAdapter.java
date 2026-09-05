package br.unoesc.linhaviva.ui.favoritos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.ItemPontoSalvoBinding;
import br.unoesc.linhaviva.util.Formatador;

public class PontoSalvoAdapter extends ListAdapter<ItemPontoSalvo, PontoSalvoAdapter.Suporte> {

    public interface AoTocar {
        void executar(ItemPontoSalvo item);
    }

    private static final DiffUtil.ItemCallback<ItemPontoSalvo> COMPARADOR =
            new DiffUtil.ItemCallback<ItemPontoSalvo>() {
                @Override
                public boolean areItemsTheSame(@NonNull ItemPontoSalvo a, @NonNull ItemPontoSalvo b) {
                    return a.ponto.id.equals(b.ponto.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ItemPontoSalvo a, @NonNull ItemPontoSalvo b) {
                    return a.distanciaMetros == b.distanciaMetros;
                }
            };

    private final AoTocar aoTocar;

    public PontoSalvoAdapter(AoTocar aoTocar) {
        super(COMPARADOR);
        this.aoTocar = aoTocar;
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemPontoSalvoBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        suporte.vincular(getItem(posicao), aoTocar);
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemPontoSalvoBinding binding;

        Suporte(ItemPontoSalvoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(ItemPontoSalvo item, AoTocar aoTocar) {
            binding.nomePontoSalvo.setText(item.ponto.nome);
            binding.bairroPontoSalvo.setText(item.ponto.bairro);
            if (item.distanciaMetros < 0) {
                binding.distanciaPontoSalvo.setVisibility(View.GONE);
            } else {
                binding.distanciaPontoSalvo.setVisibility(View.VISIBLE);
                binding.distanciaPontoSalvo.setText(Formatador.distancia(item.distanciaMetros));
            }
            binding.getRoot().setContentDescription(
                    binding.getRoot().getContext().getString(R.string.cd_item_ponto,
                            item.ponto.nome, item.ponto.bairro));
            binding.getRoot().setOnClickListener(v -> aoTocar.executar(item));
        }
    }
}
