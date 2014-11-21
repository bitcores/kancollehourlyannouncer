package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class AudioAdapter {
	private static MediaPlayer mp = null;
	private static Integer interruptLevel = 0;
	
	public static final String[] playerMessage = new String[] { "MediaPlayer: Playing", "MediaPlayer: Not ready", "MediaPlayer: File not found or no URI", "MediaPlayer: Could not start", "MediaPlayer: Player reset" };
	
	public AudioAdapter() {
		
	}
	
	public boolean playAudio(Context context, String type, Integer interrupt, Uri clipUri, String filePath) {
		if (!initMediaPlayer(interrupt)) {
			logMessage(1, clipUri, filePath, 0);
			return false;
		}
		
		if (!setDataSource(context, clipUri, filePath)) {
			logMessage(2, clipUri, filePath, 0);
			return false;
		}
		
		//	I can't think of a reason why setting volume would fail right now so I won't test it
		setVolume(context, type);
		
		if (!startMediaPlayer(type, interrupt, clipUri, filePath)) {
			logMessage(3, clipUri, filePath, 0);
			return false;
			
		} else {
			logMessage(0, clipUri, filePath, 0);
			
			interruptLevel = interrupt;
			
			if (type.equals("file")) {	
				ViewerFragment viewerFragment = new ViewerFragment();
				ViewerFragment.pb.setProgress(0);
				ViewerFragment.pb.setMax(mp.getDuration());
				viewerFragment.updateProgressBar(mp);
			}
			
			return true;
		}	
	}
	
	//	Returns true if the mediaplayer is reset/ready for use, false if the player cannot be interrupted
	public boolean stopAudio(Integer interrupt) {
		return initMediaPlayer(interrupt);
	}
	
	//	Return true if the mediaplayer is ready for use
	private boolean initMediaPlayer(Integer interrupt) {
		if (mp == null) {
			mp = new MediaPlayer();
		} else if (mp.isPlaying()) {			
			return checkInterrupt(interrupt);
		}
		return true;
	}
	
	//	Return true if interrupt successful
	private boolean checkInterrupt(Integer interrupt) {
		if (interrupt > interruptLevel) {
			mp.reset();
			interruptLevel = 0;
			return true;
		}
		return false;
	}
	
	//	Return true if datasource set
	private boolean setDataSource(Context context, Uri clipUri, String filePath) {
		try {
			if (clipUri != null) {
				mp.setDataSource(context, clipUri);
			} else if (!filePath.equals(""))  {	
				File checkFile = new File(filePath);
				if (checkFile.exists()) {
					FileInputStream fis;					
					fis = new FileInputStream(filePath);
					mp.setDataSource(fis.getFD());
					fis.close();		
				}					
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}	
		
		return true;
	}
	
	//	Return true if volume is set
	private boolean setVolume(Context context, String type) {
		Calendar cal = Calendar.getInstance();
		int chour = cal.get(Calendar.HOUR_OF_DAY);
		int start = SettingsAdapter.quiet_start;
		int end = SettingsAdapter.quiet_end;
		boolean quiet = false;
		
		int stream = AudioManager.STREAM_NOTIFICATION;
		if (SettingsAdapter.use_volume == 1) {
			stream = AudioManager.STREAM_MUSIC;
		} else if (SettingsAdapter.use_volume == 2) {
			stream = AudioManager.STREAM_RING;
		} else if (SettingsAdapter.use_volume == 3) {
			stream = AudioManager.STREAM_ALARM;
		} 
		mp.setAudioStreamType(stream);	
		
		// Getting phone call state
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		int callstate = tm.getCallState();
					
		if (callstate != TelephonyManager.CALL_STATE_IDLE && SettingsAdapter.call_action == 1) {
			quiet = true;
		}
		else if (SettingsAdapter.use_quiet == 1) {
			if (start > end) {
				if (chour >= start) {
					quiet = true;
				} else {
					end += 24;
					chour += 24;
					if (chour >= start && chour < end) {
						quiet = true;
					}
				}
				
			} else {
				if ((chour >= start && chour < end) || start == end) {
					quiet = true;
				}
			}
		}
			
		//	Because we don't release the mediaplayer changes to the volume can persist. We need to make sure it is the desired level for the application.
		if ((quiet && type.equals("announce")) || type.equals("quiet")) {
			float volume = (float) (1 - (Math.log(100 - SettingsAdapter.quiet_volume) / Math.log(100)));
			mp.setVolume(volume, volume);
		} else {
			mp.setVolume(1.0f, 1.0f);
		}
		
		return true;
	}
	
	//	Return true if the mediaplayer starts correctly
	private boolean startMediaPlayer(String type, Integer interrupt, Uri clipUri, String filePath) {		
		try {
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// Because we want the service to stay alive and be ready to play other clips we reset the player instead of releasing it
					logMessage(4, null, "", 5);
					mp.reset();
					interruptLevel = 0;
				}
			});
			
			mp.prepare();

			mp.start();
					
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
			
		return true;
	}
	
	//	Generate error message for log
	private void logMessage(Integer errorLevel, Uri clipUri, String filePath, Integer verbose) {
		String message = "";
		String media = "";
		if (clipUri != null) {
			media = "file: Notification Uri";
		} else {
			media = "file: " + filePath;
		}
		
		switch (errorLevel) {
			case 0:
			case 2:
			case 3:
				message = playerMessage[errorLevel] + " " + media;
				break;
			case 1:
			case 4:
				message = playerMessage[errorLevel];
				break;		
		}
		
		SettingsAdapter settingsAdapter = new SettingsAdapter();
		settingsAdapter.logEvent(message, verbose);
	}
}
