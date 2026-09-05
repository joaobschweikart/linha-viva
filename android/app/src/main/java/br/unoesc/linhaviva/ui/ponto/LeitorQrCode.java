package br.unoesc.linhaviva.ui.ponto;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import br.unoesc.linhaviva.R;

/**
 * Leitura do QR Code afixado na placa do ponto (RF13). Quando a camera nao esta
 * disponivel ou a permissao e negada, cai para a entrada manual do codigo.
 */
class LeitorQrCode {

    interface AoLer {
        void executar(String conteudo);
    }

    private final AppCompatActivity activity;
    private final AoLer aoLer;
    private final ActivityResultLauncher<ScanOptions> leitura;
    private final ActivityResultLauncher<String> permissaoCamera;

    private Runnable alternativa;

    LeitorQrCode(AppCompatActivity activity, AoLer aoLer) {
        this.activity = activity;
        this.aoLer = aoLer;

        leitura = activity.registerForActivityResult(new ScanContract(), resultado -> {
            if (resultado.getContents() != null) aoLer.executar(resultado.getContents());
        });

        permissaoCamera = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), concedida -> {
                    if (concedida) abrirCamera();
                    else if (alternativa != null) alternativa.run();
                });
    }

    void iniciar(Runnable alternativa) {
        this.alternativa = alternativa;

        boolean temCamera = activity.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        if (!temCamera) {
            android.widget.Toast.makeText(activity, R.string.qr_sem_camera,
                    android.widget.Toast.LENGTH_SHORT).show();
            alternativa.run();
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamera();
        } else {
            permissaoCamera.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamera() {
        ScanOptions opcoes = new ScanOptions();
        opcoes.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        opcoes.setPrompt(activity.getString(R.string.qr_instrucao));
        opcoes.setBeepEnabled(false);
        opcoes.setOrientationLocked(false);
        leitura.launch(opcoes);
    }
}
