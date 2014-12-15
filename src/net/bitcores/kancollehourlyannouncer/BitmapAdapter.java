package net.bitcores.kancollehourlyannouncer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

public class BitmapAdapter {
	private static LruCache<String, Bitmap> mMemoryCache = null;
	
	public BitmapAdapter() {
		
	}
	
	public void initBitmapCache() {
		if (mMemoryCache == null) {
			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			final int cacheSize = maxMemory / 8;
				
			mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return bitmap.getByteCount() / 1024;
				}
			};
		}
	}
	
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}
	
	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}
	
	private Bitmap processCache(String filepath) {
		Bitmap bitmap = getBitmapFromMemCache(filepath);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeFile(filepath);
			addBitmapToMemoryCache(filepath, bitmap);
		} 
		
		return bitmap;
	}
	
	public void loadBitmap(String filepath, ImageView imageView) {		
		imageView.setImageBitmap(processCache(filepath));
	}
	
	public Bitmap getBitmap(String filepath) {
		return processCache(filepath);
	}
}
