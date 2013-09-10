/*
Copyright (c) 2013, Kevin Coakley <kevin@k117.us>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.noagendaapp.rss;

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
            String length = null;
            String date = null;
            String episodeNum = null;

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
                        
                        // Parse the title on - and get the episode number, the second element "NA-117-1978-11-07"
                        try {
                        	String[] titleArray = title.split("-");
                        	episodeNum = titleArray[1];
                        } catch (Exception ex) {
                        	// TODO: Do something with this exception other than just assign the var ERR
                        	episodeNum = "ERR";
                        }
                    }
                    if (parser.getText() != null && !parser.getText().trim().isEmpty() && "itunes:subtitle".equals(currentTag)) {
                        summary = parser.getText();
                    }
                    if (parser.getText() != null && !parser.getText().trim().isEmpty() && "pubDate".equals(currentTag)) {
                        date = parser.getText();
                    }
                    if (parser.getText() != null && !parser.getText().trim().isEmpty() && "itunes:duration".equals(currentTag)) {
                    	length = parser.getText();
                    }
                // Act when xml tags are closed    
                } else if (parser.getEventType() == XmlPullParser.END_TAG) {

                	// When the end of the item tag is reached then add a new Entry object to the entires array
                    if ("item".equals(parser.getName())) {
                    	entries.add(new Entry(title, summary, link, length, date, episodeNum));
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
        public final String length;
        public final String date;
        public final String episodeNum;

        private Entry(String title, String summary, String link, String length, String date, String episodeNum) {
            this.title = title;
            this.summary = summary;
            this.link = link;
            this.length = length;
            this.date = date;
            this.episodeNum = episodeNum;
        }
    }
}
