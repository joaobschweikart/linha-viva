package br.unoesc.linhaviva.ui.linha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.ItemItinerarioBinding;

public class ItinerarioAdapter extends ListAdapter<ItemItinerario, ItinerarioAdapter.Suporte> {

    public interface AoTocar {
        void executar(ItemItinerario item);
    }

    private static final DiffUtil.ItemCallback<ItemItinerario> COMPARADOR =
            new DiffUtil.ItemCallback<ItemItinerario>() {
                @Override
                public boolean areItemsTheSame(@NonNull ItemItinerario a, @NonNull ItemItinerario b) {
                    return a.ponto.ordem == b.ponto.ordem
                            && Objects.equals(a.ponto.pontoId, b.ponto.pontoId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ItemItinerario a, @NonNull ItemItinerario b) {
                    return Objects.equals(a.hora, b.hora) && a.comVeiculo == b.comVeiculo
                            && a.jaPassou == b.jaPassou;
                }
            };

    private final AoTocar aoTocar;

    public ItinerarioAdapter(AoTocar aoTocar) {
        super(COMPARADOR);
        this.aoTocar = aoTocar;
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemItinerarioBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        suporte.vincular(getItem(posicao), aoTocar);
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemItinerarioBinding binding;

        Suporte(ItemItinerarioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(ItemItinerario item, AoTocar aoTocar) {
            binding.nomePonto.setText(item.ponto.pontoNome);
            binding.horaPonto.setText(item.hora);
            binding.marcaVeiculo.setVisibility(item.comVeiculo ? View.VISIBLE : View.GONE);

            binding.tracoSuperior.setVisibility(item.primeiro ? View.INVISIBLE : View.VISIBLE);
            binding.tracoInferior.setVisibility(item.ultimo ? View.INVISIBLE : View.VISIBLE);

            boolean destacado = item.ponto.terminal;
            binding.marcadorPonto.setBackgroundResource(destacado
                    ? R.drawable.bg_ponto_terminal
                    : (item.jaPassou || item.comVeiculo
                            ? R.drawable.bg_ponto_ativo : R.drawable.bg_ponto_inativo));

            binding.nomePonto.setTypeface(null, destacado || item.comVeiculo
                    ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            binding.nomePonto.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(),
                    item.comVeiculo ? R.color.azul_primario : R.color.texto_primario));

            binding.getRoot().setOnClickListener(v -> aoTocar.executar(item));
        }
    }
}
