package br.unoesc.linhaviva.ui.linhas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.ItemLinhaBinding;
import br.unoesc.linhaviva.util.Formatador;

public class LinhaAdapter extends ListAdapter<ItemLinha, LinhaAdapter.Suporte> {

    public interface AoTocar {
        void executar(ItemLinha item);
    }

    private static final DiffUtil.ItemCallback<ItemLinha> COMPARADOR =
            new DiffUtil.ItemCallback<ItemLinha>() {
                @Override
                public boolean areItemsTheSame(@NonNull ItemLinha a, @NonNull ItemLinha b) {
                    return a.linha.id.equals(b.linha.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ItemLinha a, @NonNull ItemLinha b) {
                    return a.mesmoConteudo(b);
                }
            };

    private final AoTocar aoTocar;

    public LinhaAdapter(AoTocar aoTocar) {
        super(COMPARADOR);
        this.aoTocar = aoTocar;
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemLinhaBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        suporte.vincular(getItem(posicao), aoTocar);
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemLinhaBinding binding;

        Suporte(ItemLinhaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(ItemLinha item, AoTocar aoTocar) {
            binding.numeroLinha.setText(item.linha.numero);
            binding.nomeLinha.setText(item.linha.nome);
            binding.sentidoLinha.setText(item.linha.descricaoSentido(item.sentido));
            binding.iconeFavorito.setVisibility(item.favorita ? View.VISIBLE : View.GONE);
            binding.iconeAcessivel.setVisibility(item.linha.acessivel ? View.VISIBLE : View.GONE);

            if (item.minutos == null) {
                binding.tempoPrevisao.setText(R.string.sem_previsao);
                binding.tempoPrevisao.setTextColor(cor(R.color.texto_terciario));
                binding.tempoPrevisao.setTextSize(13f);
                binding.indicadorTempoReal.setVisibility(View.GONE);
            } else {
                binding.tempoPrevisao.setText(
                        Formatador.previsao(binding.getRoot().getContext(), item.minutos));
                binding.tempoPrevisao.setTextSize(15f);
                binding.tempoPrevisao.setTextColor(cor(item.tempoReal
                        ? R.color.verde_tempo_real : R.color.texto_secundario));
                binding.indicadorTempoReal.setVisibility(item.tempoReal ? View.VISIBLE : View.GONE);
            }

            binding.getRoot().setContentDescription(
                    binding.getRoot().getContext().getString(R.string.cd_item_linha,
                            item.linha.numero, item.linha.nome,
                            binding.tempoPrevisao.getText().toString()));
            binding.getRoot().setOnClickListener(v -> aoTocar.executar(item));
        }

        private int cor(int recurso) {
            return androidx.core.content.ContextCompat.getColor(binding.getRoot().getContext(), recurso);
        }
    }
}
