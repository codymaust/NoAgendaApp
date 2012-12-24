package us.k117.noagendaapp.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.util.Log;

import us.k117.noagendaapp.R;
import us.k117.noagendaapp.R.id;
import us.k117.noagendaapp.R.layout;
import us.k117.noagendaapp.R.string;
import us.k117.noagendaapp.rss.RSSFeedXmlParser;
import us.k117.noagendaapp.rss.RSSFeedXmlParser.Entry;

public class ShowsFragment extends Fragment {

    private static final String URL = "http://feed.nashownotes.com/rss.xml";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Test", "hello");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_shows, container, false);
		//TextView textView = (TextView) view.findViewById(R.id.showsText);
		//textView.setText("Shows");
		loadPage();
		return view;
	}
	
	//
	// Stuff for RSS Parsing Below
	//
    private void loadPage() {
            // AsyncTask subclass
    		Log.d(getClass().getName(), "loadPage()");
            new DownloadRSSTask().execute(URL);
    }
    
    // Implementation of AsyncTask used to download RSS feed
    private class DownloadRSSTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadRSSFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
        	
        	Log.d(getClass().getName(), "onPostExecute:" + result);
            // Displays the HTML string in the UI via a WebView
            WebView myWebView = (WebView) getView().findViewById(R.id.webview);
            myWebView.loadData(result, "text/html", null);
        }
    }
    
    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private String loadRSSFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        RSSFeedXmlParser rssFeedXmlParser = new RSSFeedXmlParser();
        List<Entry> entries = null;

        Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>No Agenda RSS Feed</h3>");
        htmlString.append("<em>Last Updated: " + formatter.format(rightNow.getTime()) + "</em>");
        
        try {
            stream = downloadUrl(urlString);
            entries = rssFeedXmlParser.parse(stream);
        // Makes sure that the InputStream is closed after the app is finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        
        // This section processes the entries list to combine each entry with HTML markup.
        // Each entry is displayed in the UI as a link that optionally includes
        // a text summary.
        for (Entry entry : entries) {
            htmlString.append("<p><a href='");
            htmlString.append(entry.link);
            htmlString.append("'>" + entry.title + "</a><br/>");
            htmlString.append(entry.summary + "</p>");
        }
        
        return htmlString.toString();
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }  
}
