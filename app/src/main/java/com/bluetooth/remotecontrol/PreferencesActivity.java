package com.bluetooth.remotecontrol;

import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

public class PreferencesActivity extends PreferenceActivity implements View.OnClickListener {
    Preference longClickReleaseAction;
	Resources res;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    	
    	Boolean buyKeyValue;
    	Preference buyKeyPrefScreen, accelerometerPrefScreen, invertButtonPrefScreen, longClickReleasePrefScreen, setButtonLabelsPrefScreen, feedbackPrefScreen;
    	CheckBoxPreference accelOutput;
    	CheckBoxPreference longClickRelease;
    	EditTextPreference longClickReleaseOutput;
    	EditTextPreference etFeedbackSB1, etFeedbackSB2, etFeedbackSB3, etFeedbackSB4, etFeedbackSB5, etFeedbackSB6, etFeedbackSB7, etFeedbackSB8, etFeedbackTV9;
    	CheckBoxPreference cbFeedbackSB1, cbFeedbackSB2, cbFeedbackSB3, cbFeedbackSB4, cbFeedbackSB5, cbFeedbackSB6, cbFeedbackSB7;
    	CheckBoxPreference checkBoxPreference;
    	Resources res;
    	
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			res = getResources();
			
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
			
			// SETS WHETHER WE HAVE PURCHASED A KEY
			buyKeyValue = true;
			//buyKeyValue = false;
			//buyKeyPrefScreen = (Preference) this.findPreference(res.getString(R.string.KeyBuyKeyPref));
			accelerometerPrefScreen = (Preference) this.findPreference(res.getString(R.string.KeySetButtonLabelsPref));
			invertButtonPrefScreen = (Preference) this.findPreference(res.getString(R.string.KeyInvertLayoutPref));
			longClickReleasePrefScreen = (Preference) this.findPreference(res.getString(R.string.KeyLongClickReleasePref));
			setButtonLabelsPrefScreen = (Preference) this.findPreference(res.getString(R.string.KeySetButtonLabelsPref));
			feedbackPrefScreen = (Preference) this.findPreference(res.getString(R.string.KeyFeedbackGroupPref));
			
