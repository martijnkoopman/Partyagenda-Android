package nl.martijn.partyagenda.partyDetail;

import nl.martijn.partyagenda.ExternalStorage;
import nl.martijn.partyagenda.ImageDownloadService;
import nl.martijn.partyagenda.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PartyFlyerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		setContentView(R.layout.activity_party_flyer);
		
		// Get party id
		final int partyId = getIntent().getExtras().getInt("id");
		final String partyName = getIntent().getExtras().getString("name");
		
		// Register for message when flyer is loaded
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(loadFlyerFromFile(partyId) == null) {
					// Show error message
					ProgressBar loadingSign = (ProgressBar)findViewById(R.id.party_flyer_loading_sign);
					loadingSign.setVisibility(View.GONE);
					TextView flyerTextView = (TextView)findViewById(R.id.party_flyer_text);
					flyerTextView.setText("Flyer kan niet geladen worden.");
				}
			}
		}, new IntentFilter("flyer"));
		
		// Load and show flyer
		if(loadFlyerFromFile(partyId) == null) {
			downloadFlyer(partyId);
		}

		// Save button
		Button saveButton = (Button)findViewById(R.id.party_flyer_save);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String filePath = ExternalStorage.FLYER_STORAGE_DIR+partyId+".jpg";
				ContentValues values = new ContentValues(); 
			    values.put( Media.TITLE, partyName ); 
			    values.put( Images.Media.DATE_TAKEN, System.currentTimeMillis() );
			    values.put( Images.Media.BUCKET_ID, filePath.hashCode() );
			    values.put( Images.Media.BUCKET_DISPLAY_NAME, "Partyagenda Flyers" );

			    values.put( Images.Media.MIME_TYPE, "image/jpeg" );
			    values.put( Media.DESCRIPTION, partyName ); 
			    values.put( MediaStore.MediaColumns.DATA, filePath );
			    getContentResolver().insert( Media.EXTERNAL_CONTENT_URI , values );
			    Toast.makeText(PartyFlyerActivity.this, "Flyer opgeslagen in gallerij.", Toast.LENGTH_SHORT).show();
			    
				/*
				try {
					MediaStore.Images.Media.insertImage(getContentResolver(), ExternalStorage.FLYER_STORAGE_DIR+partyId+".jpg", partyName, "Flyer");
    				Toast.makeText(PartyFlyerActivity.this, "Flyer opgeslagen in gallerij.", Toast.LENGTH_SHORT).show();
				} catch (FileNotFoundException e) {
					Toast.makeText(PartyFlyerActivity.this, "Flyer kan niet opgeslagen worden.", Toast.LENGTH_SHORT).show();
				}
				*/
			}
		});
	}
	
	private Bitmap loadFlyerFromFile(int partyId) {
		// Show flyer from downloaded file
		ImageView flyerImageView = (ImageView)findViewById(R.id.party_flyer);
		ProgressBar loadingSign = (ProgressBar)findViewById(R.id.party_flyer_loading_sign);
		TextView flyerTextView = (TextView)findViewById(R.id.party_flyer_text);
		Button saveButton = (Button)findViewById(R.id.party_flyer_save);
		
		Bitmap flyerBitmap = ExternalStorage.openBitmap(ExternalStorage.FLYER_STORAGE_DIR + partyId + ".jpg");
		if(flyerBitmap != null) {
			loadingSign.setVisibility(View.GONE);
			flyerImageView.setImageBitmap(flyerBitmap);
			flyerTextView.setVisibility(View.GONE);
			saveButton.setVisibility(View.VISIBLE);
		}
		return flyerBitmap;
	}

	private void downloadFlyer(int partyId) {
		if(loadFlyerFromFile(partyId) == null) 
		{
			// Download flyer
			String flyerUrl = "http://content.partyagenda-app.nl/v1/flyers/"+partyId+".jpg";
	    	ImageDownloadService.startDownloadingImage(this, flyerUrl, ExternalStorage.FLYER_STORAGE_DIR, ""+partyId, "flyer");
		}
	}
	
	
	
}
