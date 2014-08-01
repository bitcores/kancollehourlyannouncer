package net.bitcores.kancollehourlyannouncer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//	I may want to put some form of splash screen here at some point, or create a custom layout so that there is no title bar at all
		startService(new Intent(MainActivity.this, AudioService.class));
		startActivity(new Intent(MainActivity.this, PagerActivity.class));
		
	}
	
}
