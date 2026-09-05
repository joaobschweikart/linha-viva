package br.unoesc.linhaviva.ui.mais;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;
import br.unoesc.linhaviva.databinding.ItemInformacaoBinding;

public class InformacaoAdapter extends ListAdapter<InformacaoEntity, InformacaoAdapter.Suporte> {

    private static final DiffUtil.ItemCallback<InformacaoEntity> COMPARADOR =
            new DiffUtil.ItemCallback<InformacaoEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull InformacaoEntity a, @NonNull InformacaoEntity b) {
                    return a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull InformacaoEntity a, @NonNull InformacaoEntity b) {
                    return a.atualizadoEm == b.atualizadoEm;
                }
            };

    public InformacaoAdapter() {
        super(COMPARADOR);
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemInformacaoBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        suporte.vincular(getItem(posicao));
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemInformacaoBinding binding;

        Suporte(ItemInformacaoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(InformacaoEntity informacao) {
            binding.tituloInformacao.setText(informacao.titulo);
            binding.descricaoInformacao.setText(informacao.descricao);
            binding.descricaoInformacao.setVisibility(View.VISIBLE);
            binding.valorInformacao.setText(informacao.valor);
            binding.valorInformacao.setVisibility(View.VISIBLE);
            binding.setaInformacao.setVisibility(View.GONE);
            binding.iconeInformacao.setImageResource(iconeDa(informacao.categoria));
        }

        private int iconeDa(String categoria) {
            switch (categoria) {
                case InformacaoEntity.CATEGORIA_TERMINAL: return R.drawable.ic_terminal;
                case InformacaoEntity.CATEGORIA_CONTATO: return R.drawable.ic_telefone;
                default: return R.drawable.ic_tarifa;
            }
        }
    }
}
