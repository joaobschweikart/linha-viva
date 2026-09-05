package br.unoesc.linhaviva.ui.linha;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.ItemHorarioBinding;
import br.unoesc.linhaviva.util.Formatador;

public class HorarioAdapter extends ListAdapter<String, HorarioAdapter.Suporte> {

    private static final DiffUtil.ItemCallback<String> COMPARADOR =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String a, @NonNull String b) {
                    return a.equals(b);
                }

                @Override
                public boolean areContentsTheSame(@NonNull String a, @NonNull String b) {
                    return a.equals(b);
                }
            };

    private boolean destacarProxima = true;

    public HorarioAdapter() {
        super(COMPARADOR);
    }

    public void definirDestaque(boolean destacar) {
        this.destacarProxima = destacar;
    }

    @NonNull
    @Override
    public Suporte onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Suporte(ItemHorarioBinding.inflate(
                LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Suporte suporte, int posicao) {
        boolean destacado = destacarProxima && ehProxima(posicao);
        suporte.vincular(getItem(posicao), destacado);
    }

    private boolean ehProxima(int posicao) {
        int agora = Formatador.minutosDoDiaAgora();
        if (Formatador.horaParaMinutos(getItem(posicao)) < agora) return false;
        return posicao == 0 || Formatador.horaParaMinutos(getItem(posicao - 1)) < agora;
    }

    static class Suporte extends RecyclerView.ViewHolder {

        private final ItemHorarioBinding binding;

        Suporte(ItemHorarioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(String hora, boolean destacado) {
            binding.hora.setText(hora);
            binding.hora.setBackgroundResource(destacado
                    ? R.drawable.bg_pill_tempo_real : R.drawable.bg_pill_neutro);
            binding.hora.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(),
                    destacado ? R.color.verde_tempo_real : R.color.texto_primario));
            binding.hora.setTypeface(null, destacado
                    ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }
}
