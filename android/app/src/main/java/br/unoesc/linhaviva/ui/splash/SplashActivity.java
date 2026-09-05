package br.unoesc.linhaviva.ui.splash;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.data.repository.CargaInicial;
import br.unoesc.linhaviva.data.repository.Sincronizador;
import br.unoesc.linhaviva.databinding.ActivitySplashBinding;
import br.unoesc.linhaviva.ui.main.MainActivity;
import br.unoesc.linhaviva.util.AppExecutors;
import br.unoesc.linhaviva.util.MonitorConectividade;

/**
 * Abertura do aplicativo. Restaura os dados locais e tenta atualizar pela rede,
 * mas nunca bloqueia a entrada: sem conexao o app segue com o banco local (RNF05).
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long DURACAO_MINIMA_MS = 1200L;
    private static final long DURACAO_MAXIMA_MS = 3000L;

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long inicio;
    private boolean navegou;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicio = System.currentTimeMillis();
        animarPontos();
        handler.postDelayed(this::avancar, DURACAO_MAXIMA_MS);
        preparar();
    }

    private void preparar() {
        boolean online = MonitorConectividade.temConexao(this);
        binding.splashStatus.setText(online ? R.string.atualizando : R.string.estado_offline);

        AppExecutors.get().io().execute(() -> {
            new CargaInicial(this).executarSeNecessario();
            if (online) new Sincronizador(this).sincronizar(false);

            long decorrido = System.currentTimeMillis() - inicio;
            long espera = Math.max(0, DURACAO_MINIMA_MS - decorrido);
            handler.postDelayed(this::avancar, espera);
        });
    }

    private void avancar() {
        if (navegou || isFinishing()) return;
        navegou = true;
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void animarPontos() {
        View[] pontos = {binding.pontoCarga1, binding.pontoCarga2, binding.pontoCarga3};
        for (int i = 0; i < pontos.length; i++) {
            ObjectAnimator animacao = ObjectAnimator.ofFloat(pontos[i], View.ALPHA, 0.25f, 1f);
            animacao.setDuration(600);
            animacao.setStartDelay(i * 200L);
            animacao.setRepeatCount(ValueAnimator.INFINITE);
            animacao.setRepeatMode(ValueAnimator.REVERSE);
            animacao.start();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
