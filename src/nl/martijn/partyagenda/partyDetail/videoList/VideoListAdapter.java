package nl.martijn.partyagenda.partyDetail.videoList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nl.martijn.partyagenda.ExternalStorage;
import nl.martijn.partyagenda.ExternalStorageCached;
import nl.martijn.partyagenda.ImageDownloadService;
import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoListAdapter extends CursorAdapter {

	// Content of this list adapter
	// * Cached thumbnails
	
	/********** Variables **********/
	
	private SimpleDateFormat mDateFormater;
	private ExternalStorageCached mCachedThumbStorage = null;
	
	/********** Functions **********/
	
	// Constructor
	public VideoListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);	
		mDateFormater = new SimpleDateFormat("d MMM yyyy");
		
		// Register for broadcast messages:
		//	Sent when a thumbnail has been downloaded
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
		lbm.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				notifyDataSetChanged();
			}
		}, new IntentFilter("video"));
		
		// 
		mCachedThumbStorage = new ExternalStorageCached();
	}
	
	// New view
	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		View rowView = null;
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rowView = layoutInflater.inflate(R.layout.video_list_item, parent, false);
		return rowView;
	}
	
	// Bind view
	@Override
	public void bindView(View v, Context context, Cursor c) {
		// UI elements
		ImageView thumbImageView = (ImageView)v.findViewById(R.id.video_list_item_thumb);
		TextView titleTextView = (TextView)v.findViewById(R.id.video_list_item_title);
		TextView durationTextView = (TextView)v.findViewById(R.id.video_list_item_duration);
		TextView uploaderTextView = (TextView)v.findViewById(R.id.video_list_item_uploader);
		TextView uploadedTextView = (TextView)v.findViewById(R.id.video_list_item_uploaded);
		
		// Data
		String youtubeId = c.getString(c.getColumnIndex(PartysContract.Partys.Video.YOUTUBE_ID));
		String title = c.getString(c.getColumnIndex(PartysContract.Partys.Video.TITLE));
		String uploader = c.getString(c.getColumnIndex(PartysContract.Partys.Video.UPLOADER));
		int duration = c.getInt(c.getColumnIndex(PartysContract.Partys.Video.DURATION));
		long uploaded = c.getLong(c.getColumnIndex(PartysContract.Partys.Video.UPLOADED));
		
		// Uploaded date
		Calendar uploadedDate = new GregorianCalendar();
		uploadedDate.setTimeInMillis(uploaded);
		String uploadedStr = mDateFormater.format(uploadedDate.getTime());
		
		// Duration
		String durationStr = durationToString(duration);

		// Set values
		titleTextView.setText(title);
		durationTextView.setText(durationStr);
		uploaderTextView.setText(uploader);
		uploadedTextView.setText(uploadedStr);
		
		Bitmap thumb = mCachedThumbStorage.openBitmap(ExternalStorage.VIDEO_THUMB_STORAGE_DIR + youtubeId + ".jpg");
		if(thumb != null) {
			thumbImageView.setImageBitmap(thumb);
		} else {
			String thumbUrl = "http://i.ytimg.com/vi/"+youtubeId+"/default.jpg";
	    	ImageDownloadService.startDownloadingImage(mContext, thumbUrl, ExternalStorage.VIDEO_THUMB_STORAGE_DIR, youtubeId, "video");
		}
	}
	
	private String durationToString(int duration) {
		if(duration == 0)
			return "00:00";
		
		int hours, minutes, seconds;
		minutes = duration / 60;
		if(minutes > 60) {
			hours = minutes / 60;
			minutes = minutes - (hours * 60);
			seconds = duration - (hours * (60*60)) - (minutes * 60);
			return hours+":"+String.format("%02d",minutes)+":"+String.format("%02d",seconds);
		} else {
			seconds = duration - (minutes * 60);
			return minutes+":"+String.format("%02d",seconds);
		}
	}
}
