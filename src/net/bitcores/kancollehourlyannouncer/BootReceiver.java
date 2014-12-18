package net.bitcores.kancollehourlyannouncer;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	AlarmAdapter alarmAdapter;
	SettingsAdapter settingsAdapter;
	
	@Override
	public void onReceive(Context context, Intent intent) {	
		Log.i("kancolle announcer", "Received BootIntent");
		
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		
		// Initialize settings and set the alarm if it is enabled		
		if (!SettingsAdapter.init) {		
			settingsAdapter.initSettings(context);
		}
		
		if (SettingsAdapter.enabled == 1) {
			alarmAdapter.setAlarm(context);	
		}
			
		context.startService(new Intent(context, AnnounceService.class));	
		
		// Play the kanmusu idle line on device boot
		if (SettingsAdapter.boot_idle == 1) {
			AudioAdapter audioAdapter = new AudioAdapter();
			String kanmusu = "";
			if (SettingsAdapter.secretary_idle == 0 || SettingsAdapter.secretary_kanmusu == "") {
				kanmusu = settingsAdapter.getKanmusu();
			} else {
				kanmusu = SettingsAdapter.secretary_kanmusu;
			}
			
			String filePath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/29.mp3";
			File checkFile = new File(filePath);
			if (checkFile.exists()) {
				audioAdapter.playAudio(context, "boot", 0, null, filePath);
			}
		}
	}
}

