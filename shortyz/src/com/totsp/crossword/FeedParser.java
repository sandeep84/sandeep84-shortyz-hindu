// Mostly copied from http://developer.android.com/training/basics/network-ops/xml.html

package com.totsp.crossword;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class FeedParser {
    // We don't use namespaces
    private static final String ns = null;
    private String title = null;
    private String content = null;
    private String author = null;
   
    public FeedParser(InputStream in) {
    	try {
			parse(in);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public String getTitle() {
		return this.title;
	}

	public String getContent() {
		return this.content;
	}

	public String getAuthor() {
		return this.author;
	}

	public void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readFeed(parser);
        } finally {
            in.close();
        }
    }

	private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, ns, "feed");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        // Starts by looking for the entry tag
	        if (name.equals("entry")) {
	            readEntry(parser);
	        } else {
	            skip(parser);
	        }
	    }  
	}
	
	private void readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, ns, "entry");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        if (name.equals("title")) {
	            this.title = readTitle(parser);
	        } else if (name.equals("content")) {
	            this.content = readContent(parser);
	        } else {
	            skip(parser);
	        }
	    }
	}

	// Processes title tags in the feed.
	private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "title");
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "title");
	    return title;
	}
	  
	// Processes link tags in the feed.
	@SuppressWarnings("unused")
	private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String link = "";
	    parser.require(XmlPullParser.START_TAG, ns, "link");
	    String tag = parser.getName();
	    String relType = parser.getAttributeValue(null, "rel");  
	    if (tag.equals("link")) {
	        if (relType.equals("alternate")){
	            link = parser.getAttributeValue(null, "href");
	            parser.nextTag();
	        } 
	    }
	    parser.require(XmlPullParser.END_TAG, ns, "link");
	    return link;
	}

	// Processes summary tags in the feed.
	private String readContent(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "content");
	    String summary = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "content");
	    return summary;
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
}
