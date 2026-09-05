package br.unoesc.linhaviva.ui.favoritos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.unoesc.linhaviva.data.local.entity.AvisoEntity;
import br.unoesc.linhaviva.databinding.ItemAvisoBinding;

public class AvisoAdapter extends ListAdapter<AvisoEntity, AvisoAdapter.Suporte> {

    private static final DiffUtil.ItemCallback<AvisoEntity> COMPARADOR =
            new DiffUtil.ItemCallback<AvisoEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull AvisoEntity a, @NonNull AvisoEntity b) {
                    return a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull AvisoEntity a, @NonNull AvisoEntity b) {
                    return a.atualizadoEm == b.atualizadoEm;
                }
            };

    public AvisoAdapter() {
        super(COMPARADOR);
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemAvisoBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        suporte.vincular(getItem(posicao));
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemAvisoBinding binding;

        Suporte(ItemAvisoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(AvisoEntity aviso) {
            binding.tituloAviso.setText(aviso.titulo);
            binding.descricaoAviso.setText(aviso.descricao);
        }
    }
}
