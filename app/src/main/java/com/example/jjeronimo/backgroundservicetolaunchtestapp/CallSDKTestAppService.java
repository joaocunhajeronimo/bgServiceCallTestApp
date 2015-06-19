/**
 * Fyber Android SDK
 * <p/>
 * Copyright (c) 2015 Fyber. All rights reserved.
 */
package com.example.jjeronimo.backgroundservicetolaunchtestapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CallSDKTestAppService extends Service {

	private static final String TAG = "CallSDKTestAppService";
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private static final int THRESHOLD = 100;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public String getDeviceInfo() {
		return  "SDK version: " +
				android.os.Build.VERSION.SDK +
				", Device: " +
				android.os.Build.DEVICE +
				", Model: " +
				android.os.Build.MODEL +
				", Product: " +
				android.os.Build.PRODUCT;

	}

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// Normally we would do some work here, like download a file.
			// For our sample, we just sleep for 5 seconds.

			synchronized (this) {
				try {
					callSdkTestApp();
					int startTestAppCount = 0;
					while (true) {
						wait(30000);
						callSdkTestApp();
						startTestAppCount++;
						if (startTestAppCount % THRESHOLD == 0) {
							sendExceptionByEmail(getDeviceInfo() + " has started the app " + startTestAppCount + " times");
							Log.i("", "Email has been sent");
						}
					}
				} catch (Exception e) {
				}
			}
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}
	}

	private void sendExceptionByEmail(String body) {

		try {

			Mail m = new Mail("delta.compadre@gmail.com", "shitonastick");

			String[] toArr = {"joao.jeronimo@fyber.com", "theoklitos.christodoulou@fyber.com"};
			m.setTo(toArr);
			m.setFrom("fyber@fyber.com");
			m.setSubject("[PRECRASH] App start");
			m.setBody(body);
			m.send();


		} catch (Exception e) {
			Log.e("MailApp", "Could not send email", e);
			e.printStackTrace();
		}

	}

	private void callSdkTestApp() {

		Log.d(TAG, "callSdkTestApp");
		Intent intent = new Intent("android.intent.category.LAUNCHER");
//		intent.setClassName("com.your.package", "com.your.package.MainActivity");
		intent.setClassName("com.fyber.precachingtest", "com.fyber.precachingtest.MainActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}
}
