package nl.martijn.partyagenda;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ExternalStorage {
	public static final String EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory().toString();
	public static final String APP_STORAGE_DIR = EXTERNAL_STORAGE_DIR + "/Android/data/nl.martijn.partyagenda/";
	public static final String ICON_STORAGE_DIR = APP_STORAGE_DIR + "icons/";
	public static final String FLYER_STORAGE_DIR = APP_STORAGE_DIR + "flyers/";
	public static final String FLYER_THUMB_STORAGE_DIR = FLYER_STORAGE_DIR + "thumbs/";
	public static final String VIDEO_THUMB_STORAGE_DIR = APP_STORAGE_DIR + "videos/";
	
	private static final String TAG = "ExternalStorage";
	
	// Open a bitmap from a given path
	// Returns a bitmap on success, null on error
	public static Bitmap openBitmap(String path) {
		return BitmapFactory.decodeFile(path);
	}
	
	/// Save a bitmap on a given path
	// Returns true on success, false on error
	public static boolean saveBitmap(Bitmap bitmap, String path){
		if(bitmap == null)
			return false;
		
		//if(outputFile.exists()) 
    	//	outputFile.delete();
		
		FileOutputStream outputStream;
		try {
        	outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File Not found");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.e(TAG, "IO exception");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
