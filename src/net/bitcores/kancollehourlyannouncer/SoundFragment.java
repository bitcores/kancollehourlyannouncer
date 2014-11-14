package net.bitcores.kancollehourlyannouncer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SoundFragment extends Fragment {
	private static SettingsAdapter settingsAdapter;
	private static AudioAdapter audioAdapter;
	
	private static Activity context;
	private static BackgroundReceiver backgroundReceiver;
	private static View rootView;	
	private static Spinner mediaSpinner;
	private static Spinner callSpinner;
	private static CheckBox useQuietBox;	
	private static TextView startTime;
	private static TextView endTime;
	private static TextView bgText;
	private static ImageView bgImage;
	private static SeekBar quietVolumeBar;
	
	private static String target = "";
	
	public SoundFragment() {
		
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(false);
		rootView = inflater.inflate(R.layout.fragment_sound, container, false);

		context = getActivity();
		settingsAdapter = new SettingsAdapter();
		audioAdapter = new AudioAdapter();
	
		LinearLayout startTimeLayout = (LinearLayout)rootView.findViewById(R.id.startTimeLayout);
		LinearLayout endTimeLayout = (LinearLayout)rootView.findViewById(R.id.endTimeLayout);
		mediaSpinner = (Spinner)rootView.findViewById(R.id.mediaSpinner);
		callSpinner = (Spinner)rootView.findViewById(R.id.callSpinner);
		useQuietBox = (CheckBox)rootView.findViewById(R.id.quietHoursBox);		
				
		startTime = (TextView)rootView.findViewById(R.id.quietHoursStart);	
		endTime = (TextView)rootView.findViewById(R.id.quietHoursEnd);
		bgText = (TextView)rootView.findViewById(R.id.settingsBgText);
		bgImage = (ImageView)rootView.findViewById(R.id.settingsBgImage);
		quietVolumeBar = (SeekBar)rootView.findViewById(R.id.quietHoursVolume);	
		
		
		startTimeLayout.setOnTouchListener(pickerTouchListener);
		endTimeLayout.setOnTouchListener(pickerTouchListener);
		
		mediaSpinner.setSelection(SettingsAdapter.use_volume);
		mediaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long it) {
				SettingsAdapter.use_volume = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {		
			}		
		});
		callSpinner.setSelection(SettingsAdapter.call_action);
		callSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SettingsAdapter.call_action = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}		
		});
		
		if (SettingsAdapter.use_quiet == 1) {
			useQuietBox.setChecked(true);
		}
		useQuietBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (useQuietBox.isChecked()) {
					SettingsAdapter.use_quiet = 1;
				} else {
					SettingsAdapter.use_quiet = 0;
				}
			}
		});
		
		startTime.setText(new StringBuilder().append(SettingsAdapter.quiet_start).append(":00"));
		endTime.setText(new StringBuilder().append(SettingsAdapter.quiet_end).append(":00"));
		
		quietVolumeBar.setMax(100);
		quietVolumeBar.setProgress(SettingsAdapter.quiet_volume);
		quietVolumeBar.setOnSeekBarChangeListener(seekBarListener);
		
		settingsAdapter.doBackground(bgImage, bgText);

		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();		
		IntentFilter filter = new IntentFilter();
		backgroundReceiver = new BackgroundReceiver();
		filter.addAction(WidgetShare.UPDATE_WIDGET);
		context.registerReceiver(backgroundReceiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();		
		if (backgroundReceiver != null) {
			context.unregisterReceiver(backgroundReceiver);
		}
	}
		
	private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {	
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			SettingsAdapter.quiet_volume = progress;
			Uri notifysound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			
			//	I may want to test the results of the play/stop audio in the future, but for now I think it is fine
			audioAdapter.playAudio(context, "quiet", 2, notifysound, "");
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			audioAdapter.stopAudio(3);
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {		
		}
	};
	
	private void numberPickerDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		
		View dialogTitle = LayoutInflater.from(context).inflate(R.layout.dialog_title, (ViewGroup)context.findViewById(R.id.dialogLayout), false);
	    TextView titleView = (TextView)dialogTitle.findViewById(R.id.dialogTitleText);
	    
	    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_numberpicker, (ViewGroup)context.findViewById(R.id.dialogImageLayout), false);
		final NumberPicker numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
		numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		TextView minuteText = (TextView)dialogView.findViewById(R.id.minuteText);
		TextView subText = (TextView)dialogView.findViewById(R.id.subText);
		
		if (target.equals("start") || target.equals("end")) {
			minuteText.setText(getResources().getString(R.string.minutefill));
			numberPicker.setMinValue(0);
			numberPicker.setMaxValue(23);
			if (target.equals("start")) {
				numberPicker.setValue(Integer.parseInt(startTime.getText().toString().split(":")[0]));
				titleView.setText(getResources().getString(R.string.startpicktitle));
				subText.setText(getResources().getString(R.string.starttimesub));
			} else if (target.equals("end")) {
				numberPicker.setValue(Integer.parseInt(endTime.getText().toString().split(":")[0]));
				titleView.setText(getResources().getString(R.string.endpicktitle));
				subText.setText(getResources().getString(R.string.endtimesub));
			}
		} 
				   
	    dialogBuilder.setCustomTitle(dialogTitle);
	    dialogBuilder.setView(dialogView);
	    
	    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    {
	        @Override
	        public void onClick(DialogInterface dialog, int button) 
	        {
	        	Integer value = numberPicker.getValue();
	        	
	        	if (target.equals("start")) {
					startTime.setText(new StringBuilder().append(value).append(":00"));
					SettingsAdapter.quiet_start = value;
				} else if (target.equals("end")) {
					endTime.setText(new StringBuilder().append(value).append(":00"));
					SettingsAdapter.quiet_end = value;
				} 
	        }
	    }).setNegativeButton("Cancel", null);
		
		AlertDialog numberPickerDialog = dialogBuilder.create();
		
		numberPickerDialog.show();
	}
	
	
	
	OnTouchListener pickerTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			LinearLayout layout = (LinearLayout)view;
			
			switch (layout.getId()) {
				case R.id.startTimeLayout:
					target = "start";
					break;
				case R.id.endTimeLayout:
					target = "end";
					break;
				default:
					target = "";
					break;
			}
			
			if (!target.isEmpty()) {		
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					layout.setBackgroundColor(0xFFA7A7A7);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					numberPickerDialog();
					layout.setBackgroundColor(0x00A7A7A7);
				}		
				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					layout.setBackgroundColor(0x00A7A7A7);
				} 
			}
			
			return true;
		}		
	};	
	
	private class BackgroundReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			settingsAdapter.doBackground(bgImage, bgText);		
		}
	}
	
}
