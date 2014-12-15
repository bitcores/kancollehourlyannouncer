package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ViewerFragment extends Fragment {
	private static SettingsAdapter settingsAdapter;
	private static AudioAdapter audioAdapter;
	private static BitmapAdapter bitmapAdapter;

	private static Activity context;		
	private static View rootView;
	private static TextView msgBox;
	private static Spinner playerSpinner;
	private static Spinner kanmusuSpinner;
	private static ImageView playerImage;
	private static ImageView leftImage;
	private static ImageView rightImage;
	private static Button playButton;
	private static Button stopButton;
	
	private static String currentKanmusu;
	private static String currentDir;
	private static Integer currentId;
	private static Integer currentView;
	private static Integer currentImg;
	
	public static ProgressBar pb;
	
	private final Handler handler = new Handler();
	List<String> imageList = new ArrayList<String>();
	List<String> clipList = new ArrayList<String>();
	List<Integer> nameList = new ArrayList<Integer>();
	
	public ViewerFragment() {
		
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		Integer layout = null;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			layout = R.layout.fragment_viewer_land;
		} else {
			layout = R.layout.fragment_viewer;
		}
		
		rootView = inflater.inflate(layout, container, false);

		context = getActivity();
		audioAdapter = new AudioAdapter();
		settingsAdapter = new SettingsAdapter();
		bitmapAdapter = new BitmapAdapter();
		
		pb = (ProgressBar)rootView.findViewById(R.id.playprogress);
		kanmusuSpinner = (Spinner)rootView.findViewById(R.id.kanmususpinner);
		playerSpinner = (Spinner)rootView.findViewById(R.id.playerspinner);
		msgBox = (TextView)rootView.findViewById(R.id.msgbox);
		playerImage = (ImageView)rootView.findViewById(R.id.viewerimage);
		leftImage = (ImageView)rootView.findViewById(R.id.leftimage);
		rightImage = (ImageView)rootView.findViewById(R.id.rightimage);
		playButton = (Button)rootView.findViewById(R.id.playbutton);
		stopButton = (Button)rootView.findViewById(R.id.stopbutton);
		
		leftImage.setOnClickListener(imageChangeListener);
		rightImage.setOnClickListener(imageChangeListener);
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
		playerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currentId = position;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}			
		});
		
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				playClip(nameList.get(currentId));
			}		
		});	
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				stopClip();
			}		
		});
		
		if (savedInstanceState == null) {
			currentKanmusu = SettingsAdapter.hourly_kanmusu;
		}
		setupPage();
		
		return rootView;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {	
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.viewerfragment_actions, menu);    
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_setringtone:
	    	String filepath = currentDir + "/" + currentId + ".mp3";						
			settingsAdapter.setRingtone(context, filepath, currentKanmusu, currentId);

			if (SettingsAdapter.use_shuffle == 1) {
				SettingsAdapter.use_shuffle = 0;					
				Toast.makeText(context, context.getResources().getString(R.string.rtoneunset), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, context.getResources().getString(R.string.rtoneset), Toast.LENGTH_SHORT).show();
			}
	        return true;
	    case R.id.action_setkanmusugroup:
	        if (SettingsAdapter.use_shuffle == 0) {
				SettingsAdapter.use_shuffle = 1;							
				Toast.makeText(context, context.getResources().getString(R.string.rtoneshufenset), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, context.getResources().getString(R.string.rtoneshufset), Toast.LENGTH_SHORT).show();
			}
			
			SettingsAdapter.viewer_kanmusu = currentKanmusu;
			SettingsAdapter.shuffle_action = 3;										
			settingsAdapter.shuffleRingtone(context);
	        return true;
	    case R.id.action_setsecretary:
	    	SettingsAdapter.secretary_kanmusu = currentKanmusu;
	    	Toast.makeText(context, context.getResources().getString(R.string.secretaryset), Toast.LENGTH_SHORT).show();
	    	return true;
	    default:
	        break;
	    }

	    return false;
	}
	
	private void setupPage() {
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, SettingsAdapter.full_list);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		kanmusuSpinner.setAdapter(spinnerAdapter);
		if (SettingsAdapter.full_list.size() == 0) {
			kanmusuSpinner.setEnabled(false);
			playerSpinner.setEnabled(false);
			imageList.clear();
			clipList.clear();
			nameList.clear();
			playerImage.setImageDrawable(null);
			currentDir = null;
			currentId = null;
			if (SettingsAdapter.kanmusu_list.size() > 0) {
				msgBox.setText(getResources().getString(R.string.viewererror1));
			} else {			
				msgBox.setText(getResources().getString(R.string.viewererror2));
			}
		} else { 
			kanmusuSpinner.setEnabled(true);
			playerSpinner.setEnabled(true);
			kanmusuSpinner.setSelection(SettingsAdapter.full_list.indexOf(currentKanmusu));
			msgBox.setText("");
		}
		
		playButton.setEnabled(false);
		stopButton.setEnabled(false);
	}
	
	private void generateViewer() {
		//generate sound clip list
		currentKanmusu = SettingsAdapter.full_list.get(currentView);
		currentDir = SettingsAdapter.kancolle_dir + "/" + currentKanmusu;
		
		currentImg = 2;
		imageList.clear();
		clipList.clear();
		nameList.clear();
						
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
	        			clipList.add(SettingsAdapter.kArray[i-1]);
	        			nameList.add(i);
	        		}
	        	}
	        	
	        	ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, clipList);        	
	        	playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        	playerSpinner.setAdapter(playerAdapter);
	        	
	        	// Because the number and numbering of image files is not entirely consistent we have to actually build an array of them
	        	for (File file : dirImage.listFiles()) {
	        		String imgname = file.getName();
	        		if (imgname.contains("image") && (imgname.contains(".jpg") || imgname.contains(".png"))) {
	        			String[] parts = imgname.split(" ");
		        		String[] parts2 = parts[1].split("\\.");
		        		iArray[Integer.parseInt(parts2[0])] = imgname;
	        		}
    	        }
	        	
	        	for (String img : iArray) {
	        		if (img != null) {
	        			imageList.add(img);
	        		}
	        	}
	        }
	        
	    } catch (Exception e) {
	    }
        
	    playButton.setEnabled(true);
	    if (imageList.size() > 0) {
	    	setImages();
	    }
	}
	
	private void playClip(Integer filename) {
		String filePath = currentDir + "/" + filename + ".mp3";
		
		Boolean result = audioAdapter.playAudio(context, "file", 0, null, filePath);
		
		if (result) {
			playButton.setEnabled(false);
			stopButton.setEnabled(true);
		} else {
			
		}
		
	}
	
	private void stopClip() {		
		Boolean result = audioAdapter.stopAudio(1);
		if (result) {
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
		} else {
			
		}
	}
		
	private Integer checkImageLeft(Integer check) {
		if (check == 0) {
			check = imageList.size()-1;
		} else {
			check--;
		}
		
		return check;
	}
	
	private Integer checkImageRight(Integer check) {
		if (check == imageList.size()-1) {
			check = 0;
		} else {
			check++;
		}
		
		return check;
	}
	
	private void setImages() {
		String filepath = currentDir + "/Image/" + imageList.get(currentImg);
		File checkfile = new File(filepath);
		if (checkfile.exists()) {
			bitmapAdapter.loadBitmap(filepath, playerImage);
			
			filepath = currentDir + "/Image/" + imageList.get(checkImageLeft(currentImg));
			bitmapAdapter.loadBitmap(filepath, leftImage);
			
			filepath = currentDir + "/Image/" + imageList.get(checkImageRight(currentImg));
			bitmapAdapter.loadBitmap(filepath, rightImage);
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
		} else {
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}
	
	OnClickListener imageChangeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.leftimage) {
				currentImg = checkImageLeft(currentImg);
			} else if (view.getId() == R.id.rightimage) {
				currentImg = checkImageRight(currentImg);
			}
			
			setImages();
		}            		
	};
	
	OnLongClickListener imgLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final Dialog imageDialog = new Dialog(context);
			View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image, (ViewGroup)context.findViewById(R.id.dialogImageLayout), false);
			ImageView dialogImage = (ImageView)dialogView.findViewById(R.id.dialogImage);
			
			// I remove the file check here because I should hope the file does not get deleted between being displayed and enlarged			
			String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			Bitmap bitmap = bitmapAdapter.getBitmap(filepath);
			dialogImage.setImageBitmap(bitmap);
		
			imageDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			imageDialog.setContentView(dialogView);
			
			// Scaling the popup image to fit the screen
			// I hope there is a simpler way to do this, but I cant think of it so here we are
			// I still hope there is a simpler way of doing this
			View content = context.findViewById(Window.ID_ANDROID_CONTENT);
			float displayWidth = content.getWidth() - 8;
			float displayHeight = content.getHeight() - 8;
			float widthRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
			float heightRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();			
			int ratioedWidth = (int) (displayHeight / heightRatio);
			int ratioedHeight = (int) (displayWidth / widthRatio);
						
			if (displayWidth > bitmap.getWidth() && displayHeight > bitmap.getHeight()) {	
				ViewGroup.LayoutParams dialogParams = dialogView.getLayoutParams();	
	
				if (((Math.round(displayHeight / heightRatio)) > displayWidth && displayWidth > displayHeight) || ((Math.round(displayWidth / widthRatio)) < displayHeight) && displayWidth < displayHeight) {
					dialogParams.width = (int) displayWidth;
					dialogParams.height = ratioedHeight; 
					
				} else {
					dialogParams.height = (int) displayHeight;
					dialogParams.width = ratioedWidth;
				}

				dialogView.setLayoutParams(dialogParams);
			}
			
			// Long press the image to dismiss it
			dialogImage.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					imageDialog.dismiss();
					return true;
				}
				
			});
			
			imageDialog.show();
			
			return true;
		}
		
	};
}
