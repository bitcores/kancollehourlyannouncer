package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AudioService extends Service {
	SettingsAdapter settingsAdapter;
	ViewerFragment viewerFragment;
	private static MediaPlayer mp;
	private ServiceReceiver receiver;
	
	static String type;
	static String file;
	static Integer interrupt;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// This is set up so that it should be able to handle all audio events if called correctly
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				type = extras.getString("TYPE");
				file = extras.getString("FILE");
				interrupt = extras.getInt("INTERRUPT");
	
				playEvent();
			}
		}
		
		return Service.START_STICKY;
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
			broadcastIntent.setAction("net.bitcores.kancollehourlyannouncer.UPDATE_WIDGET");
			broadcastIntent.putExtra("placeholder", "data");
			sendBroadcast(broadcastIntent);
		}
	}
	
	public void playEvent() {
		if (interrupt == 1) {
			mp.reset();
		}
		if (!mp.isPlaying()) {
			String filepath = "";
			File checkfile;
			
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
				}
			} else if (type.equals("file")) {
				viewerFragment = new ViewerFragment();
				filepath = file;
			} else {
				return;
			}

			checkfile = new File(filepath);
			if (checkfile.exists()) {
				try {					
					mp.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							// Because we want the service to stay alive and be ready to play other clips we reset the player instead of releasing it
							Log.i("kancolle announcer", "AudioService MediaPlayer reset");
							mp.reset();
						}
					});
					
					FileInputStream fis = new FileInputStream(filepath);			
					mp.setDataSource(fis.getFD());
					fis.close();
					
					int stream = AudioManager.STREAM_NOTIFICATION;
					if (SettingsAdapter.use_volume == 1) {
						stream = AudioManager.STREAM_MUSIC;
					} else if (SettingsAdapter.use_volume == 2) {
						stream = AudioManager.STREAM_RING;
					} else if (SettingsAdapter.use_volume == 3) {
						stream = AudioManager.STREAM_ALARM;
					} 

					mp.setAudioStreamType(stream);	
					
					mp.prepare();
					if (type.equals("file")) {
						ViewerFragment.pb.setProgress(0);
						ViewerFragment.pb.setMax(mp.getDuration());
					}
					
					Log.i("kancolle announcer", "AudioService Playing file: " + filepath);
					mp.start();
					if (type.equals("file")) {						
						viewerFragment.updateProgressBar(mp);
					}
					
				} catch (Exception e) {
					Log.e("kancolle announcer", "AudioService Error playing file: " + filepath);
				}
			}
		}
	}
}
