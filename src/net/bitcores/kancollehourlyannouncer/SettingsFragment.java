package net.bitcores.kancollehourlyannouncer;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class SettingsFragment extends Fragment {
	SharedPreferences preferences;
	SettingsAdapter settingsAdapter;
	MediaPlayer mp;
	Activity context;
	
	private static Spinner mediaSpinner;
	private static Spinner callSpinner;
	private static Spinner shuffleSpinner;
	private static CheckBox useQuietBox;	
	private static TextView startTime;
	private static TextView endTime;
	private static SeekBar quietVolumeBar;
		
	public static CheckBox useShuffleBox;
	public View rootView;
	public String target;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_settings, container, false);
		
		context = getActivity();
		settingsAdapter = new SettingsAdapter();
		preferences = context.getSharedPreferences(SettingsAdapter.PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		mediaSpinner = (Spinner)rootView.findViewById(R.id.mediaSpinner);
		callSpinner = (Spinner)rootView.findViewById(R.id.callSpinner);
		shuffleSpinner = (Spinner)rootView.findViewById(R.id.shuffleSpinner);
		useQuietBox = (CheckBox)rootView.findViewById(R.id.quietHoursBox);		
		useShuffleBox = (CheckBox)rootView.findViewById(R.id.shuffleBox);			
		startTime = (TextView)rootView.findViewById(R.id.quietHoursStart);	
		endTime = (TextView)rootView.findViewById(R.id.quietHoursEnd);	
		
		mediaSpinner.setSelection(SettingsAdapter.use_volume);
		callSpinner.setSelection(SettingsAdapter.call_action);
		shuffleSpinner.setSelection(SettingsAdapter.shuffle_action);
		if (SettingsAdapter.use_quiet == 1) {
			useQuietBox.setChecked(true);
		} 
		if (SettingsAdapter.use_shuffle == 1) {
			useShuffleBox.setChecked(true);
		} 
		startTime.setText(new StringBuilder().append(pad(SettingsAdapter.quiet_start)).append(":00"));
		endTime.setText(new StringBuilder().append(pad(SettingsAdapter.quiet_end)).append(":00"));
		
		LinearLayout startTimeLayout = (LinearLayout)rootView.findViewById(R.id.startTimeLayout);
		
		// There is probably a better way of picking the start and end times, seeing only the hour is important
		// the full time picker isnt needed, but it is simple to use so i like it
		startTimeLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				LinearLayout layout = (LinearLayout)view;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					layout.setBackgroundColor(0xFFA7A7A7);
				} 
				
				if (event.getAction() == MotionEvent.ACTION_UP) {
					target = "start";
					String[] getStart = startTime.getText().toString().split(":");
					new TimePickerDialog(context, timePickerListener, Integer.parseInt(getStart[0]), 0, true).show();	
					layout.setBackgroundColor(0x00A7A7A7);
				}
				
				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					layout.setBackgroundColor(0x00A7A7A7);
				} 
				
				return true;
			}			
		});
		
		LinearLayout endTimeLayout = (LinearLayout)rootView.findViewById(R.id.endTimeLayout);
		endTimeLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				LinearLayout layout = (LinearLayout)view;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					layout.setBackgroundColor(0xFFA7A7A7);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					target = "end";
					String[] getEnd = endTime.getText().toString().split(":");
					new TimePickerDialog(context, timePickerListener, Integer.parseInt(getEnd[0]), 0, true).show();
					layout.setBackgroundColor(0x00A7A7A7);
				}
				
				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					layout.setBackgroundColor(0x00A7A7A7);
				} 
				
				return true;
			}		
		});
		
		quietVolumeBar = (SeekBar)rootView.findViewById(R.id.quietHoursVolume);	
		quietVolumeBar.setMax(100);
		quietVolumeBar.setProgress(SettingsAdapter.quiet_volume);
		quietVolumeBar.setOnSeekBarChangeListener(seekBarListener);
		
		Button saveButton = (Button)rootView.findViewById(R.id.saveSettings);
		saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	saveSettings();
        	}     
        });

		return rootView;
	}
		
	private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {	
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// At some point I want this to use the MediaPlayer in AudioService
			mp = new MediaPlayer();
			Uri notifysound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			int progress = seekBar.getProgress();
			
			Integer streamType = mediaSpinner.getSelectedItemPosition();
			float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));	
			
			try {
		        mp.setDataSource(context, notifysound);
		        
		        int stream = AudioManager.STREAM_NOTIFICATION;
				if (streamType == 1) {
					stream = AudioManager.STREAM_MUSIC;
				} else if (streamType == 2) {
					stream = AudioManager.STREAM_RING;
				} else if (streamType == 3) {
					stream = AudioManager.STREAM_ALARM;
				} 
				
		        mp.setAudioStreamType(stream);
		        mp.setVolume(volume, volume);
		        mp.prepare();
		        mp.setOnCompletionListener(new OnCompletionListener() {
		        	@Override
		        	public void onCompletion(MediaPlayer mp) {
		        		mp.release();
		        	}
		        });
		        mp.start();
			} catch (Exception e) {
				
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if (mp != null) {
				mp.release();
			}
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {		
		}
	};

	
	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
			int hour = selectedHour;
 
			if (target.equals("start")) {
				startTime.setText(new StringBuilder().append(pad(hour)).append(":00"));
			} else if (target.equals("end")) {
				endTime.setText(new StringBuilder().append(pad(hour)).append(":00"));
			}
		}
	};
	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}
	
	public void saveSettings() {
		SettingsAdapter.use_volume = mediaSpinner.getSelectedItemPosition();	
		SettingsAdapter.call_action = callSpinner.getSelectedItemPosition();
		SettingsAdapter.shuffle_action = shuffleSpinner.getSelectedItemPosition();
		
		if (useQuietBox.isChecked()) {
			SettingsAdapter.use_quiet = 1;
		} else {
			SettingsAdapter.use_quiet = 0;
		}
		
		if (useShuffleBox.isChecked()) {
			SettingsAdapter.use_shuffle = 1;
		} else {
			SettingsAdapter.use_shuffle = 0;
		}
		
		String[] getStart = startTime.getText().toString().split(":");
		SettingsAdapter.quiet_start = Integer.parseInt(getStart[0]);
		String[] getEnd = endTime.getText().toString().split(":");
		SettingsAdapter.quiet_end = Integer.parseInt(getEnd[0]);
		
		SettingsAdapter.quiet_volume = quietVolumeBar.getProgress();
		
		settingsAdapter.saveSettings(context, 1);
	}
	
}