			accelOutput = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyAccelOutput));
			accelOutput.setOnPreferenceChangeListener(this);
			
			checkBoxPreference = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyResetDefault));
			checkBoxPreference.setOnPreferenceChangeListener(this);
			
			longClickRelease = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyLongClickReleaseAction));
			longClickRelease.setOnPreferenceChangeListener(this);
			
			cbFeedbackSB1 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup1));
			cbFeedbackSB2 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup2));
			cbFeedbackSB3 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup3));
			cbFeedbackSB4 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup4));
			cbFeedbackSB5 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup5));
			cbFeedbackSB6 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup6));
			cbFeedbackSB7 = (CheckBoxPreference) this.findPreference(res.getString(R.string.KeyFeedbackGroup7));
			
			cbFeedbackSB1.setOnPreferenceChangeListener(this);
			cbFeedbackSB2.setOnPreferenceChangeListener(this);
			cbFeedbackSB3.setOnPreferenceChangeListener(this);
			cbFeedbackSB4.setOnPreferenceChangeListener(this);
			cbFeedbackSB5.setOnPreferenceChangeListener(this);
			cbFeedbackSB6.setOnPreferenceChangeListener(this);
			cbFeedbackSB7.setOnPreferenceChangeListener(this);
			
			etFeedbackSB1 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB1));
			etFeedbackSB2 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB2));
			etFeedbackSB3 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB3));
			etFeedbackSB4 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB4));
			etFeedbackSB5 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB5));
			etFeedbackSB6 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB6));
			etFeedbackSB7 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB7));
			etFeedbackSB8 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelSB8));
			etFeedbackTV9 = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLabelTV9));
			
			longClickReleaseOutput = (EditTextPreference) this.findPreference(res.getString(R.string.KeyLongClickReleaseLabel));
			this.loadInitialEditTextState();
		}

		@Override
		public boolean onPreferenceChange(Preference sharedPreferences, Object key) {
			if(sharedPreferences.getKey().equals(res.getString(R.string.KeyBuyKeyPref))) {
				// NOT WORKING
				Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/collection/editors_choice"));
				startActivity(browserIntent);
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyResetDefault))) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Reset all preferences to default?")
                        .setTitle("Reset")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            	setAllPreferencesToDefault();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            	// do stuff on cancel
                            }
                        })
                        .show();
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyLongClickReleaseAction))) {
				if((Boolean) key) {	longClickReleaseOutput.setEnabled(true); }
				else { longClickReleaseOutput.setEnabled(false); }
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyAccelOutput))) {
				if((Boolean) key) {	accelOutput.setEnabled(true); }
				else { accelOutput.setEnabled(false); }
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup1))) {
				if((Boolean) key) {	
					etFeedbackSB1.setEnabled(true);
					etFeedbackSB2.setEnabled(true);
				}
				else { 
					etFeedbackSB1.setEnabled(false);
					etFeedbackSB2.setEnabled(false);
				}
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup2))) {
				if((Boolean) key) {	
					etFeedbackSB3.setEnabled(true);
				}
				else {
					etFeedbackSB3.setEnabled(false);
				}
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup3))) {
				if((Boolean) key) {
					etFeedbackSB4.setEnabled(true);
				}
				else {
					etFeedbackSB4.setEnabled(false);
				}
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup4))) {
				if((Boolean) key) {	
					etFeedbackSB5.setEnabled(true);	
				}
				else { 
					etFeedbackSB5.setEnabled(false);	
				}
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup5))) {
				if((Boolean) key) {	
					etFeedbackSB6.setEnabled(true);	
				}
				else { 
					etFeedbackSB6.setEnabled(false);
				}
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup6))) {
				if((Boolean) key) {	
					etFeedbackSB7.setEnabled(true);
					etFeedbackSB8.setEnabled(true);
				}
				else { 
					etFeedbackSB7.setEnabled(false);
					etFeedbackSB8.setEnabled(false);
				}
			}
			else if(sharedPreferences.getKey().equals(res.getString(R.string.KeyFeedbackGroup7))) {
				if((Boolean) key) {
					etFeedbackTV9.setEnabled(true);
				}
				else {
					etFeedbackTV9.setEnabled(false);
				}
			}
			
			// return false to not update the checkbox with new value
			return true;
		}
		
		private void loadInitialEditTextState() {
			// Check to see if we are using the paid or free version
			/*if(buyKeyValue) {
				buyKeyPrefScreen.setEnabled(false);
				buyKeyPrefScreen.setTitle(res.getString(R.string.BoughtKey));
				buyKeyPrefScreen.setSummary("");
			}
			else
				buyKeyPrefScreen.setEnabled(true);
			*/
			
			
			// CHECK IF A KEY HAS BEEN PURCHASED
			if(buyKeyValue) {
				// Accelerometer Output Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyAccelOutput), true)) {
					accelOutput.setEnabled(true);
				}
				else
					accelOutput.setEnabled(false);
				
				// Long Click Release Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyLongClickReleaseAction), true)) {
					longClickReleaseOutput.setEnabled(true);
				}
				else
					longClickReleaseOutput.setEnabled(false);
				
				// Feedback Group 1 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup1), true)) {
					etFeedbackSB1.setEnabled(true);
					etFeedbackSB2.setEnabled(true);
				}
				else { 
					etFeedbackSB1.setEnabled(false);
					etFeedbackSB2.setEnabled(false);
				}
				
				// Feedback Group 2 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup2), true)) {
					etFeedbackSB3.setEnabled(true);
				}
				else {
					etFeedbackSB3.setEnabled(false);
				}
				
				// Feedback Group 3 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup3), true)) {
					etFeedbackSB4.setEnabled(true);
				}
				else {
					etFeedbackSB4.setEnabled(false);
				}
				
				// Feedback Group 4 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup4), true)) {
					etFeedbackSB5.setEnabled(true);
				}
				else {
					etFeedbackSB5.setEnabled(false);
				}
				
				// Feedback Group 5 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup5), true)) {
					etFeedbackSB6.setEnabled(true);
				}
				else {
					etFeedbackSB6.setEnabled(false);
				}
				
				// Feedback Group 6 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup6), true)) {
					etFeedbackSB7.setEnabled(true);
					etFeedbackSB8.setEnabled(true);
				}
				else { 
					etFeedbackSB7.setEnabled(false);
					etFeedbackSB8.setEnabled(false);
				}
				
				// Feedback Group 7 Action
				if(this.getPreferenceManager().getSharedPreferences().getBoolean(res.getString(R.string.KeyFeedbackGroup7), true)) {
					etFeedbackTV9.setEnabled(true);
				}
			}
			else {
				// Disable all feedback groups if the app key hasn't been bought
				accelOutput.setEnabled(false);
				longClickReleaseOutput.setEnabled(false);
				setButtonLabelsPrefScreen.setEnabled(false);
				feedbackPrefScreen.setEnabled(false);
				accelerometerPrefScreen.setEnabled(false);
				invertButtonPrefScreen.setEnabled(false);
				longClickReleasePrefScreen.setEnabled(false);
			}
		}
		private void setAllPreferencesToDefault() {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			Editor editor = sharedPrefs.edit();
			editor.clear();
			editor.commit();
			
			PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, true);
			
			// Finish or refresh the activity
			this.getActivity().finish();
		}
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}