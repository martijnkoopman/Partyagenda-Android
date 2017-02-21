package nl.martijn.partyagenda.partyList;

import nl.martijn.partyagenda.PartyApp;
import nl.martijn.partyagenda.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        // Action bar
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);
        
		// Privacy link
		TextView privacyLink = (TextView) findViewById(R.id.privacy_link);
	    privacyLink.setMovementMethod(LinkMovementMethod.getInstance());
	    privacyLink.setText(Html.fromHtml("<a href=\"http://www.partyagenda-app.nl/privacy.html\">Privacyverklaring</a>"));
	    
		// License link
		TextView licenseLink = (TextView) findViewById(R.id.license_link);
		licenseLink.setMovementMethod(LinkMovementMethod.getInstance());
		licenseLink.setText(Html.fromHtml("<a href=\"http://www.partyagenda-app.nl/licentie.html\">Google Maps licentie</a>"));
	    
		// Feedback button
        Button feedbackButton = (Button)findViewById(R.id.about_feedback);
        feedbackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setType("text/email");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "info@partyagenda-app.nl" });
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Partyagenda app feedback");
				if (getPackageManager().queryIntentActivities(emailIntent, 0).size() > 0)
		        {
					startActivity(Intent.createChooser(emailIntent, "Jouw Feedback"));
		        }
			}
		});
        
        // Review button
        Button reviewButton = (Button)findViewById(R.id.about_review);
        reviewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
		        Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);
		        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
		        {
		            startActivity(rateAppIntent);
		            PartyApp app = (PartyApp)getApplication();
		            app.setAppRated(true);
		        }
			}
		});
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
