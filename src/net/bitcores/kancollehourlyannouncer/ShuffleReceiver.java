package net.bitcores.kancollehourlyannouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShuffleReceiver extends BroadcastReceiver {
	SettingsAdapter settingsAdapter;
	
	@Override
	public void onReceive(Context context, Intent intent) {		
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {		
			settingsAdapter.initSettings(context);
		}
		
		if (SettingsAdapter.use_shuffle == 1 && SettingsAdapter.kanmusu_list.size() > 0) {
			settingsAdapter.logEvent("Shuffling Notification Ringtone", 5);
			settingsAdapter.shuffleRingtone(context);			
		}
	}

}
