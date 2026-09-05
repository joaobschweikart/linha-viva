package br.unoesc.linhaviva.ui.mais;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import br.unoesc.linhaviva.databinding.ActivityListaInfoBinding;

/** Lista de informacoes uteis de uma categoria (tarifas, terminais ou contatos) — RF12. */
public class ListaInfoActivity extends AppCompatActivity {

    private static final String EXTRA_CATEGORIA = "categoria";
    private static final String EXTRA_TITULO = "titulo";

    public static void abrir(Context contexto, String categoria, String titulo) {
        Intent intent = new Intent(contexto, ListaInfoActivity.class);
        intent.putExtra(EXTRA_CATEGORIA, categoria);
        intent.putExtra(EXTRA_TITULO, titulo);
        contexto.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityListaInfoBinding binding = ActivityListaInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);
        if (categoria == null) {
            finish();
            return;
        }

        binding.tituloInfo.setText(getIntent().getStringExtra(EXTRA_TITULO));
        binding.botaoVoltar.setOnClickListener(v -> finish());

        InformacaoAdapter adaptador = new InformacaoAdapter();
        binding.listaInfo.setLayoutManager(new LinearLayoutManager(this));
        binding.listaInfo.setAdapter(adaptador);

        MaisViewModel viewModel = new ViewModelProvider(this).get(MaisViewModel.class);
        viewModel.informacoesDe(categoria).observe(this, adaptador::submitList);
    }
}
