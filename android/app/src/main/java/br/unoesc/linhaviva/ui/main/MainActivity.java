package br.unoesc.linhaviva.ui.main;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.databinding.ActivityMainBinding;
import br.unoesc.linhaviva.ui.favoritos.FavoritosFragment;
import br.unoesc.linhaviva.ui.linhas.LinhasFragment;
import br.unoesc.linhaviva.ui.mais.MaisFragment;
import br.unoesc.linhaviva.ui.mapa.MapaFragment;

/**
 * Host da navegacao inferior fixa com os quatro destinos do protótipo.
 * Os fragmentos sao mantidos em memoria e alternados por show/hide para
 * preservar o estado de rolagem e do mapa entre as abas.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ABA = "aba";
    public static final int ABA_MAPA = 0;
    public static final int ABA_LINHAS = 1;
    public static final int ABA_FAVORITOS = 2;
    public static final int ABA_MAIS = 3;

    private static final String[] TAGS = {"mapa", "linhas", "favoritos", "mais"};

    private ActivityMainBinding binding;
    private int abaAtual = -1;

    private final ActivityResultLauncher<String> pedidoNotificacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navegacaoInferior.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_mapa) selecionar(ABA_MAPA);
            else if (id == R.id.nav_linhas) selecionar(ABA_LINHAS);
            else if (id == R.id.nav_favoritos) selecionar(ABA_FAVORITOS);
            else if (id == R.id.nav_mais) selecionar(ABA_MAIS);
            return true;
        });

        int inicial = savedInstanceState != null
                ? savedInstanceState.getInt(EXTRA_ABA, ABA_MAPA)
                : getIntent().getIntExtra(EXTRA_ABA, ABA_MAPA);
        binding.navegacaoInferior.setSelectedItemId(idDoMenu(inicial));

        pedirPermissaoDeNotificacao();
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra(EXTRA_ABA)) {
            binding.navegacaoInferior.setSelectedItemId(idDoMenu(intent.getIntExtra(EXTRA_ABA, ABA_MAPA)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ABA, abaAtual);
    }

    private int idDoMenu(int aba) {
        switch (aba) {
            case ABA_LINHAS: return R.id.nav_linhas;
            case ABA_FAVORITOS: return R.id.nav_favoritos;
            case ABA_MAIS: return R.id.nav_mais;
            default: return R.id.nav_mapa;
        }
    }

    private void selecionar(int aba) {
        if (aba == abaAtual) return;
        abaAtual = aba;

        FragmentTransaction transacao = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < TAGS.length; i++) {
            Fragment existente = getSupportFragmentManager().findFragmentByTag(TAGS[i]);
            if (i == aba) {
                if (existente == null) {
                    transacao.add(R.id.conteudo, criarFragmento(i), TAGS[i]);
                } else {
                    transacao.show(existente);
                }
            } else if (existente != null) {
                transacao.hide(existente);
            }
        }
        transacao.commit();
    }

    private Fragment criarFragmento(int aba) {
        switch (aba) {
            case ABA_LINHAS: return new LinhasFragment();
            case ABA_FAVORITOS: return new FavoritosFragment();
            case ABA_MAIS: return new MaisFragment();
            default: return new MapaFragment();
        }
    }

    private void pedirPermissaoDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedidoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
