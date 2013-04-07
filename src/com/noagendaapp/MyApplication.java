package com.noagendaapp;

import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "", // will not be used
				formUri = "http://noagendaapp.com/errorlog.php",
				mode = ReportingInteractionMode.TOAST,
				forceCloseDialogAfterToast = false, // optional, default false
				resToastText = R.string.acra_toast_text) 
public class MyApplication extends Application {
	
	 @Override
	  public void onCreate() {
	      super.onCreate();

	      // The following line triggers the initialization of ACRA
	      ACRA.init(this);
	  }
}