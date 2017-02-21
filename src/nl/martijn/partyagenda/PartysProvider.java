package nl.martijn.partyagenda;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class PartysProvider extends ContentProvider {
	//private static final String TAG = "PartysProvider";
	
	// ---------- Database ----------
    private static final String DATABASE_NAME = "PartyDatabase";
    private static final int DATABASE_VERSION = 3;
	
    private static final String PARTY_TABLE_CREATE =
        "CREATE TABLE `"+PartysContract.Partys.TABLE_NAME+"` ( `"+PartysContract.Partys._ID+"` INTEGER primary key, " +
        "`"+PartysContract.Partys.NAME+"` TEXT, `"+PartysContract.Partys.SUBNAME+"` TEXT, `"+PartysContract.Partys.POPULARITY+"` INT, " + 
        "`"+PartysContract.Partys.DATE+"` LONG, `"+PartysContract.Partys.TIME+"` TEXT, `"+PartysContract.Partys.MINIMUM_AGE+"` INT, " + 
        "`"+PartysContract.Partys.PRICE+"` TEXT, `"+PartysContract.Partys.VENUE+"` TEXT, `"+PartysContract.Partys.ADDRESS+"` TEXT, " + 
        "`"+PartysContract.Partys.POSTCODE+"` TEXT, `"+PartysContract.Partys.CITY+"` TEXT, `"+PartysContract.Partys.LATITUDE+"` REAL, " + 
        "`"+PartysContract.Partys.LONGITUDE+"` REAL, `"+PartysContract.Partys.GENRES+"` TEXT, `"+PartysContract.Partys.LINE_UP+"` TEXT, " +
        "`"+PartysContract.Partys.IS_FAVORITE+"` INT)";

    private static final String ONLINE_TABLE_CREATE = "CREATE TABLE `"+PartysContract.Partys.Online.TABLE_NAME+"` ( `"+PartysContract.Partys.Online._ID+"` INTEGER primary key autoincrement, " +
        "`"+PartysContract.Partys.Online.PARTY_ID+"` INTEGER REFERENCES "+PartysContract.Partys.TABLE_NAME+"("+PartysContract.Partys._ID+") ON DELETE CASCADE, " + 
        "`"+PartysContract.Partys.Online.TYPE+"` TEXT, `"+PartysContract.Partys.Online.NAME+"` TEXT, `"+PartysContract.Partys.Online.URL+"` TEXT);";
    private static final String TICKETS_TABLE_CREATE = "CREATE TABLE `"+PartysContract.Partys.Tickets.TABLE_NAME+"` ( `"+PartysContract.Partys.Tickets._ID+"` INTEGER primary key autoincrement, " +
            "`"+PartysContract.Partys.Tickets.PARTY_ID+"` INTEGER REFERENCES "+PartysContract.Partys.TABLE_NAME+"("+PartysContract.Partys._ID+") ON DELETE CASCADE, " + 
            "`"+PartysContract.Partys.Tickets.TYPE+"` TEXT, `"+PartysContract.Partys.Tickets.NAME+"` TEXT, `"+PartysContract.Partys.Tickets.URL+"` TEXT);";
    private static final String TRAVEL_TABLE_CREATE = "CREATE TABLE `"+PartysContract.Partys.Travel.TABLE_NAME+"` ( `"+PartysContract.Partys.Travel._ID+"` INTEGER primary key autoincrement, " +
            "`"+PartysContract.Partys.Travel.PARTY_ID+"` INTEGER REFERENCES "+PartysContract.Partys.TABLE_NAME+"("+PartysContract.Partys._ID+") ON DELETE CASCADE, " + 
            "`"+PartysContract.Partys.Travel.TYPE+"` TEXT, `"+PartysContract.Partys.Travel.NAME+"` TEXT, `"+PartysContract.Partys.Travel.URL+"` TEXT);";
    
    private static final String VIDEO_TABLE_CREATE = "CREATE TABLE `"+PartysContract.Partys.Video.TABLE_NAME+"` ( `"+PartysContract.Partys.Video._ID+"` INTEGER primary key autoincrement, " +
        	"`"+PartysContract.Partys.Video.PARTY_ID+"` INTEGER REFERENCES "+PartysContract.Partys.TABLE_NAME+"("+PartysContract.Partys._ID+") ON DELETE CASCADE, " +
    		"`"+PartysContract.Partys.Video.YOUTUBE_ID+"` TEXT, `"+PartysContract.Partys.Video.TITLE+"` TEXT, `"+PartysContract.Partys.Video.DURATION+"` INTEGER, " +
        	"`"+PartysContract.Partys.Video.UPLOADER+"` TEXT, `"+PartysContract.Partys.Video.UPLOADED+"` LONG);";
    
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDb;
	
	// ---------- URI Matcher ----------
	private static final int PARTYS = 1;
	private static final int PARTYS_ID = 2;
	private static final int ONLINE = 3;
	private static final int ONLINE_ID = 4;
	private static final int TICKETS = 5;
	private static final int TICKETS_ID = 6;
	private static final int TRAVEL = 7;
	private static final int TRAVEL_ID = 8;
	private static final int VIDEO = 9;
	private static final int VIDEO_ID = 10;
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);;
	static {
		sUriMatcher.addURI(PartysContract.AUTHORITY, "partys", PARTYS);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#", PARTYS_ID);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Online.TABLE_NAME, ONLINE);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Online.TABLE_NAME+"/#", ONLINE_ID);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Tickets.TABLE_NAME, TICKETS);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Tickets.TABLE_NAME+"/#", TICKETS_ID);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Travel.TABLE_NAME, TRAVEL);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Travel.TABLE_NAME+"/#", TRAVEL_ID);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Video.TABLE_NAME, VIDEO);
    	sUriMatcher.addURI(PartysContract.AUTHORITY, "partys/#/"+PartysContract.Partys.Video.TABLE_NAME+"/#", VIDEO_ID);
	}
	
	// ---------- Methods ----------
	@Override
	public boolean onCreate() {
		// Create a new database helper object
		mDatabaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		return true;
	}
	
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		//Log.e(TAG, "Create");
		//Log.e(TAG, "URI = " + uri.toString());
		
		mDb = mDatabaseHelper.getWritableDatabase();
		
		long id = 0;
		String partyId;
		switch(sUriMatcher.match(uri))
		{
		case PARTYS:
			// content://nl.martijn.partyagenda.provider/partys
			// Insert party
			id = mDb.insert(PartysContract.Partys.TABLE_NAME, null, values);
			if (id > 0) {
				Uri itemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(itemUri, null);
				return itemUri;
			}
			return null;
		case ONLINE:
			// content://nl.martijn.partyagenda.provider/partys/#/online
			// Insert online link
			partyId = uri.getPathSegments().get(1);
			values.put(PartysContract.Partys.Online.PARTY_ID, partyId);
			id = mDb.insert(PartysContract.Partys.Online.TABLE_NAME, null, values);
			if (id > 0) {
				Uri itemUri = ContentUris.withAppendedId(PartysContract.Partys.Online.CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(itemUri, null);
				return itemUri;
			} 
			return null;
		case TICKETS:
			// content://nl.martijn.partyagenda.provider/partys/#/online_tickets
			// Insert ticket link
			partyId = uri.getPathSegments().get(1);
			values.put(PartysContract.Partys.Tickets.PARTY_ID, partyId);
			id = mDb.insert(PartysContract.Partys.Tickets.TABLE_NAME, null, values);
			if (id > 0) {
				Uri itemUri = ContentUris.withAppendedId(PartysContract.Partys.Tickets.CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(itemUri, null);
				return itemUri;
			} 
			return null;
		case TRAVEL:
			// content://nl.martijn.partyagenda.provider/partys/#/online_travel
			// Insert travel link
			partyId = uri.getPathSegments().get(1);
			values.put(PartysContract.Partys.Travel.PARTY_ID, partyId);
			id = mDb.insert(PartysContract.Partys.Travel.TABLE_NAME, null, values);
			if (id > 0) {
				Uri itemUri = ContentUris.withAppendedId(PartysContract.Partys.Travel.CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(itemUri, null);
				return itemUri;
			} 
			return null;
		case VIDEO:
			// content://nl.martijn.partyagenda.provider/partys/#/video
			// Insert youtube video
			partyId = uri.getPathSegments().get(1);
			values.put(PartysContract.Partys.Video.PARTY_ID, partyId);
			id = mDb.insert(PartysContract.Partys.Video.TABLE_NAME, null, values);
			if (id > 0) {
				Uri itemUri = ContentUris.withAppendedId(PartysContract.Partys.Video.CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(itemUri, null);
				return itemUri;
			}
			return null;
		default:
			return null;
		}
	}
	
	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		//Log.e(TAG, "Update");
		//Log.e(TAG, "URI = " + uri.toString());
		
		mDb = mDatabaseHelper.getWritableDatabase();
		
		int rowsAffected = 0;
		switch(sUriMatcher.match(uri))
		{
		case PARTYS: case PARTYS_ID:
			// content://nl.martijn.partyagenda.provider/partys/#
			// Update party
			rowsAffected = mDb.update(PartysContract.Partys.TABLE_NAME, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);  // ???
			break;
		case ONLINE: case ONLINE_ID:
			// Update online link
			break;
		case TICKETS: case TICKETS_ID:
			// Update ticket link
			break;
		case TRAVEL: case TRAVEL_ID:
			// Update travel link
			break;
		case VIDEO: case VIDEO_ID:
			// Update youtube video
			break;
		default:
			break;
		}
		return rowsAffected;
	}
	
	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		// TODO: Throw exception or return null on inernal error
		
		mDb = mDatabaseHelper.getWritableDatabase();
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		Cursor cursor = null;
		String partyId;
		String onlineId;
		
		switch(sUriMatcher.match(uri))
		{
		case PARTYS:
			// content://nl.martijn.partyagenda.provider/partys
			// Read all party's
	        qBuilder.setTables(PartysContract.Partys.TABLE_NAME);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								sortOrder);
	        cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
	        return cursor;
		case PARTYS_ID:
			// content://nl.martijn.partyagenda.provider/partys/#
			// Read a single party
	        qBuilder.setTables(PartysContract.Partys.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys._ID + " = " + uri.getLastPathSegment());
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.moveToFirst();
	        //cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
	        return cursor;
		case ONLINE:
			// content://nl.martijn.partyagenda.provider/partys/#/online
			// Read all online links for a party
			partyId = uri.getPathSegments().get(1);
	        qBuilder.setTables(PartysContract.Partys.Online.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Online.PARTY_ID + " = " + partyId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
	        return cursor;
		case ONLINE_ID:
			// content://nl.martijn.partyagenda.provider/partys/#/online/#
			// Read a single online link
			partyId = uri.getPathSegments().get(1);
			onlineId =  uri.getPathSegments().get(3);	
			qBuilder.setTables(PartysContract.Partys.Online.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Online._ID + " = " + onlineId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.moveToFirst();
	        //cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
			return cursor;
		case TICKETS:
			// content://nl.martijn.partyagenda.provider/partys/#/online_tickets
			// Read all ticket links for a party
			partyId = uri.getPathSegments().get(1);
	        qBuilder.setTables(PartysContract.Partys.Tickets.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Tickets.PARTY_ID + " = " + partyId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
	        return cursor;
		case TICKETS_ID:
			// content://nl.martijn.partyagenda.provider/partys/#/online_tickets/#
			// Read a single ticket link
			partyId = uri.getPathSegments().get(1);
			onlineId =  uri.getPathSegments().get(3);	
			qBuilder.setTables(PartysContract.Partys.Tickets.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Tickets._ID + " = " + onlineId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.moveToFirst();
	        //cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
			return cursor;
		case TRAVEL:
			// content://nl.martijn.partyagenda.provider/partys/#/online_travel
			// Read all travel links for a party
			partyId = uri.getPathSegments().get(1);
	        qBuilder.setTables(PartysContract.Partys.Travel.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Travel.PARTY_ID + " = " + partyId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
	        return cursor;
		case TRAVEL_ID:
			// content://nl.martijn.partyagenda.provider/partys/#/online_travel/#
			// Read a single travel link
			partyId = uri.getPathSegments().get(1);
			onlineId =  uri.getPathSegments().get(3);	
			qBuilder.setTables(PartysContract.Partys.Travel.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Travel._ID + " = " + onlineId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.moveToFirst();
	        //cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
			return cursor;
		case VIDEO:
			// content://nl.martijn.partyagenda.provider/partys/#/video
			// Read all youtube video's for a party
			partyId = uri.getPathSegments().get(1);
	        qBuilder.setTables(PartysContract.Partys.Video.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Video.PARTY_ID + " = " + partyId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
	        return cursor;
		case VIDEO_ID:
			// content://nl.martijn.partyagenda.provider/partys/#/video/#
			// Read a single youtube video
			partyId = uri.getPathSegments().get(1);
			String videoId =  uri.getPathSegments().get(3);	
			qBuilder.setTables(PartysContract.Partys.Video.TABLE_NAME);
	        qBuilder.appendWhere(PartysContract.Partys.Video._ID + " = " + videoId);
	        cursor = qBuilder.query(mDb,
	                				projection,
                					selection,
            						selectionArgs,
        							null,
    								null,
    								null);
	        cursor.moveToFirst();
	        //cursor.setNotificationUri(getContext().getContentResolver(), uri);	// ???
			return cursor;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}
	
	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) 
	{	
		mDb = mDatabaseHelper.getWritableDatabase();
		int numAffectedRows = 0;
		String[] whereArgs;
		
		switch(sUriMatcher.match(uri))
		{
		case PARTYS:
			// content://nl.martijn.partyagenda.provider/partys
			// Delete all party's
			numAffectedRows = mDb.delete(PartysContract.Partys.TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(PartysContract.Partys.CONTENT_URI, null);
			break;
		case PARTYS_ID:
			// content://nl.martijn.partyagenda.provider/partys/#
			// Delete a single party
			// Note: online links and video's are deleted through the foreign key reference action DELETE CASCADE
			whereArgs = new String[] { uri.getLastPathSegment() };
			numAffectedRows = mDb.delete(PartysContract.Partys.TABLE_NAME, PartysContract.Partys._ID + " = ?", whereArgs);
			getContext().getContentResolver().notifyChange(PartysContract.Partys.CONTENT_URI, null);
			break;
		case ONLINE:
			// content://nl.martijn.partyagenda.provider/partys/#/online
			// Delete all online links for a party
			whereArgs = new String[] { uri.getPathSegments().get(1) };
			numAffectedRows = mDb.delete(PartysContract.Partys.Online.TABLE_NAME, PartysContract.Partys.Online.PARTY_ID + " = ?", whereArgs);
			getContext().getContentResolver().notifyChange(PartysContract.Partys.Online.CONTENT_URI, null);
			break;
		case ONLINE_ID:
			// Delete a single online link
			break;
		case TICKETS:
			// content://nl.martijn.partyagenda.provider/partys/#/online_tickets
			// Delete all ticket links for a party
			whereArgs = new String[] { uri.getPathSegments().get(1) };
			numAffectedRows = mDb.delete(PartysContract.Partys.Tickets.TABLE_NAME, PartysContract.Partys.Tickets.PARTY_ID + " = ?", whereArgs);
			getContext().getContentResolver().notifyChange(PartysContract.Partys.Tickets.CONTENT_URI, null);
			break;
		case TICKETS_ID:
			// Delete a single ticket link
			break;
		case TRAVEL:
			// content://nl.martijn.partyagenda.provider/partys/#/online_travel
			// Delete all travel links for a party
			whereArgs = new String[] { uri.getPathSegments().get(1) };
			numAffectedRows = mDb.delete(PartysContract.Partys.Travel.TABLE_NAME, PartysContract.Partys.Travel.PARTY_ID + " = ?", whereArgs);
			getContext().getContentResolver().notifyChange(PartysContract.Partys.Travel.CONTENT_URI, null);
			break;
		case TRAVEL_ID:
			// Delete a single travel link
			break;
		case VIDEO:
			// content://nl.martijn.partyagenda.provider/partys/#/video
			// Delete all youtube video's for a party
			whereArgs = new String[] { uri.getPathSegments().get(1) };
			numAffectedRows = mDb.delete(PartysContract.Partys.Video.TABLE_NAME, PartysContract.Partys.Video.PARTY_ID + " = ?", whereArgs);
			getContext().getContentResolver().notifyChange(PartysContract.Partys.Video.CONTENT_URI, null);
			break;
		case VIDEO_ID:
			// Delete a single youtube video
			break;
		default:
			break;
		}

		return numAffectedRows;
	}

	@Override
	public synchronized String getType(Uri uri) {
        switch (sUriMatcher.match(uri))
        {
            case PARTYS:
                return "vnd.android.cursor.dir/party";
            case PARTYS_ID:
                return "vnd.android.cursor.item/party";
    		case ONLINE:
    			return "vnd.android.cursor.dir/online";
    		case ONLINE_ID:
    			return "vnd.android.cursor.item/online";
    		case TICKETS:
    			return "vnd.android.cursor.dir/tickets";
    		case TICKETS_ID:
    			return "vnd.android.cursor.item/ticket";
    		case TRAVEL:
    			return "vnd.android.cursor.dir/travel";
    		case TRAVEL_ID:
    			return "vnd.android.cursor.item/travel";
    		case VIDEO:
    			return "vnd.android.cursor.dir/video";
    		case VIDEO_ID:
    			return "vnd.android.cursor.item/video";
            default:
                return null;
        }
	}

	/* ************************************* */
	/* ********** Database Helper ********** */
	/* ************************************* */
	
	protected static final class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("PRAGMA foreign_keys = ON;");
			
			// Create tables
			db.execSQL(PARTY_TABLE_CREATE);
			db.execSQL(ONLINE_TABLE_CREATE);
			db.execSQL(TICKETS_TABLE_CREATE);
			db.execSQL(TRAVEL_TABLE_CREATE);
			db.execSQL(VIDEO_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Log.w(TAG, "Upgrading database from version " + oldVersion 
            //        + " to "
            //        + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+PartysContract.Partys.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+PartysContract.Partys.Online.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+PartysContract.Partys.Tickets.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+PartysContract.Partys.Travel.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+PartysContract.Partys.Video.TABLE_NAME);
			onCreate(db);
		}
	}
}
