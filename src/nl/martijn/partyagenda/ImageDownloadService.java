package nl.martijn.partyagenda;

import java.io.File;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class ImageDownloadService extends IntentService {
	
	private static final String TAG = "ImageDownloadService";
	
	private static final String EXTRA_PARAM1 = "nl.martijn.partyagenda.extra.url";
	private static final String EXTRA_PARAM2 = "nl.martijn.partyagenda.extra.path";
	private static final String EXTRA_PARAM3 = "nl.martijn.partyagenda.extra.id";
	private static final String EXTRA_PARAM4 = "nl.martijn.partyagenda.extra.action";

	/**
	 * Starts this service to perform action DownloadImage with the given parameters.
	 * If the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startDownloadingImage(
			Context context, 
			String url, 		// URL of image to download
			String path, 		// Local path to store image
			String id,			// Local image id (file name)
			String action		// Broadcast intent action (callback)
	) {
		Intent intent = new Intent(context, ImageDownloadService.class);
		intent.putExtra(EXTRA_PARAM1, url);
		intent.putExtra(EXTRA_PARAM2, path);
		intent.putExtra(EXTRA_PARAM3, id);
		intent.putExtra(EXTRA_PARAM4, action);
		context.startService(intent);
	}

	/**********************************************************************/
	
	public ImageDownloadService() {
		super("ImageDownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String url = intent.getStringExtra(EXTRA_PARAM1);
			final String path = intent.getStringExtra(EXTRA_PARAM2);
			final String id = intent.getStringExtra(EXTRA_PARAM3);
			final String action = intent.getStringExtra(EXTRA_PARAM4);
			
			downloadImage(url, path, id);
		
			LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
			lbm.sendBroadcast(new Intent(action));
		}
	}

	/**
	 * Handle action DownloadImage in the provided background thread with 
	 * the provided parameters.
	 */
	private void downloadImage(String url, String path, String id) {
		// 1. Download bitmap
		// 2.1. Save bitmap locally
		// 2.2. Store bitmap (icon) in database
		Log.e(TAG, "Downloading image; id = "+id+", url = "+url+", path = "+path);
		
		// 1. Download bitmap
		Bitmap bmp = downloadBitmap(url);
		if(bmp == null)
			return;

		if(!TextUtils.isEmpty(path)) 
		{
			// 2.1. Save bitmap locally
			// Create directories if needed
			File pathFile = new File(path);
			pathFile.mkdirs();
			
			// Save bitmap
			String filePath = path + id + ".jpg";
			Log.e(TAG, "Saving image: "+filePath);
			ExternalStorage.saveBitmap(bmp, filePath);
		}
	}
	
    private Bitmap downloadBitmap(String url) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) { 
            	Log.e("ImageDownloadService", "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent(); 
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();  
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
        	Log.e("ImageDownloadService", "Error while retrieving bitmap from " + url);
        } finally {
        }
        return null;
    }
}
