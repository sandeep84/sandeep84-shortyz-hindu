package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

import com.totsp.crossword.PuzzleFinishedActivity;


@TargetApi(11)
public class HoneycombUtil extends GingerbreadUtil {
	
	{
		System.out.println("Honeycomb Utils.");
	}
	
	@Override
    public void finishOnHomeButton(final Activity a) {
		ActionBar bar = a.getActionBar();
		if(bar == null){
			return;
		}
		bar.setDisplayHomeAsUpEnabled(true);
		View home = a.findViewById(android.R.id.home);
        if(home != null){
	        home.setOnClickListener(new OnClickListener() {
	                public void onClick(View arg0) {
	                    a.finish();
	                }
	            });
        }
    }

    @TargetApi(11)
	@Override
    public void holographic(Activity a) {
        if(a instanceof PuzzleFinishedActivity){
        	a.setTheme(android.R.style.Theme_Holo_Dialog);
        } else {
        	a.setTheme(android.R.style.Theme_Holo);
        }
        ActionBar bar = a.getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onActionBarWithText(MenuItem a) {
        a.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT + MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public void onActionBarWithText(SubMenu a) {
        this.onActionBarWithText(a.getItem());
    }
    
    @Override
    public boolean downloadFile(URL url, File destination, Map<String, String> headers, boolean notification,
            String title) {
            DownloadManager mgr = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);

            Request request = new Request(Uri.parse(url.toString()));
            request.setDestinationUri(Uri.fromFile(destination));
            

            for (Entry<String, String> entry : headers.entrySet()) {
                request.addRequestHeader(entry.getKey(), entry.getValue());
            }

            request.setMimeType("application/x-crossword");
            
            request.setNotificationVisibility(notification ? Request.VISIBILITY_VISIBLE : Request.VISIBILITY_HIDDEN);
            
            request.setTitle(title);
            mgr.enqueue(request);

            return false;
        }
    
    public View onActionBarCustom(Activity a, int id) {
    	System.out.println("Setting custom ActionBar view");
    	ActionBar bar = a.getActionBar();
    	if(bar == null){
    		return null;
    	}
    	bar.setDisplayShowCustomEnabled(true);
    	bar.setDisplayShowTitleEnabled(false);
    	bar.setDisplayShowHomeEnabled(true);
    	bar.setCustomView(id);
    	System.out.println(bar.getCustomView());
    	return bar.getCustomView();
	}
    
    public void hideWindowTitle(Activity a) {
    	// no op;
    }

	public void hideActionBar(Activity a) {
		ActionBar ab = a.getActionBar();
		if(ab == null){
			return;
		}
		ab.hide();
	}
    
}
