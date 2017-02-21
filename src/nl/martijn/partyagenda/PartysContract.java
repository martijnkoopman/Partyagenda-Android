package nl.martijn.partyagenda;

import android.net.Uri;

public final class PartysContract {
	/*
	 * - partys
	 * 	-> online
	 * 	-> tickets
	 *	-> travel
	 *	-> media
	 */
	
	public static final String AUTHORITY = "nl.martijn.partyagenda.provider";
	
	public static interface Partys {
		public static final String TABLE_NAME = "partys";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/partys");
		public static final Uri CONTENT_URI_ID = Uri.parse("content://" + AUTHORITY + "/partys/");
		public static final String _ID = "_id";
		public static final String NAME = "name";
		public static final String SUBNAME = "subname";
		public static final String POPULARITY = "popularity";
		public static final String DATE = "date";
		public static final String TIME = "time";
		public static final String MINIMUM_AGE = "minimum_age";
		public static final String PRICE = "price";
		public static final String VENUE = "venue";
		public static final String ADDRESS = "address";
		public static final String POSTCODE = "postcode";
		public static final String CITY = "city";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String GENRES = "genres";
		public static final String LINE_UP = "line_up";
		public static final String IS_FAVORITE = "is_favorite";
		
		public static interface Online {
			public static final String TABLE_NAME = "online";
			public static final Uri CONTENT_URI = Uri.parse(Partys.CONTENT_URI_ID  + "online");	// #/online ???
			public static final String _ID = "_id";
			public static final String PARTY_ID = "party_id";
			public static final String TYPE = "type";
			public static final String NAME = "name";
			public static final String URL = "url";
		}
		
		public static interface Tickets extends Online {
			public static final String TABLE_NAME = "online_tickets";
			public static final Uri CONTENT_URI = Uri.parse(Partys.CONTENT_URI  + "/tickets");	// #???
		}
		
		public static interface Travel extends Online {
			public static final String TABLE_NAME = "online_travel";
			public static final Uri CONTENT_URI = Uri.parse(Partys.CONTENT_URI  + "/travel");	// #???
		}
		
		public static interface Video {
			public static final String TABLE_NAME = "video";
			public static final Uri CONTENT_URI = Uri.parse(Partys.CONTENT_URI  + "/video");	// #???
			public static final String _ID = "_id";
			public static final String PARTY_ID = "party_id";
			public static final String YOUTUBE_ID = "youtube_id";
			public static final String TITLE = "title";
			public static final String DURATION = "duration";
			public static final String UPLOADER = "uploader";
			public static final String UPLOADED = "uploaded";
		}
	    	
		//public static interface Media {
		//	public static final String TABLE_NAME = "media";
		//	public static final Uri CONTENT_URI = Uri.parse(Partys.CONTENT_URI  + "/media");	// #???
		//	public static final String _ID = "_id";
		//	public static final String PARTY_ID = "party_id";
		//	public static final String TYPE = "type";
		//	public static final String NAME = "name";
		//	public static final String THUMBNAIL = "thumbnail";
		//	public static final String URL = "url";
		//}
	}
}
