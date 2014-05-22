package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Random;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ServiceTask extends Service {
	AlarmAdapter alarmAdapter;
	SettingsAdapter settingsAdapter;
	PowerManager pm;
	WakeLock wl;
	
	private static MediaPlayer mp;
		
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
		
	@Override
	public void onCreate() {	
		super.onCreate();
		
		// Acquire wake lock with 10 second timeout to prevent the device from going to sleep
		// before the service starts playing the announcement
		pm = (PowerManager)getSystemService(ServiceTask.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Released");
		wl.acquire(10000);

		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(ServiceTask.this);
		}
		
		if (SettingsAdapter.enabled == 1) {
			if (SettingsAdapter.kanmusu_use.size() > 0) {
				Calendar cal = Calendar.getInstance();
				int mp3 = cal.get(Calendar.HOUR_OF_DAY) + 30;
				int chour = cal.get(Calendar.HOUR_OF_DAY);
				int start = SettingsAdapter.quiet_start;
				int end = SettingsAdapter.quiet_end;			
				int quiet = 0;
				int rand = 0;
				boolean checkLine = false;
				int checkBreak = 0;
				
				// Getting phone call state
				TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
				int callstate = tm.getCallState();
				
				// If phone call state is idle or is not idle and not the mute announcement option, continue to play the announcement
				if (callstate == TelephonyManager.CALL_STATE_IDLE || (callstate != TelephonyManager.CALL_STATE_IDLE && SettingsAdapter.call_action != 2)) {			
					if (callstate != TelephonyManager.CALL_STATE_IDLE && SettingsAdapter.call_action == 1) {
						quiet = 1;
					}
					else if (SettingsAdapter.use_quiet == 1) {
						if (start > end) {
							if (chour >= start) {
								quiet = 1;
							} else {
								end += 24;
								chour += 24;
								if (chour >= start && chour < end) {
									quiet = 1;
								}
							}
							
						} else {
							if ((chour >= start && chour < end) || start == end) {
								quiet = 1;
							}
						}
					}
					
					while (!checkLine)
					{
						if (SettingsAdapter.kanmusu_use.size() > 1) {
							int max = SettingsAdapter.kanmusu_use.size();
							Random r = new Random();
							rand = r.nextInt(max);
						}						
						String filepath = SettingsAdapter.kancolle_dir + "/" + SettingsAdapter.kanmusu_use.get(rand) + "/" + mp3 + ".mp3";
						
						// Check the file exists
						File checkfile = new File(filepath);
						if (checkfile.exists()) {
							checkLine = true;
							SettingsAdapter.hourly_kanmusu = SettingsAdapter.kanmusu_use.get(rand);
							
							try {
								// Set up mediaplayer
								mp = new MediaPlayer();
								mp.setOnCompletionListener(new OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {
										Log.i("kancolle announcer", "ServiceTask MediaPlayer released");
										mp.release();
									}
								});
								FileInputStream fis = new FileInputStream(filepath);			
								mp.setDataSource(fis.getFD());
								fis.close();
								// Setting the volume type and quiet volume
								int stream = AudioManager.STREAM_NOTIFICATION;
								if (SettingsAdapter.use_volume == 1) {
									stream = AudioManager.STREAM_MUSIC;
								} else if (SettingsAdapter.use_volume == 2) {
									stream = AudioManager.STREAM_RING;
								} else if (SettingsAdapter.use_volume == 3) {
									stream = AudioManager.STREAM_ALARM;
								} 
								mp.setAudioStreamType(stream);	
								if (quiet == 1) {
									float volume = (float) (1 - (Math.log(100 - SettingsAdapter.quiet_volume) / Math.log(100)));
									mp.setVolume(volume, volume);
								}
								mp.prepare();
								
								Log.i("kancolle announcer", "ServiceTask Playing file: " + filepath);
								mp.start();						
							} catch (Exception e) {
								Log.e("kancolle announcer", "ServiceTask Error playing file: " + filepath);
							}
						} else {
							// If kanmusu doesn't have the time clip then remove it from the use list
							SettingsAdapter.kanmusu_use.remove(rand);
							
							// Break out of the loop checking for files if more than four failures
							checkBreak++;
							if (checkBreak > 4) {
								break;
							}
						}				
					}
				}
				
				settingsAdapter.saveSettings(ServiceTask.this, 0);
				
				if (SettingsAdapter.use_shuffle == 1 && SettingsAdapter.kanmusu_list.size() > 0) {
					settingsAdapter.shuffleRingtone(ServiceTask.this);
				}				
			}

			alarmAdapter.setAlarm(ServiceTask.this);
			
			// Tell widgets to update
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(WidgetShare.UPDATE_WIDGET);
			sendBroadcast(broadcastIntent);
		}
		
		// Release the wake lock and stop this service
		wl.release();
		stopSelf();
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
