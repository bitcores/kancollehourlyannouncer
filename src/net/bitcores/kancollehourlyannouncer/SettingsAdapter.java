package net.bitcores.kancollehourlyannouncer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsAdapter {
	private SharedPreferences preferences;
	private static final String LOG_FILE_NAME = "eventlog.txt";	
	
	public static final String PREF_FILE_NAME = "settings";
	public static Boolean init = false;	
	public static Integer enabled = 0;
	public static Integer use_volume = 0;
	public static Integer call_action = 0;
	public static Integer use_quiet = 0;
	public static Integer quiet_start = 0;
	public static Integer quiet_end = 6;
	public static Integer quiet_volume = 0;
	public static Integer use_shuffle = 0;
	public static Integer shuffle_action = 0;
	public static Integer enable_log = 0;
	public static Integer verbose_log = 0;
	public static Integer secretary_bg = 0;
	public static Integer secretary_bgimgtype = 0;
	public static Integer secretary_widget = 0;
	public static Integer secretary_widgetimgtype = 0;
	public static Integer boot_idle = 0;
	public static Integer secretary_idle = 0;
	public static String kancolle_dir = "";
	public static String hourly_kanmusu = "";
	public static String viewer_kanmusu = "";
	public static String secretary_kanmusu = "";
	public static List<String> full_list = new ArrayList<String>();
	public static List<String> kanmusu_list = new ArrayList<String>();
	public static List<String> kanmusu_select = new ArrayList<String>();
	public static List<String> kanmusu_use = new ArrayList<String>();
	public static List<String> kanmusu_shufflelist = new ArrayList<String>();
	public static List<String> event_log = new ArrayList<String>();
	
	// Luckily seeing the sound clips are all named in numbers and the numbers corespond to their use usage I can just make this huge list of
	// names, generate the list based of file names and then deduce the file name from the id of the item in the list when pressed
	// Images are done similarly
	// I want to use this same list in the settings fragment for the shuffle list selection so it can be here instead
	public static final String[] kArray = new String[] { "Introduction", "Secretary 1", "Secretary 2", "Secretary 3", "Ship Construction", "Finish Repair", "Return from Sortie", "Show player's score", "Equipment 1", "Equipment 2",
		"Docking", "Docking (heavy damage)", "Joining Fleet", "Start Sortie", "Battle Start", "Attack", "Air/Night Attack", "Night Battle", "Under fire 1", "Under fire 2", "Badly Damaged", "Sunk", "MVP", 
		"Confession", "Library Intro", "Equipment 3", "Supply", "Secretary Wife", "Idle", "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", 
		"13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00" };

	public SettingsAdapter() {
		
	}
	
	public void initSettings(Context context) {
		preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		kancolle_dir = preferences.getString("kancolle_dir", "");
		enabled = preferences.getInt("enabled", 0);
		use_volume = preferences.getInt("use_volume", 0);
		call_action = preferences.getInt("call_action", 0);
		use_quiet = preferences.getInt("use_quiet", 0);
		quiet_start = preferences.getInt("quiet_start", 0);
		quiet_end = preferences.getInt("quiet_end", 6);
		quiet_volume = preferences.getInt("quiet_volume", 0);
		use_shuffle = preferences.getInt("use_shuffle", 0);
		shuffle_action = preferences.getInt("shuffle_action", 0);
		enable_log = preferences.getInt("enable_log", 0);
		verbose_log = preferences.getInt("verbose_log", 0);
		secretary_bg = preferences.getInt("secretary_bg", 0);
		secretary_bgimgtype = preferences.getInt("secretary_bgimgtype", 0);
		secretary_widget = preferences.getInt("secretary_widget", 0);
		secretary_widgetimgtype = preferences.getInt("secretary_widgetimgtype", 0);
		boot_idle = preferences.getInt("boot_idle", 0);
		secretary_idle = preferences.getInt("secretary_idle", 0);
		hourly_kanmusu = preferences.getString("hourly_kanmusu", "");
		viewer_kanmusu = preferences.getString("viewer_kanmusu", "");
		secretary_kanmusu = preferences.getString("secretary_kanmusu", "");
		
		Set<String> full = new HashSet<String>();
		full = preferences.getStringSet("full_list", null);
		if (full != null) {
			full_list.addAll(full);
			sortList(full_list);
		}
		Set<String> use = new HashSet<String>();
		use = preferences.getStringSet("kanmusu_use", null);
		if (use != null) {
			kanmusu_use.addAll(use);
			kanmusu_select.addAll(use);
			sortList(kanmusu_use);
			sortList(kanmusu_select);
		}
		Set<String> list = new HashSet<String>();
		list = preferences.getStringSet("kanmusu_list", null);
		if (list != null) {
			kanmusu_list.addAll(list);
			sortList(kanmusu_list);
		}
		Set<String> shufflelist = new HashSet<String>();
		shufflelist = preferences.getStringSet("kanmusu_shufflelist", null);
		if (shufflelist != null) {
			kanmusu_shufflelist.addAll(shufflelist);			
		} else {
			for (Integer s = 0; s < 29; s++) {
				kanmusu_shufflelist.add(s.toString());
			}
		}
		
		File f = new File(context.getFilesDir(), LOG_FILE_NAME);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String line;
			while ((line = br.readLine()) != null){
				event_log.add(line);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		init = true;
	}
	
	public void saveSettings(Context context) {
		preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString("kancolle_dir", SettingsAdapter.kancolle_dir);
		editor.putInt("enabled", enabled);
		editor.putInt("use_volume", use_volume);
		editor.putInt("call_action", call_action);
		editor.putInt("use_quiet", use_quiet);
		editor.putInt("quiet_start", quiet_start);
		editor.putInt("quiet_end", quiet_end);
		editor.putInt("quiet_volume", quiet_volume);
		editor.putInt("use_shuffle", use_shuffle);
		editor.putInt("shuffle_action", shuffle_action);
		editor.putInt("enable_log", enable_log);
		editor.putInt("verbose_log", verbose_log);
		editor.putInt("secretary_bg", secretary_bg);
		editor.putInt("secretary_bgimgtype", secretary_bgimgtype);
		editor.putInt("secretary_widget", secretary_widget);
		editor.putInt("secretary_widgetimgtype", secretary_widgetimgtype);
		editor.putInt("boot_idle", boot_idle);
		editor.putInt("secretary_idle", secretary_idle);
		editor.putString("hourly_kanmusu", hourly_kanmusu);
		editor.putString("viewer_kanmusu", viewer_kanmusu);
		editor.putString("secretary_kanmusu", secretary_kanmusu);
		
		Set<String> full_set = new HashSet<String>();
		full_set.addAll(full_list);
		editor.putStringSet("full_list", full_set);
		
		Set<String> list_set = new HashSet<String>();
		list_set.addAll(kanmusu_list);
		editor.putStringSet("kanmusu_list", list_set);
		
		Set<String> use_set = new HashSet<String>();
		use_set.addAll(kanmusu_use);
		editor.putStringSet("kanmusu_use", use_set);
		
		Set<String> shufflelist_set = new HashSet<String>();
		shufflelist_set.addAll(kanmusu_shufflelist);
		editor.putStringSet("kanmusu_shufflelist", shufflelist_set);
			
		editor.commit();
	}
	
	public void sortList(List<String> tosort) {
		Collections.sort(tosort, new Comparator<String>()
	    {
	        public int compare(String o1, String o2) 
	        {
	            return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
	        }
	    });
	}
	
	public String getKanmusu() {
		
		if (hourly_kanmusu.equals("")) {
			int rand = 0;
			if (kanmusu_use.size() > 1) {
				int max = kanmusu_use.size();
				Random r = new Random();
				rand = r.nextInt(max);
			}
			hourly_kanmusu = kanmusu_use.get(rand);
		} 
		
		return hourly_kanmusu;
	}
	
	public String getSecretary(String type, String app) {
		class local {
			public List<String> img(String[] array, String kanmusu) {
				List<String> imgList = new ArrayList<String>();
				for (String filename : array) {
					String filepath = kancolle_dir + "/" + kanmusu + "/Image/" + filename;
					File checkfile = new File(filepath);
					if (checkfile.exists()) {
						imgList.add(filename);
					}
				}
				
				return imgList;
			}
		}
		
		// The Arpeggio event characters didn't include an image 17.png but instead had a 16 and 18 that were usually combined to fill the place of 17
		// we use 16 if 17 isn't present
		final String[] bgImgs = new String[] { "image 17.png", "image 19.png", "image 16.png", "image 18.png" };
		final String[] widgetImgs = new String[] { "image 21.png", "image 23.png" };
		List<String> availImgs = new ArrayList<String>();
		String kanmusu = "";	
		local parseImgs = new local();

		if (app.equals("bg")) {
			if (secretary_bg == 1 && !secretary_kanmusu.equals("")) {
				kanmusu = secretary_kanmusu;
			} else {
				kanmusu = getKanmusu();
			}		
		} else if (app.equals("widget")) {
			if (secretary_widget == 1 && !secretary_kanmusu.equals("")) {
				kanmusu = secretary_kanmusu;
			} else {
				kanmusu = getKanmusu();
			}			
		}
		
		if (type.equals("bg")) {
			availImgs = parseImgs.img(bgImgs, kanmusu);
		} else if (type.equals("widget")) {
			availImgs = parseImgs.img(widgetImgs, kanmusu);
		}
		
		if (availImgs.size() == 0) {
			logEvent("No images found for " + kanmusu, 0);
			return null;
		} else if (availImgs.size() == 1) {
			return kancolle_dir + "/" + kanmusu + "/Image/" + availImgs.get(0);
		} else if (availImgs.size() == 2) {		
			if (app.equals("bg")) {
				return kancolle_dir + "/" + kanmusu + "/Image/" + availImgs.get(secretary_bgimgtype);		
			} else if (app.equals("widget")) {
				return kancolle_dir + "/" + kanmusu + "/Image/" + availImgs.get(secretary_widgetimgtype);	
			} else {
				return null;
			}
		}
				
		return null;
	}
	
	public void setRingtone(Context context, String filepath, String name, Integer num) {
		String title = name + " " + num;
		ContentValues values = new ContentValues();	
		values.put(MediaStore.MediaColumns.TITLE, title);
		
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(filepath);
		Uri ringtoneUri;
		
		String[] projection = {MediaStore.Audio.AudioColumns._ID, MediaStore.Audio.AudioColumns.TITLE};
		ContentResolver mCr = context.getContentResolver();
		
		// Due to changes in Android 4.3+ the data column in the MediaStore must be unique so the data cannot be inserted just to get a
		// Uri generated like most examples show, thus we now search the MediaStore for the file first and update the title if it doesnt
		// include the shipgirls name, which it wont if the clip was added to the MediaStore automatically
		Cursor cursor = mCr.query(uri, projection, MediaStore.Audio.AudioColumns.DATA + " LIKE ?", new String[] { filepath }, null);
		if (cursor.moveToFirst() != false ) {
			long audioid = cursor.getLong(cursor.getColumnIndex(projection[0]));
			String audiotitle = cursor.getString(cursor.getColumnIndex(projection[1]));
			if (!audiotitle.equals(title)) {
				mCr.update(uri, values, MediaStore.Audio.AudioColumns._ID + "=" + audioid, null);
			}
			ringtoneUri = Uri.parse(uri + "/" + audioid);
		} else {
			values.put(MediaStore.MediaColumns.DATA, filepath);
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
			ringtoneUri = mCr.insert(uri, values);
		}
		cursor.close();
		
		RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, ringtoneUri);
	}
	
	public void shuffleRingtone(Context context) {
		int rand = 0;
		int rand2 = 0;
		int listsize = kanmusu_shufflelist.size();
		String kanmusu = "";
		
		if (shuffle_action == 0) {
			if (kanmusu_list.size() > 1) {
				int max = kanmusu_list.size();
				Random r = new Random();
				rand = r.nextInt(max);
			}
			kanmusu =  kanmusu_list.get(rand);
		} else if (shuffle_action == 1) {
			if (kanmusu_use.size() > 1) {
				int max = kanmusu_use.size();
				Random r = new Random();
				rand = r.nextInt(max);
			}
			kanmusu =  kanmusu_use.get(rand);
		} else if (shuffle_action == 2) {
			kanmusu = hourly_kanmusu;
		} else {
			kanmusu = viewer_kanmusu;
		}
		
		//	We are now using kanmusu_shufflelist to determine selection of clips that are chosen from, these are still within the 1-29 set
		//	Because a kanmusu may not have one of the selected clips we will allow the randomization to loop up to three times to fine one
		for (int c = 0; c < 3; c++) {
			if (listsize > 0) {
				if (listsize > 1) {
					Random r2 = new Random();
					rand2 = r2.nextInt(listsize);
				}
				Integer clip = Integer.parseInt(kanmusu_shufflelist.get(rand2)) + 1;			
				String filepath = kancolle_dir + "/" + kanmusu + "/" + clip + ".mp3";
				
				File checkfile = new File(filepath);		
				if (checkfile.exists()) {
					Log.i("kancolle announcer", "Ringtone Set: " + filepath);				
					setRingtone(context, filepath, kanmusu, rand2);
					
					break;
				}
			}
		}
	}
	
	public void updateWidgets(Context context) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(WidgetProvider.UPDATE_WIDGET);
		context.sendBroadcast(broadcastIntent);
	}
	
	public void doBackground(ImageView bgImage, TextView bgText) {
		BitmapAdapter bitmapAdapter = new BitmapAdapter();

		if (kanmusu_use.size() > 0) {
			String kanmusu = "";
			if (secretary_bg == 0 || secretary_kanmusu == "") {
				kanmusu = getKanmusu();
			} else {
				kanmusu = secretary_kanmusu;
			}
						
			String filepath = getSecretary("bg", "bg");
			if (filepath != null) {
				bitmapAdapter.loadBitmap(filepath, bgImage);
			}
			bgText.setText(kanmusu);	
		}
	}
	
	public void logEvent(String logMessage, Integer logLevel) {
		Log.i("kancolle announcer", logMessage);
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss.SSS E dd", Locale.US);
		
		if (enable_log == 1) {
			if (logLevel < 5 || verbose_log == 1) {
				//	I want a maximum of 128 entries in the log
				while (event_log.size() > 128) {
					event_log.remove(0);
				}
				
				event_log.add(f.format(cal.getTimeInMillis()) + ": " + logMessage);				
			}
		}	
	}
	
	public void writeLog(Context context) {
		String output = "";
		
		for (String line : SettingsAdapter.event_log) {
			output = output + line + "\n";
		}
		
		try {
			FileOutputStream fo = context.openFileOutput(LOG_FILE_NAME, Context.MODE_PRIVATE);
			fo.write(output.getBytes());			
		
		} catch (FileNotFoundException e) {
			logEvent("Eventlog: Log file could not be created. Please reinstall the app to fix.", 0);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
}
