package br.ufg.inf.es.gcm;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

//Final

/* Esta classe � respons�vel por Registrar o dispositivo no servi�o GCM do Google, obtendo assim o GCM regID. Al�m de compartilhar este GCM regID com o servidor de aplica��o */
public class RegistroActivity extends Activity {

	Button btnRegistroGCM;
	Button btnCompartilharApp;
	GoogleCloudMessaging gcm;
	Context context;
	String regId;
	CompartilharServidorExterno appUtil;
	AsyncTask<Void, Void, String> shareRegidTask;

	private static final String REG_ID = "regId";
	private static final String APP_VERSION = "appVersion";
	static final String TAG = "RegistroActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);

		context = getApplicationContext();

		/*
		 * No clique do bot�o Registro, vamos utilizar o Google ID Projeto de
		 * registrar com o servidor de mensagens em nuvem do Google e obter a
		 * GCM RegID.
		 */
		btnRegistroGCM = (Button) findViewById(R.id.btnRegistroGCM);
		btnRegistroGCM.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(regId)) {
					regId = registrarGCM();
					Log.d("RegistroActivity", "GCM RegId: " + regId);
				} else {
					Toast.makeText(getApplicationContext(),
							"J� est� registrado com o Servidor GCM!",
							Toast.LENGTH_LONG).show();
				}

			}
		});

		/*
		 * O objetivo deste bot�o � compartilhar este RegID com a nossa
		 * aplica��o web personalizada. Ao clicar, a URL da aplica��o web ser�
		 * invocada
		 */
		btnCompartilharApp = (Button) findViewById(R.id.btnCompartilharApp);
		btnCompartilharApp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(regId)) {
					Toast.makeText(getApplicationContext(),
							"RegId est� vazio!", Toast.LENGTH_LONG).show();
				} else {
					Log.d("RegistroActivity",
							"onClick do Compartilhamento: Antes de iniciar a main activity.");
					appUtil = new CompartilharServidorExterno();

					final Context context = getBaseContext();
					shareRegidTask = new AsyncTask<Void, Void, String>() {
						@Override
						protected String doInBackground(Void... params) {
							String result = appUtil
									.compartilhaRegIdComServidorApp(context,
											regId);
							return result;
						}

						@Override
						protected void onPostExecute(String result) {
							shareRegidTask = null;
							Toast.makeText(getApplicationContext(), result,
									Toast.LENGTH_LONG).show();
						}

					};
					shareRegidTask.execute(null, null, null);

					Log.d("RegistroActivity",
							"onClick do Compartilhamento: Depois de finalizar.");
				}

			}
		});
	}

	public String registrarGCM() {
		gcm = GoogleCloudMessaging.getInstance(this);
		regId = getIdRegistro(context);

		if (TextUtils.isEmpty(regId)) {
			registroEmSegundoPlano();

			Log.d("RegistroActivity",
					"registrarGCM - Registrado com sucesso no Servidor GCM - regId: "
							+ regId);
		} else {
			Toast.makeText(getApplicationContext(),
					"RegId j� dispon�vel. RegId: " + regId, Toast.LENGTH_LONG)
					.show();
		}
		return regId;
	}

	// M�todo respons�vel por buscar o registro do aparelho, caso exista.
	// Verifica tamb�m se a vers�o do aplicativo mudou, pois caso mude deve
	// gerar um novo regID.
	private String getIdRegistro(Context context) {
		final SharedPreferences prefs = getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		String registrationId = prefs.getString(REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registro n�o encontrado.");
			return "";
		}
		int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getVersaoApp(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "Vers�o do App modificada.");
			return "";
		}
		return registrationId;
	}

	private static int getVersaoApp(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			Log.d("RegistroActivity", "Erro inesperado!" + e);
			throw new RuntimeException(e);
		}
	}

	private void registroEmSegundoPlano() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regId = gcm.register(Config.GOOGLE_PROJECT_ID);
					Log.d("RegistroActivity",
							"registroEmSegundoPlano - regId: " + regId);
					msg = "Dispositivo registrado, ID de registro =" + regId;

					storeRegistrationId(context, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					Log.d("RegistroActivity", "Error: " + msg);
				}
				Log.d("RegistroActivity", "AsyncTask completed: " + msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Toast.makeText(getApplicationContext(),
						"Registrado com Servidor GCM." + msg, Toast.LENGTH_LONG)
						.show();
			}
		}.execute(null, null, null);
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		int appVersion = getVersaoApp(context);
		Log.i(TAG, "Salvando regId no aplicativo vers�o " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(REG_ID, regId);
		editor.putInt(APP_VERSION, appVersion);
		editor.commit();
	}
}