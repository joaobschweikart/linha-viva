package br.unoesc.linhaviva.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import br.unoesc.linhaviva.R;

public final class Notificacoes {

    public static final String CANAL_APROXIMACAO = "aproximacao_veiculo";
    private static final int ID_APROXIMACAO = 2101;

    private Notificacoes() {
    }

    public static void criarCanais(Context contexto) {
        NotificationManager gerenciador = contexto.getSystemService(NotificationManager.class);
        if (gerenciador == null) return;

        NotificationChannel canal = new NotificationChannel(
                CANAL_APROXIMACAO,
                contexto.getString(R.string.canal_aproximacao),
                NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription(contexto.getString(R.string.canal_aproximacao_descricao));
        gerenciador.createNotificationChannel(canal);
    }

    public static boolean podeNotificar(Context contexto) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void avisarAproximacao(Context contexto, String titulo, String texto, Intent aoTocar) {
        if (!podeNotificar(contexto)) return;

        PendingIntent acao = PendingIntent.getActivity(
                contexto, 0, aoTocar,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder construtor = new NotificationCompat.Builder(contexto, CANAL_APROXIMACAO)
                .setSmallIcon(R.drawable.ic_onibus)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setAutoCancel(true)
                .setContentIntent(acao);

        try {
            NotificationManagerCompat.from(contexto).notify(ID_APROXIMACAO, construtor.build());
        } catch (SecurityException ignorado) {
            // permissao revogada
        }
    }
}
