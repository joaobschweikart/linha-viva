package br.unoesc.linhaviva.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.databinding.ItemPrevisaoBinding;
import br.unoesc.linhaviva.util.Formatador;

public class PrevisaoAdapter extends ListAdapter<PrevisaoEntity, PrevisaoAdapter.Suporte> {

    public interface AoTocar {
        void executar(PrevisaoEntity previsao);
    }

    private static final DiffUtil.ItemCallback<PrevisaoEntity> COMPARADOR =
            new DiffUtil.ItemCallback<PrevisaoEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull PrevisaoEntity a, @NonNull PrevisaoEntity b) {
                    return a.linhaId.equals(b.linhaId) && a.sentido.equals(b.sentido);
                }

                @Override
                public boolean areContentsTheSame(@NonNull PrevisaoEntity a, @NonNull PrevisaoEntity b) {
                    return a.minutos == b.minutos && a.tempoReal == b.tempoReal
                            && a.atualizadoEm == b.atualizadoEm;
                }
            };

    private final AoTocar aoTocar;

    public PrevisaoAdapter(AoTocar aoTocar) {
        super(COMPARADOR);
        this.aoTocar = aoTocar;
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemPrevisaoBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        suporte.vincular(getItem(posicao), aoTocar);
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemPrevisaoBinding binding;

        Suporte(ItemPrevisaoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(PrevisaoEntity previsao, AoTocar aoTocar) {
            binding.numeroLinha.setText(previsao.linhaNumero);
            binding.nomeLinha.setText(previsao.linhaNome);
            String origem = previsao.tempoReal && previsao.prefixoVeiculo != null
                    ? binding.getRoot().getContext().getString(
                            R.string.previsao_veiculo, previsao.prefixoVeiculo)
                    : binding.getRoot().getContext().getString(
                            R.string.previsao_programada, previsao.horaProgramada);
            binding.detalhePrevisao.setText(previsao.sentidoDescricao == null
                    ? origem
                    : binding.getRoot().getContext().getString(
                            R.string.previsao_sentido_origem, previsao.sentidoDescricao, origem));

            binding.tempoPrevisao.setText(
                    Formatador.previsao(binding.getRoot().getContext(), previsao.minutos));
            binding.tempoPrevisao.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(),
                    previsao.tempoReal ? R.color.verde_tempo_real : R.color.texto_secundario));
            binding.indicadorTempoReal.setVisibility(previsao.tempoReal ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> aoTocar.executar(previsao));
        }
    }
}
