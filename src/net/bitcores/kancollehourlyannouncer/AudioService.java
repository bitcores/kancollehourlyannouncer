package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Random;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AudioService extends Service {
	private SettingsAdapter settingsAdapter;
	private AlarmAdapter alarmAdapter;
	private ViewerFragment viewerFragment;	
	private ServiceReceiver receiver;
	private PowerManager pm;
	private WakeLock wl;
	
	private static MediaPlayer mp;
	
	private static String type;
	private static String file;
	private static Integer interrupt;
	private static Integer interruptLevel = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// This is set up so that it should be able to handle all audio events if called correctly
		if (intent != null) {
			pm = (PowerManager)getSystemService(AudioService.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Released");
			wl.acquire(10000);
			
			Bundle extras = intent.getExtras();
			if (extras != null) {
				type = extras.getString("TYPE");
				file = extras.getString("FILE");
				interrupt = extras.getInt("INTERRUPT");
	
				playEvent();
			}
		}
		
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		settingsAdapter = new SettingsAdapter();
		mp = new MediaPlayer();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(AudioService.this);
		}
		
		IntentFilter filter = new IntentFilter();
		receiver = new ServiceReceiver();
		filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}
	
	public class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(WidgetShare.UPDATE_WIDGET);
			broadcastIntent.putExtra("placeholder", "data");
			sendBroadcast(broadcastIntent);
		}
	}
	
	public void playEvent() {
		if (interrupt > interruptLevel) {
			mp.reset();
			interruptLevel = 0;
		}
		if (!mp.isPlaying()) {
			int quiet = 0;
			String filepath = "";
			File checkfile = null;
			Uri notifysound = null;
			Boolean checked = false;
			
			if (type.equals("secretary")) {
				// Some people may not have all the secretary wife lines so if any file is missing it should only be the 28
				String[] sArray = new String[] { "2.mp3", "3.mp3", "4.mp3", "28.mp3" };
				Random r = new Random();
				int rand = r.nextInt(4);		
				String kanmusu = settingsAdapter.getKanmusu();
				filepath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/" + sArray[rand];
				checkfile = new File(filepath);
				if (!checkfile.exists()) {
					rand = r.nextInt(3);
					filepath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/" + sArray[rand];
				} else {
					checked = true;
				}
			} else if (type.equals("file")) {
				viewerFragment = new ViewerFragment();
				filepath = file;
			} else if (type.equals("quiet")) {
				notifysound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			} else if (type.equals("announce")) {
				alarmAdapter = new AlarmAdapter();
				
				if (SettingsAdapter.enabled == 1) {
					if (SettingsAdapter.kanmusu_use.size() > 0) {
						Calendar cal = Calendar.getInstance();
						int mp3 = cal.get(Calendar.HOUR_OF_DAY) + 30;
						int chour = cal.get(Calendar.HOUR_OF_DAY);
						int start = SettingsAdapter.quiet_start;
						int end = SettingsAdapter.quiet_end;			
						
						int rand = 0;
						
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
							
							for (int c = 0; c < 4; c++) {
								if (SettingsAdapter.kanmusu_use.size() > 1) {
									int max = SettingsAdapter.kanmusu_use.size();
									Random r = new Random();
									rand = r.nextInt(max);
								}						
								filepath = SettingsAdapter.kancolle_dir + "/" + SettingsAdapter.kanmusu_use.get(rand) + "/" + mp3 + ".mp3";
								
								// Check the file exists
								checkfile = new File(filepath);
								if (checkfile.exists()) {
									SettingsAdapter.hourly_kanmusu = SettingsAdapter.kanmusu_use.get(rand);
									checked = true;
									break;
								} else {
									// If kanmusu doesn't have the time clip then remove it from the use list
									SettingsAdapter.kanmusu_use.remove(rand);
								}
							}
							settingsAdapter.saveSettings(AudioService.this, 0);
							
							if (SettingsAdapter.use_shuffle == 1 && SettingsAdapter.kanmusu_list.size() > 0) {
								settingsAdapter.shuffleRingtone(AudioService.this);
							}				
						}

						alarmAdapter.setAlarm(AudioService.this);
						
						// Tell widgets to update
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(WidgetShare.UPDATE_WIDGET);
						sendBroadcast(broadcastIntent);
					}
				} else {
					wl.release();
					return;
				}
			} else {
				wl.release();
				return;
			}
			
			if (!filepath.equals("") && !checked) {
				checkfile = new File(filepath);
				if (checkfile.exists()) {
					checked = true;
				}
			}
			
			if (checked || notifysound != null) {
				try {					
					mp.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							// Because we want the service to stay alive and be ready to play other clips we reset the player instead of releasing it
							Log.i("kancolle announcer", "AudioService MediaPlayer reset");
							mp.reset();
							interruptLevel = 0;
						}
					});
					
					if (notifysound != null) {
						mp.setDataSource(AudioService.this, notifysound);
					} else {
						FileInputStream fis = new FileInputStream(filepath);			
						mp.setDataSource(fis.getFD());
						fis.close();
					}
					
					int stream = AudioManager.STREAM_NOTIFICATION;
					if (SettingsAdapter.use_volume == 1) {
						stream = AudioManager.STREAM_MUSIC;
					} else if (SettingsAdapter.use_volume == 2) {
						stream = AudioManager.STREAM_RING;
					} else if (SettingsAdapter.use_volume == 3) {
						stream = AudioManager.STREAM_ALARM;
					} 

					mp.setAudioStreamType(stream);	
					if ((quiet == 1 && type.equals("announce")) || type.equals("quiet")) {
						float volume = (float) (1 - (Math.log(100 - SettingsAdapter.quiet_volume) / Math.log(100)));
						mp.setVolume(volume, volume);
					}
					mp.prepare();
					if (type.equals("file")) {
						ViewerFragment.pb.setProgress(0);
						ViewerFragment.pb.setMax(mp.getDuration());
					}
					
					if (type.equals("quiet")) {
						Log.i("kancolle announcer", "AudioService Playing Notification Uri");
					} else {
						Log.i("kancolle announcer", "AudioService Playing file: " + filepath);
					}
					mp.start();
					interruptLevel = interrupt;
					if (type.equals("file")) {						
						viewerFragment.updateProgressBar(mp);
					}
					
				} catch (Exception e) {
					Log.e("kancolle announcer", "AudioService Error playing file: " + filepath);
				}
			}
		}
		
		wl.release();
	}
}
