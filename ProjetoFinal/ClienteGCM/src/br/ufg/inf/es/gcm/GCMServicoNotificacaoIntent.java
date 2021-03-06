package br.ufg.inf.es.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
//Final
public class GCMServicoNotificacaoIntent extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GCMServicoNotificacaoIntent() {
		super("GcmServicoIntent");
	}

	public static final String TAG = "GCMServicoNotificacaoIntent";

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Erro ao enviar: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Mensagens deletadas no servidor: "
						+ extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

//				for (int i = 0; i < 3; i++) {
//					Log.i(TAG,
//							"Funcionando... " + (i + 1) + "/5 @ "
//									+ SystemClock.elapsedRealtime());
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//					}
//
//				}
//				Log.i(TAG, "Trabalho completo @ " + SystemClock.elapsedRealtime());

				sendNotification("Mensagem recebido do Servidor GCM Google : "
						+ extras.get(Config.MESSAGE_KEY));
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		GCMReceptorBroadcast.completeWakefulIntent(intent);
	}

	//Método responsável por receber a mensagem e enviar para a aplicação
	private void sendNotification(String msg) {
		Log.d(TAG, "Preparando para enviar notificação...: " + msg);
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

//		Intent openNewActivityWindow = new Intent(this, MostraMensagemActivity.class);
	//	openNewActivityWindow.putExtra("mensagem_recebida", msg);
		Intent a = new Intent("MostraMensagem").putExtra("msg", msg);
		Log.i(TAG, "Mensagem:" + a.getExtras().toString());
		
		PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0,
                a, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.notificacao)
				.setContentTitle("Notificação GCM")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg)
				.setAutoCancel(true);
		
		mBuilder.setContentIntent(pendingIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		Log.d(TAG, "Notificação enviada com sucesso.");
	}
}