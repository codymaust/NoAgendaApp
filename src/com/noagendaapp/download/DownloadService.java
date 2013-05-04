package com.noagendaapp.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class DownloadService extends IntentService {

	private int result = Activity.RESULT_CANCELED;
	
	public DownloadService() {
		super("DownloadService");
	}
	
	// Will be call asynchronously by Android
	@Override
	protected void onHandleIntent(Intent intent) {
		Uri data = intent.getData();
		String urlPath = intent.getStringExtra("urlpath");
		String fileName = data.getLastPathSegment();
		File output = new File(getApplicationContext().getExternalCacheDir(), fileName);

		Log.d(getClass().getName(), "urlPath: " + urlPath);
		Log.d(getClass().getName(), "fileName: "+ fileName);
		Log.d(getClass().getName(), "output: " + output.getPath());
		
		if (output.exists()) {
			Log.d(getClass().getName(), output + "exists!");
			output.delete();
		}
		
		InputStream stream = null;
		FileOutputStream fos = null;
				
		try {
			URL url = new URL(urlPath);
			stream = url.openConnection().getInputStream();
			InputStreamReader reader = new InputStreamReader(stream);
			fos = new FileOutputStream(output.getPath());
			
			int next = -1;
			
			while ((next = reader.read()) != -1 ) {
				fos.write(next);
			}
			
			//Successfully finished
			result = Activity.RESULT_OK;
		} catch (Exception e) {
			Log.d(getClass().getName(), "Write Failed");
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					Log.d(getClass().getName(), "Stream Close Failed");
					e.printStackTrace();
				}
			}
			
			if ( fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					Log.d(getClass().getName(), "File Close Failed");
					e.printStackTrace();
				}
			}
		}
		
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			msg.arg1 = result;
			msg.obj = output.getAbsolutePath();
			try {
				messenger.send(msg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message", e1);
			}
		}
	}
}