package com.totsp.crossword;

import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import android.preference.Preference;

import android.preference.Preference.OnPreferenceClickListener;

import android.preference.PreferenceActivity;

import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.versions.AndroidVersionUtils;


public class PreferencesActivity extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidVersionUtils.Factory.getInstance().holographic(this);
        addPreferencesFromResource(R.xml.preferences);

        Preference release = (Preference) findPreference("releaseNotes");
        release.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/release.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        Preference license = (Preference) findPreference("license");
        license.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/license.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        Preference subscribeNyt = (Preference) findPreference("nytSubscribe");
        subscribeNyt.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com/puzzle"));
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        Preference scrapeInfo = (Preference) findPreference("aboutScrapes");
        scrapeInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/scrapes.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });
    }
}
