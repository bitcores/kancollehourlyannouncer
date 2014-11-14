package net.bitcores.kancollehourlyannouncer;

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
	}
}

