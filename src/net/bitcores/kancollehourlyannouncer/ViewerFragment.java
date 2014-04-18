package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ViewerFragment extends Fragment {
	AlarmAdapter alarmAdapter;
	SettingsAdapter settingsAdapter;
	LayoutInflater viewerInflater;

	private static Activity context;		
	private static View rootView;
	private static TextView msgBox;
	private static TableLayout playerTable;
	private static ImageView playerImage;
	private static String currentDir;
	private static String currentKanmusu;
	
	public static ProgressBar pb;
	public static Spinner kanmusuSpinner;
	public Integer currentView;
	public Integer currentImg;
	
	private final Handler handler = new Handler();
	List<String> imageList = new ArrayList<String>();
	
	public ViewerFragment() {
		
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// We want to use this inflater for the image popup
		viewerInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_viewer, container, false);
				
		context = getActivity();	
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		
		pb = (ProgressBar)rootView.findViewById(R.id.playprogress);
		kanmusuSpinner = (Spinner)rootView.findViewById(R.id.kanmususpinner);
		msgBox = (TextView)rootView.findViewById(R.id.msgbox);
		playerTable = (TableLayout)rootView.findViewById(R.id.playertable);
		playerImage = (ImageView)rootView.findViewById(R.id.viewerimage);
		
		playerImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeImage();
			}			
		});
		playerImage.setOnLongClickListener(imgLongClickListener);
		
		kanmusuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currentView = position;
				generateViewer();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {			
			}
		});
		
		setupPage();	
		
		return rootView;
	}
	
	public void setupPage() {
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, SettingsAdapter.full_list);
		kanmusuSpinner.setAdapter(spinnerAdapter);
		if (SettingsAdapter.full_list.size() == 0) {
			kanmusuSpinner.setEnabled(false);
			imageList.clear();
			playerTable.removeAllViews();
			playerImage.setImageDrawable(null);
			currentDir = null;
			if (SettingsAdapter.kanmusu_list.size() > 0) {
				msgBox.setText(getResources().getString(R.string.viewererror1));
			} else {			
				msgBox.setText(getResources().getString(R.string.viewererror2));
			}
		} else { 
			kanmusuSpinner.setEnabled(true);
			kanmusuSpinner.setSelection(SettingsAdapter.full_list.indexOf(SettingsAdapter.hourly_kanmusu));
			msgBox.setText("");
		}
	}
	
	public void generateViewer() {
		//generate sound clip list
		currentKanmusu = SettingsAdapter.full_list.get(currentView);
		currentDir = SettingsAdapter.kancolle_dir + "/" + currentKanmusu;
		
		currentImg = 2;
		imageList.clear();
		playerTable.removeAllViews();
		
		
		// Luckily seeing the sound clips are all named in numbers and the numbers corespond to their use usage I can just make this huge list of
		// names, generate the list based of file names and then deduce the file name from the id of the item in the list when pressed
		// Images are done similarly
		String[] kArray = new String[] { "Introduction", "Secretary 1", "Secretary 2", "Secretary 3", "Ship Construction", "Finish Repair", "Return from Sortie", "Show player's score", "Equipment 1", "Equipment 2",
		"Docking", "Docking (heavy damage)", "Joining Fleet", "Start Sortie", "Battle Start", "Attack", "Air/Night Attack", "Night Battle", "Under fire 1", "Under fire 2", "Badly Damaged", "Sunk", "MVP", 
		"Confession", "Library Intro", "Equipment 3", "Supply", "Secretary Wife", "Idle", "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", 
		"13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00" };
				
		String[] iArray = new String[30];
			
	    try
	    {
	        File dirFile = new File(currentDir);
	        File dirImage = new File(currentDir + "/Image");
	        if (dirFile.exists() && dirFile.isDirectory())
	        {
	        	for(int i = 1; i <= 53; i++) {
	        		File testFile = new File(currentDir + "/" + i + ".mp3");
	        		if (testFile.exists()) {
	        			String clipName = kArray[i-1];
    	            	TableRow row = new TableRow(context);
    	            	TextView kanmusu = new TextView(context);    	            	
    	            	kanmusu.setText(clipName);
    	            	kanmusu.setTextAppearance(context, R.style.hugetext);
    	            	kanmusu.setPadding(2, 2, 2, 2);
    	            	
    	            	row.setId(i);
    	            	row.addView(kanmusu);
    	            	   	            	
    	            	row.setOnTouchListener(rowTouchListener);
    	            	row.setOnClickListener(rowClickListener); 	            	
    	            	row.setOnLongClickListener(rowLongClickListener);
    	            	
    	            	playerTable.addView(row);
	        		}
	        	}
	        	
	        	// Because the number and numbering of image files is not entirely consistent we have to actually build an array of them
	        	for (File file : dirImage.listFiles()) {
	        		String imgname = file.getName();
	        		String[] parts = imgname.split(" ");
	        		String[] parts2 = parts[1].split("\\.");
	        		iArray[Integer.parseInt(parts2[0])] = imgname;
    	        }
	        	
	        	for (String img : iArray) {
	        		if (img != null) {
	        			imageList.add(img);
	        		}
	        	}
	        }
	        
	    } catch (Exception e) {
	    }
        
	    if (imageList.size() > 0) {
		    String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath);
				playerImage.setImageBitmap(bitmap);
			}
	    }
	}
	
	public void playClip(Integer filename) {
		String filepath = currentDir + "/" + filename + ".mp3";
		File checkfile = new File(filepath);
		if (checkfile.exists()) {
			Intent intent = new Intent(context, AudioService.class);
			intent.putExtra("TYPE", "file");
			intent.putExtra("FILE", filepath);
			intent.putExtra("INTERRUPT", 0);
			context.startService(intent);
		}
	}
	
	public void changeImage() {
		if (currentDir != null && imageList.size() > 0) {
			if (currentImg == imageList.size()-1) {
				currentImg = 0;
			} else {
				currentImg++;
			}
			
		    String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath);
				playerImage.setImageBitmap(bitmap);
			}
		}
	}
	
	public void updateProgressBar(final MediaPlayer mp) {
		pb.setProgress(mp.getCurrentPosition());	
		if (mp.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					updateProgressBar(mp);
				}
			};
			handler.postDelayed(notification, 1000);
		}
	}
		
	OnTouchListener rowTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			LinearLayout layout = (LinearLayout)view;
			int action = event.getAction();
			switch (action) {
				case (MotionEvent.ACTION_DOWN) :
					layout.setBackgroundColor(0xFFA7A7A7);
					break;
				case (MotionEvent.ACTION_CANCEL) :
				case (MotionEvent.ACTION_UP) :
					layout.setBackgroundColor(0x00A7A7A7);
					break;
			}   	         				
			return false;
		}
	};
	
	OnClickListener rowClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			playClip(view.getId());	
		}            		
	};
	
	OnLongClickListener rowLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final Integer clipFile = view.getId();
			final CharSequence[] listitem = { context.getResources().getString(R.string.rtonesetlist) };
			AlertDialog.Builder listBuilder = new AlertDialog.Builder(context);
			listBuilder.setItems(listitem, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String filepath = currentDir + "/" + clipFile + ".mp3";

					settingsAdapter.setRingtone(context, filepath, currentKanmusu, clipFile);

					if (SettingsAdapter.use_shuffle == 1) {
						SettingsAdapter.use_shuffle = 0;
						settingsAdapter.saveSettings(context, 0);						
						Toast.makeText(context, context.getResources().getString(R.string.rtoneunset), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context, context.getResources().getString(R.string.rtoneset), Toast.LENGTH_SHORT).show();
					}
					
					SettingsFragment.useShuffleBox.setChecked(false);
				}
			});
			AlertDialog alertList = listBuilder.create();
			alertList.show();
			
			return true;
		}            		
	};
	
	OnLongClickListener imgLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			Dialog imageDialog = new Dialog(context);
			View dialogView = viewerInflater.inflate(R.layout.dialog_image, null);
			ImageView dialogImage = (ImageView)dialogView.findViewById(R.id.dialogImage);

			String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath);
				dialogImage.setImageBitmap(bitmap);
			}
			imageDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			imageDialog.setContentView(dialogView);
			
			imageDialog.show();
			
			return true;
		}
		
	};
}
