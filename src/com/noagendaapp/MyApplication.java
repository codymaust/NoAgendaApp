package com.noagendaapp;

import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;
import static org.acra.ReportField.*;

@ReportsCrashes(formKey = "", // will not be used
				formUri = "http://noagendaapp.com/errorlog.php",
				customReportContent = { APP_VERSION_CODE, APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL, CUSTOM_DATA, STACK_TRACE, LOGCAT },
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