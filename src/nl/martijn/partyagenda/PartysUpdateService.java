package nl.martijn.partyagenda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class PartysUpdateService extends IntentService {
	
	private static final String TAG = "PartysProvider";
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");;
	
	public PartysUpdateService() {
		super("PartyListUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		PendingIntent pendingIntent = (PendingIntent) intent.getExtras().get("pendingIntent");
		int resultCode = 0;	// Return value: 0 on success, 1 on error
		String errorMessage = "";
		
		/* ******************** Feesten ophalen ******************** */
		// 1. HTTP download data.json
		// 2. Parse JSON
		// 3. Server-ready check
		// 4. Delete all party's that do not exist on the server
		// 5. Check if party N exists
		// 5.1. Yes -> Update party
		// 5.2. No -> Insert new party
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		JSONObject jsonObject = null;
		
		try {
			// 1. HTTP data.json downloaden
			inputStream = OpenHttpConnection("http://content.partyagenda-app.nl/v1/data.json");
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while((line = bufferedReader.readLine()) != null){
            	stringBuilder.append(line + "\n");
            }
            String jString = stringBuilder.toString();	
    		
            // 2. JSON parsen
			jsonObject = new JSONObject(jString.trim());
			
			// 3. Server-ready check
			String status = jsonObject.getString("status").toString().toLowerCase(Locale.US);
			if(status.toLowerCase(Locale.US).equals("error"))
			{
				// Error: Server not ready
				resultCode = 1;
				String message = jsonObject.getString("message").toString();
				errorMessage = "De server meldt: '" + message + "'";
			} 
			else if(status.toLowerCase(Locale.US).equals("ok"))
			{
				JSONArray jsonArray = jsonObject.getJSONArray("list");
				
				// 4. Delete all party's that do not exist on the server
				// 4.1. Determine party ID's on server
				ArrayList<Integer> partyIdsServer = new ArrayList<Integer>(jsonArray.length());
				for(int i = 0; i < jsonArray.length(); i++) {
					JSONObject party = jsonArray.getJSONObject(i);
					int partyId = party.getInt("id");
					partyIdsServer.add(i, partyId);
				}
				
				// 4.2. Determine local party ID
				String[] projection = { PartysContract.Partys._ID };
				Cursor c = getContentResolver().query(PartysContract.Partys.CONTENT_URI, projection, null, null, null);
				ArrayList<Integer> partyIdsLocal = new ArrayList<Integer>(c.getCount());
				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				    int partyId = c.getInt(c.getColumnIndex(PartysContract.Partys._ID));
				    partyIdsLocal.add(c.getPosition(), partyId);
				}
				c.close();
				
				// Delete every party that has it's ID in partyIdsLocal but not in partyIdsServer
				for(Integer localPartyId : partyIdsLocal) {
					if(!partyIdsServer.contains(localPartyId)) {
						Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, localPartyId);
						getContentResolver().delete(partyItemUri, null, null);
					}
				}
				
				// For each party: add or update
				for(int i = 0; i < jsonArray.length(); i++) {				
					JSONObject party = jsonArray.getJSONObject(i);
					
					// Get party attributes
					int id = party.getInt("id");
					String name = party.getString("name").toString();
					String subname = party.getString("subname").toString();
					int popularity = party.getInt("popularity");
					String dateStr = party.getString("date").toString();
					String time = party.getString("time").toString();
					int minimum_age = party.getInt("min_age");
					String price = party.getString("price").toString();
					String venue = party.getString("venue").toString();
					String address = party.getString("address").toString();
					String postcode = party.getString("postcode").toString();
					String city = party.getString("city").toString();
					double latitude = party.getDouble("latitude");
					double longitude = party.getDouble("longitude");
					String genres = party.getString("genres").toString();
					String line_up = party.getString("line_up").toString();
					// get date
					Date dateObj = null;
					long date = 0;
					try {
						dateObj = dateFormatter.parse(dateStr);
						date = dateObj.getTime();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					
					ContentValues values = new ContentValues(14);
					values.put(PartysContract.Partys._ID, id);
					values.put(PartysContract.Partys.NAME, name);
					values.put(PartysContract.Partys.SUBNAME, subname);
					values.put(PartysContract.Partys.POPULARITY, popularity);
					values.put(PartysContract.Partys.DATE, date);
					values.put(PartysContract.Partys.TIME, time);
					values.put(PartysContract.Partys.MINIMUM_AGE, minimum_age);
					values.put(PartysContract.Partys.PRICE, price);
					values.put(PartysContract.Partys.VENUE, venue);
					values.put(PartysContract.Partys.ADDRESS, address);
					values.put(PartysContract.Partys.POSTCODE, postcode);
					values.put(PartysContract.Partys.CITY, city);
					values.put(PartysContract.Partys.LATITUDE, latitude);
					values.put(PartysContract.Partys.LONGITUDE, longitude);
					values.put(PartysContract.Partys.GENRES, genres);
					values.put(PartysContract.Partys.LINE_UP, line_up);
					
					// 5. Check if party N exists locally
				    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, id);
				    c = getContentResolver().query(partyItemUri, projection, null, null, null);
					if(c.getCount() > 0) 
					{
						Log.e(TAG, "Updating party " + id);
						
						// Party already exists locally
						// 5.1. Update party
						String[] selectionArgs = { ""+id };
						getContentResolver().update(PartysContract.Partys.CONTENT_URI, values, PartysContract.Partys._ID + " = ?", selectionArgs);
					} else 
					{
						Log.e(TAG, "Inserting party " + id);
						
						// Party doesn't exist locally
						// 5.2. insert party
						getContentResolver().insert(PartysContract.Partys.CONTENT_URI, values);

				    	// 5.2c. Download flyer thumbnail
						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplication());
						if(sp.getBoolean("automatic_thumb_download", true)) {
							String flyerThumbUrl = "http://content.partyagenda-app.nl/v1/flyers/thumbs/" + id + ".jpg";
							ImageDownloadService.startDownloadingImage(this, flyerThumbUrl, ExternalStorage.FLYER_THUMB_STORAGE_DIR,""+id, "");
						}
					}
					c.close();
					
					////////// ONLINE LINKS //////////
					Uri onlineUri = Uri.withAppendedPath(partyItemUri, "online");
					// 1. Remove existing online links
					getContentResolver().delete(onlineUri, null, null);
					
					// 2. Insert new online links
					JSONArray onlineArray = party.getJSONArray("online");
					for(int j = 0; j < onlineArray.length(); j++){
						JSONObject onlineItem = onlineArray.getJSONObject(j);
						String online_type = onlineItem.getString("type").toString();
						String online_name = onlineItem.getString("name").toString();
						String online_url = onlineItem.getString("url").toString().trim();
						
						if(!online_url.contains("http://") && !online_url.contains("https://"))
							online_url = "http://" + online_url;

						// Insert online link
						values = new ContentValues(3);
						values.put(PartysContract.Partys.Online.TYPE, online_type);
						values.put(PartysContract.Partys.Online.NAME, online_name);
						values.put(PartysContract.Partys.Online.URL, online_url);
						getContentResolver().insert(onlineUri, values);
					}
					
					////////// TICKET LINKS //////////
					Uri ticketsUri = Uri.withAppendedPath(partyItemUri, "online_tickets");
					// 1. Remove existing ticket links
					getContentResolver().delete(ticketsUri, null, null);
					
					// 2. Insert new ticket links
					JSONArray ticketsArray = party.getJSONArray("tickets");
					for(int k = 0; k < ticketsArray.length(); k++){
						JSONObject ticketItem = ticketsArray.getJSONObject(k);
						String ticket_type = ticketItem.getString("type").toString();
						String ticket_name = ticketItem.getString("name").toString();
						String ticket_url = ticketItem.getString("url").toString().trim();
						
						if(!ticket_url.contains("http://") && !ticket_url.contains("https://"))
							ticket_url = "http://" + ticket_url;
						
						// Insert ticket link
						values = new ContentValues(3);
						values.put(PartysContract.Partys.Online.TYPE, ticket_type);
						values.put(PartysContract.Partys.Online.NAME, ticket_name);
						values.put(PartysContract.Partys.Online.URL, ticket_url);
						getContentResolver().insert(ticketsUri, values);
					}
					
					////////// TRAVEL LINKS //////////
					Uri travelUri = Uri.withAppendedPath(partyItemUri, "online_travel");
					// 1. Remove existing ticket links
					getContentResolver().delete(travelUri, null, null);
					
					// 2. Insert new ticket links
					// Store travel URL in database
					JSONArray travelArray = party.getJSONArray("travel");
					for(int l = 0; l < travelArray.length(); l++){
						JSONObject travelItem = travelArray.getJSONObject(l);
						String travel_type = travelItem.getString("type").toString();
						String travel_name = travelItem.getString("name").toString();
						String travel_url = travelItem.getString("url").toString().trim();
						
						if(!travel_url.contains("http://") && !travel_url.contains("https://"))
							travel_url = "http://" + travel_url;
						
						// Insert travel link
						values = new ContentValues(3);
						values.put(PartysContract.Partys.Online.TYPE, travel_type);
						values.put(PartysContract.Partys.Online.NAME, travel_name);
						values.put(PartysContract.Partys.Online.URL, travel_url);
						getContentResolver().insert(travelUri, values);
					}
					
					////////// VIDEO //////////
					Uri videoUri = Uri.withAppendedPath(partyItemUri, "video");
					// 1. Remove existing videos
					getContentResolver().delete(videoUri, null, null);
					// 2. Insert videos
					// Store video in database
					JSONArray videoArray = party.getJSONArray("videos");
					for(int l = 0; l < videoArray.length(); l++){
						JSONObject videoItem = videoArray.getJSONObject(l);
						String youtube_id = videoItem.getString("youtube_id").toString();
						String title = videoItem.getString("title").toString();
						int duration = videoItem.getInt("duration");
						String uploader = videoItem.getString("uploader").toString();
						String uploaded = videoItem.getString("uploaded").toString();
						
						// get date
						dateObj = null;
						long uploadedDate = 0;
						try {
							dateObj = dateFormatter.parse(uploaded);
							uploadedDate = dateObj.getTime();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						
						// Insert video
						values = new ContentValues(5);
						values.put(PartysContract.Partys.Video.YOUTUBE_ID, youtube_id);
						values.put(PartysContract.Partys.Video.TITLE, title);
						values.put(PartysContract.Partys.Video.DURATION, duration);		// Duration as int
						values.put(PartysContract.Partys.Video.UPLOADER, uploader);		// Date as string
						values.put(PartysContract.Partys.Video.UPLOADED, uploadedDate);	
						getContentResolver().insert(videoUri, values);
					}
				}
			}
		} catch (IOException e) {
			resultCode = 1;
			errorMessage = "Kan geen verbinding maken met de server.";
			e.printStackTrace();
		} catch (JSONException e) {
			resultCode = 1;
			errorMessage = "Het resultaat kan niet worden verwerkt. (JSON)";
			e.printStackTrace();
		} finally {
			/* ********** Release objects ********** */
			jsonObject = null;
			
			if(bufferedReader != null){
				try{ bufferedReader.close(); }
				catch (IOException e) {}
				bufferedReader = null; 
			}
			if(inputStreamReader != null){
				try{ inputStreamReader.close(); }
				catch (IOException e) {}
				inputStreamReader = null;
			}
			if(inputStream != null){
				try{ inputStream.close(); }
				catch (IOException e) {}
				inputStream = null;
			}
		}

		if(pendingIntent != null) {
			try {
				Intent dataIntent = new Intent();
				dataIntent.putExtra("errorMessage", errorMessage);
				pendingIntent.send(this, resultCode, dataIntent);
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
	}
	
	private InputStream OpenHttpConnection(String urlString) 
   	     throws IOException
     {
         InputStream in = null;
         int response = -1;
                
         URL url = new URL(urlString); 
         URLConnection conn = url.openConnection();
                  
         if (!(conn instanceof HttpURLConnection))                     
             throw new IOException("Not an HTTP connection");
         
         try{
             HttpURLConnection httpConn = (HttpURLConnection) conn;
             httpConn.setAllowUserInteraction(false);
             httpConn.setReadTimeout(6000);
             httpConn.setConnectTimeout(5000);
             httpConn.setInstanceFollowRedirects(true);
             httpConn.setRequestMethod("GET");
             httpConn.connect(); 

            response = httpConn.getResponseCode();                 
             if (response == HttpURLConnection.HTTP_OK) {
                 in = httpConn.getInputStream();
             }                     
         }
         catch (Exception ex)
         {
             throw new IOException("Error connecting");            
         }
         
         if(in == null)
        	 throw new IOException("Error connecting"); 
         
         return in;     
     }
}
