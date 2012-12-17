
package us.k117.noagendaapp;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class RSSFeedXmlParser {

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
  
    	Log.d(getClass().getName(), "parse");

    	try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            List<Entry> entries = new ArrayList<Entry>();
            
            String currentTag = null;
            String title = null;
            String summary = null;
            String link = null;

            //
            // Loop though each parsing event of the RSS feed
            // 1. Keep track of the start tags
            // 1a. If the start tag is a attribute tag (<name attributeName="attributeValue">) get the attributeValues at START_TAG
            // 1b. If the start tag is a text tag (<name>text</name>) get the text value at TEXT
            // 2. When the end of the item tag is reached create the entry object and add it to the entires array
            //
            while (parser.next() != XmlPullParser.END_DOCUMENT) {

            	// Keep track of the start tags and get xml attributes
              	if (parser.getEventType() == XmlPullParser.START_TAG) {
              		
              		// keep track of the start tag
          			currentTag = parser.getName();
          			
          			// XML attributes
                    if ("enclosure".equals(currentTag)) {
                        link = parser.getAttributeValue(null, "url");
                    }  
                // Get xml text
              	} else if (parser.getEventType() == XmlPullParser.TEXT) {
              		
                    if (parser.getText() != null && !parser.getText().trim().isEmpty() && "title".equals(currentTag)) {
                        title = parser.getText();
                    }
                    if (parser.getText() != null && !parser.getText().trim().isEmpty() && "itunes:subtitle".equals(currentTag)) {
                        summary = parser.getText();
                    }
                // Act when xml tags are closed    
                } else if (parser.getEventType() == XmlPullParser.END_TAG) {

                	// When the end of the item tag is reached then add a new Entry object to the entires array
                    if ("item".equals(parser.getName())) {
                    	entries.add(new Entry(title, summary, link));
                    }
                }
            }
            
            return entries;
            
        } finally {
            in.close();
        }
    }

    // This class represents a single entry (post) in the XML feed.
    public static class Entry {
        public final String title;
        public final String link;
        public final String summary;

        private Entry(String title, String summary, String link) {
            this.title = title;
            this.summary = summary;
            this.link = link;
        }
    }
}
