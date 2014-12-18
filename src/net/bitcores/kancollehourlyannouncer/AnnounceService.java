package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.util.Calendar;
import java.util.Random;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;

public class AnnounceService extends Service {
	private static AudioAdapter audioAdapter;
	private static SettingsAdapter settingsAdapter;
	private static AlarmAdapter alarmAdapter;
	private static ServiceReceiver receiver;
	private static PowerManager pm;
	private static WakeLock wl;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent != null) {
			pm = (PowerManager)getSystemService(AnnounceService.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Released");
			wl.acquire(10000);
			settingsAdapter.logEvent("AnnounceService: Wakelock Acquired", 5);
			
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String type = extras.getString("TYPE");
	
				handleEvent(type);
			}
			
			wl.release();
			settingsAdapter.logEvent("AnnounceService: Wakelock Released", 5);
		}
		
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		audioAdapter = new AudioAdapter();
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(AnnounceService.this);
		}
		settingsAdapter.logEvent("AnnounceService: Started", 0);
		
		IntentFilter filter = new IntentFilter();
		receiver = new ServiceReceiver();
		filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		settingsAdapter.logEvent("AnnounceService: Is being shut down", 0);
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}
	
	public class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(WidgetProvider.UPDATE_WIDGET);
			broadcastIntent.putExtra("placeholder", "data");
			sendBroadcast(broadcastIntent);
		}
	}
	
	public void handleEvent(String type) {
		Uri clipUri = null;
		String filePath = "";
		File checkFile = null;
		if (type.equals("secretary")) {
			// Some people may not have all the secretary wife lines so if any file is missing it should only be the 28
			String[] sArray = new String[] { "2.mp3", "3.mp3", "4.mp3", "28.mp3" };
			Random r = new Random();
			int rand = r.nextInt(4);		
			String kanmusu = "";
			if (SettingsAdapter.secretary_widget == 0 || SettingsAdapter.secretary_kanmusu == "") {
				kanmusu = settingsAdapter.getKanmusu();
			} else {
				kanmusu = SettingsAdapter.secretary_kanmusu;
			}
			filePath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/" + sArray[rand];
			checkFile = new File(filePath);
			if (!checkFile.exists()) {
				rand = r.nextInt(3);
				filePath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/" + sArray[rand];
			} 
			
			//	I do not believe I need to test the results of the playAudio in this service because errors are handled in the adapter
			audioAdapter.playAudio(AnnounceService.this, type, 0, clipUri, filePath);
		} else if (type.equals("announce")) {
			alarmAdapter = new AlarmAdapter();
			
			if (SettingsAdapter.enabled == 1) {
				if (SettingsAdapter.kanmusu_use.size() > 0) {
					Calendar cal = Calendar.getInstance();
					int mp3 = cal.get(Calendar.HOUR_OF_DAY) + 30;
					int rand = 0;
					
					// Getting phone call state
					TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
					int callstate = tm.getCallState();
					
					// If phone call state is idle or is not idle and not the mute announcement option, continue to play the announcement
					if (callstate == TelephonyManager.CALL_STATE_IDLE || (callstate != TelephonyManager.CALL_STATE_IDLE && SettingsAdapter.call_action != 2)) {			
											
						for (int c = 0; c < 4; c++) {
							if (SettingsAdapter.kanmusu_use.size() > 1) {
								int max = SettingsAdapter.kanmusu_use.size();
								Random r = new Random();
								rand = r.nextInt(max);
							}						
							filePath = SettingsAdapter.kancolle_dir + "/" + SettingsAdapter.kanmusu_use.get(rand) + "/" + mp3 + ".mp3";
							
							// Check the file exists
							checkFile = new File(filePath);
							if (checkFile.exists()) {
								SettingsAdapter.hourly_kanmusu = SettingsAdapter.kanmusu_use.get(rand);
								break;
							} else {
								// If kanmusu doesn't have the time clip then remove it from the use list
								SettingsAdapter.kanmusu_use.remove(rand);
							}
						}
						
						//	hourly_kanmusu is set if a kanmusu is found in the loop, if not it will be the previous kanmusu if there was one
						//	If there wasnt a previous kanmusu the player will return an error anyway because it checks for the file too
						filePath = SettingsAdapter.kancolle_dir + "/" + SettingsAdapter.hourly_kanmusu + "/" + mp3 + ".mp3";
						
						audioAdapter.playAudio(AnnounceService.this, "announce", 9, clipUri, filePath);
						
						settingsAdapter.saveSettings(AnnounceService.this);				
						
						if (SettingsAdapter.use_shuffle == 1 && SettingsAdapter.kanmusu_list.size() > 0) {
							settingsAdapter.shuffleRingtone(AnnounceService.this);
						}				
					}

					alarmAdapter.setAlarm(AnnounceService.this);
					
					// Tell widgets to update
					settingsAdapter.updateWidgets(AnnounceService.this);
					// Write the log on announcement too
					settingsAdapter.writeLog(AnnounceService.this);
				}
			}		
		} 
	}
}
