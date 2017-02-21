package nl.martijn.partyagenda;

import java.util.HashMap;
import android.graphics.Bitmap;


public class ExternalStorageCached {

	private HashMap<String, Bitmap> cachedBitmaps = new HashMap<String, Bitmap>();

	public ExternalStorageCached(){}
	
	// Open a bitmap
	public Bitmap openBitmap(String path) {
		// In cache: load from cache
		Bitmap cachedBitmap = cachedBitmaps.get(path);
		if(cachedBitmap != null)
		{
			return cachedBitmap;
		} 
		else 
		{
			// Not in cache: load from disk
			Bitmap bmp = ExternalStorage.openBitmap(path);
			if(bmp != null)
				// Store in cache
				cachedBitmaps.put(path, bmp);
			return bmp;
		}
	}

	// Save a bitmap
	public boolean saveBitmap(Bitmap bitmap, String path) {
		// Store in cache
		if(path != null && bitmap != null)
			cachedBitmaps.put(path, bitmap);

		// Save on disk
		return ExternalStorage.saveBitmap(bitmap, path);
	}

	
	// Clear all bitmaps
	public void clearCache()
	{
		cachedBitmaps.clear();
	}
}