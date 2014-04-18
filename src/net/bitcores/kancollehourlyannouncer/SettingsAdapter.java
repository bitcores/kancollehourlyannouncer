package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class SettingsAdapter {
	SharedPreferences preferences;
	
	public static final String PREF_FILE_NAME = "settings";
	public static Boolean init = false;
	public static String kancolle_dir = "";
	public static Integer enabled = 0;
	public static Integer use_volume = 0;
	public static Integer call_action = 0;
	public static Integer use_quiet = 0;
	public static Integer quiet_start = 0;
	public static Integer quiet_end = 6;
	public static Integer quiet_volume = 0;
	public static Integer use_shuffle = 0;
	public static Integer shuffle_action = 0;
	public static String hourly_kanmusu = "";
	public static List<String> full_list = new ArrayList<String>();
	public static List<String> kanmusu_list = new ArrayList<String>();
	public static List<String> kanmusu_select = new ArrayList<String>();
	public static List<String> kanmusu_use = new ArrayList<String>();
	

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
		hourly_kanmusu = preferences.getString("hourly_kanmusu", "");
		
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
		
		init = true;
	}
	
	public void saveSettings(Context context, int toast) {
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
		editor.putString("hourly_kanmusu", hourly_kanmusu);
		
		Set<String> full_set = new HashSet<String>();
		full_set.addAll(full_list);
		editor.putStringSet("full_list", full_set);
		
		Set<String> list_set = new HashSet<String>();
		list_set.addAll(kanmusu_list);
		editor.putStringSet("kanmusu_list", list_set);
		
		Set<String> use_set = new HashSet<String>();
		use_set.addAll(kanmusu_use);
		editor.putStringSet("kanmusu_use", use_set);
			
		editor.commit();
		
		if (toast == 1) {
			Toast.makeText(context, context.getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
		}
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
	
	public void setRingtone(Context context, String filepath, String name, Integer num) {
		String title = name + " " + num;
		ContentValues values = new ContentValues();	
		values.put(MediaStore.MediaColumns.TITLE, title);
		
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(filepath);
		Uri ringtoneUri;
		
		String[] projection = {MediaStore.Audio.AudioColumns._ID, MediaStore.Audio.AudioColumns.TITLE};
		ContentResolver mCr = context.getContentResolver();
		
		// Due to changes in Android 4.3+ the data colum in the MediaStore must be unique so the data cannot be inserted just to get a
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
		
		RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, ringtoneUri);
	}
	
	public void shuffleRingtone(Context context) {
		int rand = 0;
		int rand2 = 0;
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
		} else {
			kanmusu = hourly_kanmusu;
		}
		
		//	Getting time notifications for emails/sms could be confusing so we keep it to the 1-29 clips
		Random r2 = new Random();
		rand2 = r2.nextInt(29) + 1;		
				
		String filepath = kancolle_dir + "/" + kanmusu + "/" + rand2 + ".mp3";
		File checkfile = new File(filepath);
		
		if (checkfile.exists()) {
			Log.i("kancolle announcer", "Ringtone Set: " + filepath);
			
			setRingtone(context, filepath, kanmusu, rand2);
		}
	}
	
}
