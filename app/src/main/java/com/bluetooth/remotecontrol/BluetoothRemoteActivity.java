package com.bluetooth.remotecontrol;

import java.util.Arrays;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothRemoteActivity extends Activity implements View.OnClickListener, View.OnLongClickListener, OnTouchListener, SensorEventListener {
	RelativeLayout	rlHome;
	RelativeLayout	llFeedbackGroup1;
	LinearLayout	llFeedbackGroup2;
	LinearLayout	llFeedbackGroup3;
	LinearLayout	llFeedbackGroup4;
	LinearLayout	llFeedbackGroup5;
	RelativeLayout	llFeedbackGroup6;
	LinearLayout	llFeedbackGroup7;
	
	TextView tvFeedback1;
	TextView tvFeedback2;
	TextView tvFeedback3;
	TextView tvFeedback4;
	TextView tvFeedback5;
	TextView tvFeedback6;
	TextView tvFeedback7;
	TextView tvFeedback8;
	TextView tvFeedback9;
	TextView tvFeedback10;
	
	ProgressBar pbFeedback1;
	ProgressBar pbFeedback2;
	ProgressBar pbFeedback3;
	ProgressBar pbFeedback4;
	ProgressBar pbFeedback5;
	ProgressBar pbFeedback6;
	ProgressBar pbFeedback7;
	ProgressBar pbFeedback8;
	
	Button buttonActionUp;
	Button buttonActionLeft;
	Button buttonActionStop;
	Button buttonActionRight;
	Button buttonActionDown;
	
	Button buttonExtra1;
	Button buttonExtra2;
	Button buttonExtra3;
	Button buttonExtra4;
	Button buttonExtra5;
	Button buttonExtra6;
	
	Button buttonSpeedUp;
	Button buttonSpeedNormal;
	Button buttonSpeedDown;
	
	
	boolean longClickEvent = false;
	boolean feedbackGroup1 = false;
	boolean feedbackGroup2 = false;
	boolean feedbackGroup3 = false;
	boolean feedbackGroup4 = false;
	boolean feedbackGroup5 = false;
	boolean feedbackGroup6 = false;
	boolean feedbackGroup7 = false;
	
	SharedPreferences prefs;
	Resources res;
	private static final int UPDATE_PREFERENCES = 6;
	
	// Local bluetooth adapter
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();	// Default bluetooth adapter present on the phone
    private static BluetoothSerialService mSerialService = null;				// Instance of serial service for sending serial messages
    
    @SuppressWarnings("unused")
	private String mConnectedDeviceName = null;									// Name of the connected device
    
    Menu menu;
    MenuItem menuItemBluetooth;
    
    // Accelerometer controllers
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    
	// Bluetooth intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 4;
    private static final int REQUEST_ENABLE_BT = 5;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    //Bluetooth Menu Items
    private MenuItem mMenuItemConnect;
    //private String appName = "Bluetooth Remote";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load resources to be able to get strings from strings.xml
		res = getResources();
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		// Set the view of the screen 
		this.loadContentViewFromPref();
		
		// Load the buttons and event handling for all the buttons
		this.loadButtonsandViews();
		
		// Set up the accelerometer information
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);		
		this.accelerometerOutputSettings();
		
		// Set up the serial service for sending Bluetooth 
		mSerialService = new BluetoothSerialService(this, mHandlerBT);
	}
	@Override
	protected void onResume() {
		super.onResume();
		senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	@Override
	protected void onPause() {
		super.onPause();
		senSensorManager.unregisterListener(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	
		if (mSerialService != null)
        	mSerialService.stop();
    }
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);	
        
        // Reload all the event listeners on orientation change
        this.loadContentViewFromPref();
        this.loadButtonsandViews();   
        this.accelerometerOutputSettings();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_remote, menu);
		
		// Get the menu for use in editing the action bar
		this.menu = menu;
		
		// Get the mene item for the bluetooth button
		menuItemBluetooth = menu.findItem(R.id.action_bluetooth);
		
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings :
			Intent debugIntent = new Intent(this,PreferencesActivity.class);
			this.startActivityForResult(debugIntent, UPDATE_PREFERENCES);
			
			break;
		//case R.id.action_donate :
		//
		case R.id.action_bluetooth :
			this.bluetoothManager();
			break;
		case R.id.action_help :
			AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothRemoteActivity.this);
		    builder.setTitle("Help Menu")
		    		.setPositiveButton("Done", null)
		    		.setMessage(Html.fromHtml(
		    				"<p><b>Bluetooth Connect:</b> Click the bluetooth icon and pair yourself to a bluetooth enabled device." +
		    				"<br>*Hint: it's always easier to connect to a device you have previously paired with through the device settings</p>" +
		    				
		    				"<p><b>Button Actions:</b> Navigate into the settings to change what buttons do. You have the option to " +
		    				"edit the normal button click action, the long click action and if you choose, you can set an action for the " +
		    				"long click release.</p>" +
		    				
		    				"<p><b>Button Labels:</b> Change the button labels to suit the action they will perform in context with " +
		    				"your device.</p>" +
		    				
		    				"<p><b>Feedback:</b> Turn the various feedback options on and off to suit your application and device. Each scale" +
		    				"bar scales from 0 to 100, so ensure your feedback does not go outside of this range." +
		    				"<br>*Hint: to help debug your feedback, connect to a device and set one of the button outputs to equal 'SB1=10'." +
		    				"When the button is pressed the value will appear as a percentage in the scale bar.</p>"
		    		));
		    
		    AlertDialog alertDialog = builder.create();
			alertDialog.show();
			
			break;
		}
		
		return false;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
		case REQUEST_CONNECT_DEVICE:

            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(BtDeviceListActivity.EXTRA_DEVICE_ADDRESS);	// Get the device MAC address
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);					// Get the BLuetoothDevice object
                mSerialService.connect(device);                	                						// Attempt to connect to the device
            }
        break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                this.bluetoothManager();																// Bluetooth is now enabled, so go back through the loop to setup a bluetooth connection
            } 
            else {
            	Toast.makeText(this, "Bluetooth Not Enabled", Toast.LENGTH_SHORT).show();				// User did not enable Bluetooth or an error occured
            }
        break;
        case UPDATE_PREFERENCES :
    		// Load the preferences
    		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    		
    		this.loadContentViewFromPref();
    		this.loadButtonsandViews();
    		this.accelerometerOutputSettings();
		}
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// if the preference checkbox is checked and we have registered a longClickEvent
    	if(prefs.getBoolean(res.getString(R.string.KeyLongClickReleaseAction), false) && longClickEvent) {
			// If we had a long click and a release and we want to do something about it
    		if(event.getAction() == android.view.MotionEvent.ACTION_UP) {			
				switch(view.getId()) {
					case R.id.buttonActionUp :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonActionLeft :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonActionStop :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonActionRight :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonActionDown :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonExtra1 :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonExtra2 :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonExtra3 :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonExtra4 :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonExtra5 :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonExtra6 :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					/*case R.id.buttonSpeedUp :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonSpeedNormal :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;
					case R.id.buttonSpeedDown :
						this.sendLongClickUp(prefs.getString(res.getString(R.string.KeyLongClickReleaseLabel), "LongClickUp"), false);
					break;*/
				}
			}
    	}
		
		return false;
	}
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()) {
			case R.id.buttonActionUp :
				this.sendString(prefs.getString(res.getString(R.string.KeyUpButtonClick), "Up"));
			break;
			case R.id.buttonActionLeft :
				this.sendString(prefs.getString(res.getString(R.string.KeyLeftButtonClick), "Left"));
			break;
			case R.id.buttonActionStop :
				this.sendString(prefs.getString(res.getString(R.string.KeyStopButtonClick), "Stop"));
			break;
			case R.id.buttonActionRight :
				this.sendString(prefs.getString(res.getString(R.string.KeyRightButtonClick), "Right"));
			break;
			case R.id.buttonActionDown :
				this.sendString(prefs.getString(res.getString(R.string.KeyDownButtonClick), "Down"));
			break;
			case R.id.buttonExtra1 :
				this.sendString(prefs.getString(res.getString(R.string.KeyAButtonClick), "A"));
			break;
			case R.id.buttonExtra2 :
				this.sendString(prefs.getString(res.getString(R.string.KeyBButtonClick), "B"));
			break;
			case R.id.buttonExtra3 :
				this.sendString(prefs.getString(res.getString(R.string.KeyCButtonClick), "C"));
			break;
			case R.id.buttonExtra4 :
				this.sendString(prefs.getString(res.getString(R.string.KeyDButtonClick), "D"));
			break;
			case R.id.buttonExtra5 :
				this.sendString(prefs.getString(res.getString(R.string.KeyEButtonClick), "E"));
			break;
			case R.id.buttonExtra6 :
				this.sendString(prefs.getString(res.getString(R.string.KeyFButtonClick), "F"));
			break;
			case R.id.buttonSpeedUp :
				this.sendString(prefs.getString(res.getString(R.string.KeySpeedUpButtonClick), "+"));
			break;
			case R.id.buttonSpeedNormal :
				this.sendString(prefs.getString(res.getString(R.string.KeySpeedNormalButtonClick), "*"));
			break;
			case R.id.buttonSpeedDown :
				this.sendString(prefs.getString(res.getString(R.string.KeySpeedDownButtonClick), "-"));
			break;
		}
	}
	@Override
	public boolean onLongClick(View arg0) {
		switch(arg0.getId()) {
			case R.id.buttonActionUp :
				this.sendString(prefs.getString(res.getString(R.string.KeyUpButtonLongClick), "UpLong"));
			break;
			case R.id.buttonActionLeft :
				this.sendString(prefs.getString(res.getString(R.string.KeyLeftButtonLongClick), "LeftLong"));
			break;
			case R.id.buttonActionStop :
				this.sendString(prefs.getString(res.getString(R.string.KeyStopButtonLongClick), "StopLong"));
			break;
			case R.id.buttonActionRight :
				this.sendString(prefs.getString(res.getString(R.string.KeyRightButtonLongClick), "RightLong"));
			break;
			case R.id.buttonActionDown :
				this.sendString(prefs.getString(res.getString(R.string.KeyDownButtonLongClick), "DownLong"));
			break;
			case R.id.buttonExtra1 :
				this.sendString(prefs.getString(res.getString(R.string.KeyAButtonLongClick), "ALong"));
			break;
			case R.id.buttonExtra2 :
				this.sendString(prefs.getString(res.getString(R.string.KeyBButtonLongClick), "BLong"));
			break;
			case R.id.buttonExtra3 :
				this.sendString(prefs.getString(res.getString(R.string.KeyCButtonLongClick), "CLong"));
			break;
			case R.id.buttonExtra4 :
				this.sendString(prefs.getString(res.getString(R.string.KeyDButtonLongClick), "DLong"));
			break;
			case R.id.buttonExtra5 :
				this.sendString(prefs.getString(res.getString(R.string.KeyEButtonLongClick), "ELong"));
			break;
			case R.id.buttonExtra6 :
				this.sendString(prefs.getString(res.getString(R.string.KeyFButtonLongClick), "FLong"));
			break;					
		}
		
		// Set the long click event so that we can register the long click release
		longClickEvent = true;
		
		// No need to process the onClickListener since we processes onLongClickListerner
		return true;
	}
	
	private void loadContentViewFromPref() {
		final int orientation = getResources().getConfiguration().orientation;
		
		// Not really being used since we are using the layout folders to manage the layouts
		if(orientation == Configuration.ORIENTATION_LANDSCAPE) {	
			if(prefs.getBoolean(res.getString(R.string.KeyInvertLayout), false))
	        	setContentView(R.layout.home_inverse);
	        else
	        	setContentView(R.layout.home);
		}
		else if(orientation == Configuration.ORIENTATION_PORTRAIT) {
			if(prefs.getBoolean(res.getString(R.string.KeyInvertLayout), false))
	        	setContentView(R.layout.home_inverse);
	        else
	        	setContentView(R.layout.home);
		}
	}
	private void loadButtonsandViews() {
		this.rlHome = (RelativeLayout) findViewById(R.id.rlHome);
		this.llFeedbackGroup1 = (RelativeLayout) findViewById(R.id.rlFeedbackGroup1);
		this.llFeedbackGroup2 = (LinearLayout) findViewById(R.id.llFeedbackGroup2);
		this.llFeedbackGroup3 = (LinearLayout) findViewById(R.id.llFeedbackGroup3);
		this.llFeedbackGroup4 = (LinearLayout) findViewById(R.id.llFeedbackGroup4);
		this.llFeedbackGroup5 = (LinearLayout) findViewById(R.id.llFeedbackGroup5);
		this.llFeedbackGroup6 = (RelativeLayout) findViewById(R.id.rlFeedbackGroup6);
		this.llFeedbackGroup7 = (LinearLayout) findViewById(R.id.llFeedbackGroup7);
		
		// Setup the feedback layouts
		tvFeedback1 = (TextView) findViewById(R.id.textViewFeedback1);
		tvFeedback2 = (TextView) findViewById(R.id.textViewFeedback2);
		tvFeedback3 = (TextView) findViewById(R.id.textViewFeedback3);
		tvFeedback4 = (TextView) findViewById(R.id.textViewFeedback4);
		tvFeedback5 = (TextView) findViewById(R.id.textViewFeedback5);
		tvFeedback6 = (TextView) findViewById(R.id.textViewFeedback6);
		tvFeedback7 = (TextView) findViewById(R.id.textViewFeedback7);
		tvFeedback8 = (TextView) findViewById(R.id.textViewFeedback8);
		tvFeedback9 = (TextView) findViewById(R.id.textViewFeedback9);
		pbFeedback1 = (ProgressBar) findViewById(R.id.progressBarFeedback1);
		pbFeedback2 = (ProgressBar) findViewById(R.id.progressBarFeedback2);
		pbFeedback3 = (ProgressBar) findViewById(R.id.progressBarFeedback3);
		pbFeedback4 = (ProgressBar) findViewById(R.id.progressBarFeedback4);
		pbFeedback5 = (ProgressBar) findViewById(R.id.progressBarFeedback5);
		pbFeedback6 = (ProgressBar) findViewById(R.id.progressBarFeedback6);
		pbFeedback7 = (ProgressBar) findViewById(R.id.progressBarFeedback7);
		pbFeedback8 = (ProgressBar) findViewById(R.id.progressBarFeedback8);
		tvFeedback10 = (TextView) findViewById(R.id.textViewFeedback10);
		
		// Set the feedback labels to the value saved in the preferences
		tvFeedback1.setText(prefs.getString(res.getString(R.string.KeyLabelSB1), res.getString(R.string.TitleLabelSB1)));
		tvFeedback2.setText(prefs.getString(res.getString(R.string.KeyLabelSB2), res.getString(R.string.TitleLabelSB2)));
		tvFeedback3.setText(prefs.getString(res.getString(R.string.KeyLabelSB3), res.getString(R.string.TitleLabelSB3)));
		tvFeedback4.setText(prefs.getString(res.getString(R.string.KeyLabelSB4), res.getString(R.string.TitleLabelSB4)));
		tvFeedback5.setText(prefs.getString(res.getString(R.string.KeyLabelSB5), res.getString(R.string.TitleLabelSB5)));
		tvFeedback6.setText(prefs.getString(res.getString(R.string.KeyLabelSB6), res.getString(R.string.TitleLabelSB6)));
		tvFeedback7.setText(prefs.getString(res.getString(R.string.KeyLabelSB7), res.getString(R.string.TitleLabelSB7)));
		tvFeedback8.setText(prefs.getString(res.getString(R.string.KeyLabelSB8), res.getString(R.string.TitleLabelSB8)));
		tvFeedback9.setText(prefs.getString(res.getString(R.string.KeyLabelTV9), res.getString(R.string.TitleLabelTV9)));
		
		// Setup the main action buttons for event handling
		buttonActionUp = (Button) findViewById(R.id.buttonActionUp);
		buttonActionLeft = (Button) findViewById(R.id.buttonActionLeft);
		buttonActionStop = (Button) findViewById(R.id.buttonActionStop);
		buttonActionRight = (Button) findViewById(R.id.buttonActionRight);
		buttonActionDown = (Button) findViewById(R.id.buttonActionDown);
		buttonExtra1 = (Button) findViewById(R.id.buttonExtra1);
		buttonExtra2 = (Button) findViewById(R.id.buttonExtra2);
		buttonExtra3 = (Button) findViewById(R.id.buttonExtra3);
		buttonExtra4 = (Button) findViewById(R.id.buttonExtra4);
		buttonExtra5 = (Button) findViewById(R.id.buttonExtra5);
		buttonExtra6 = (Button) findViewById(R.id.buttonExtra6);
		buttonSpeedUp = (Button) findViewById(R.id.buttonSpeedUp);
		buttonSpeedNormal = (Button) findViewById(R.id.buttonSpeedNormal);
		buttonSpeedDown = (Button) findViewById(R.id.buttonSpeedDown);
		
		// Set the button label to the value saved in the preferences
		buttonActionUp.setText(prefs.getString(res.getString(R.string.KeyUpButtonLabel), res.getString(R.string.LabelUpButton)));
		buttonActionLeft.setText(prefs.getString(res.getString(R.string.KeyLeftButtonLabel), res.getString(R.string.LabelLeftButton)));
		buttonActionStop.setText(prefs.getString(res.getString(R.string.KeyStopButtonLabel), res.getString(R.string.LabelStopButton)));
		buttonActionRight.setText(prefs.getString(res.getString(R.string.KeyRightButtonLabel), res.getString(R.string.LabelRightButton)));
		buttonActionDown.setText(prefs.getString(res.getString(R.string.KeyDownButtonLabel), res.getString(R.string.LabelDownButton)));
		buttonExtra1.setText(prefs.getString(res.getString(R.string.KeyAButtonLabel), res.getString(R.string.LabelAButton)));
		buttonExtra2.setText(prefs.getString(res.getString(R.string.KeyBButtonLabel), res.getString(R.string.LabelBButton)));
		buttonExtra3.setText(prefs.getString(res.getString(R.string.KeyCButtonLabel), res.getString(R.string.LabelCButton)));
		buttonExtra4.setText(prefs.getString(res.getString(R.string.KeyDButtonLabel), res.getString(R.string.LabelDButton)));
		buttonExtra5.setText(prefs.getString(res.getString(R.string.KeyEButtonLabel), res.getString(R.string.LabelEButton)));
		buttonExtra6.setText(prefs.getString(res.getString(R.string.KeyFButtonLabel), res.getString(R.string.LabelFButton)));
		//buttonSpeedUp.setText(prefs.getString(res.getString(R.string.KeySpeedUpButtonLabel), res.getString(R.string.LabelSpeedUpButton)));
		buttonSpeedUp.setText(prefs.getString(res.getString(R.string.KeySpeedUpButtonLabel), res.getString(R.string.LabelSpeedUpButton)));
		buttonSpeedNormal.setText(prefs.getString(res.getString(R.string.KeySpeedNormalButtonLabel), res.getString(R.string.LabelSpeedNormalButton)));
		buttonSpeedDown.setText(prefs.getString(res.getString(R.string.KeySpeedDownButtonLabel), res.getString(R.string.LabelSpeedDownButton)));
		
		// Set the onClick and onLongClick Listeners
		buttonActionUp.setOnClickListener(this);
		buttonActionUp.setOnLongClickListener(this);
		buttonActionLeft.setOnClickListener(this);
		buttonActionLeft.setOnLongClickListener(this);
		buttonActionStop.setOnClickListener(this);
		buttonActionStop.setOnLongClickListener(this);
		buttonActionRight.setOnClickListener(this);
		buttonActionRight.setOnLongClickListener(this);
		buttonActionDown.setOnClickListener(this);
		buttonActionDown.setOnLongClickListener(this);
		
		// Set the onClick and onClick Listeners
		buttonExtra1.setOnClickListener(this);
		buttonExtra1.setOnLongClickListener(this);
		buttonExtra2.setOnClickListener(this);
		buttonExtra2.setOnLongClickListener(this);
		buttonExtra3.setOnClickListener(this);
		buttonExtra3.setOnLongClickListener(this);
		buttonExtra4.setOnClickListener(this);
		buttonExtra4.setOnLongClickListener(this);
		buttonExtra5.setOnClickListener(this);
		buttonExtra5.setOnLongClickListener(this);
		buttonExtra6.setOnClickListener(this);
		buttonExtra6.setOnLongClickListener(this);
		
		buttonSpeedUp.setOnClickListener(this);
		buttonSpeedNormal.setOnClickListener(this);
		buttonSpeedDown.setOnClickListener(this);
		
		// Set the onClick and onTouch Listeners
		buttonActionUp.setOnTouchListener(this);
		buttonActionLeft.setOnTouchListener(this);
		buttonActionStop.setOnTouchListener(this);
		buttonActionRight.setOnTouchListener(this);
		buttonActionDown.setOnTouchListener(this);
		buttonExtra1.setOnTouchListener(this);
		buttonExtra2.setOnTouchListener(this);
		buttonExtra3.setOnTouchListener(this);
		buttonExtra4.setOnTouchListener(this);
		buttonExtra5.setOnTouchListener(this);
		buttonExtra6.setOnTouchListener(this);
		
		// Set visibility of feedback group 1 based on the preferences
		setFeedbackGroupVisibility();
	}
	private void accelerometerOutputSettings() {
		// Register the sensor event listener if the checkbox is selected in the preferences menu
		if(prefs.getBoolean(res.getString(R.string.KeyAccelOutput), false)) {
			senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
		else {
			senSensorManager.unregisterListener(this);
		}
	}
	private void sendLongClickUp(String stringToSend, boolean longClickEventValue) {
		// Send the string associated with the long click up event
		this.sendString(stringToSend);

		// Reset the long click event for trigger next time
    	longClickEvent = longClickEventValue;
	}
	private void setFeedbackGroupVisibility() {
		// Set visibility of feedback group 2 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup1), false)) {
			this.llFeedbackGroup1.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup1.setVisibility(View.GONE);
		}
		
		// Set visibility of feedback group 2 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup2), false)) {
			this.llFeedbackGroup2.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup2.setVisibility(View.GONE);
		}
		
		// Set visibility of feedback group 3 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup3), false)) {
			this.llFeedbackGroup3.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup3.setVisibility(View.GONE);
		}
		
		// Set visibility of feedback group 4 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup4), false)) {
			this.llFeedbackGroup4.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup4.setVisibility(View.GONE);
		}
		
		// Set visibility of feedback group 5 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup5), false)) {
			this.llFeedbackGroup5.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup5.setVisibility(View.GONE);
		}
		
		// Set visibility of feedback group 6 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup6), false)) {
			this.llFeedbackGroup6.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup6.setVisibility(View.GONE);
		}
		
		// Set visibility of feedback group 7 based on the preferences
		if(prefs.getBoolean(res.getString(R.string.KeyFeedbackGroup7), false)) {
			this.llFeedbackGroup7.setVisibility(View.VISIBLE);
		}
		else {
			this.llFeedbackGroup7.setVisibility(View.GONE);
		}
	}
	
	
	
	//*******************************
	//****BLUETOOTH ACTIVITIES*******
	//*******************************
	private void bluetoothManager() {
		if (mBluetoothAdapter != null) {
			//Intent btDevicesIntent = null;
			
			if (!mBluetoothAdapter.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			else {
				if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
	        		// Launch the DeviceListActivity to see devices and do scan
	        		Intent serverIntent = new Intent(this, BtDeviceListActivity.class);
	        		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	        	}
	        	else {
	            	if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
	            		mSerialService.stop();
			    		mSerialService.start();
	            	}
	        	}
			}
		}
		else
			Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
	}
    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {
    	
        @Override
        public void handleMessage(Message msg) {        	
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                //if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothSerialService.STATE_CONNECTED:
                	//if (mMenuItemConnect != null) {
                	//	mMenuItemConnect.setTitle("Bluetooth Disconnect");
                	//}
                	
                    //setTitle(appName +": Connected To " + mConnectedDeviceName);
                    menuItemBluetooth.setIcon(R.drawable.device_access_bluetooth_connected_on);
                    
                    break;
                    
                case BluetoothSerialService.STATE_CONNECTING:
                	//setTitle(appName + ": Connecting");
                	menuItemBluetooth.setIcon(R.drawable.device_access_bluetooth_searching_on);
                	
                    break;
                    
                case BluetoothSerialService.STATE_LISTEN:
                case BluetoothSerialService.STATE_NONE:
                	//if (mMenuItemConnect != null) {
                	//	mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
                	//	mMenuItemConnect.setTitle("Bluetooth Connect");
                	//}
                	
                    //setTitle(appName + ": Not Connected");
                    menuItemBluetooth.setIcon(R.drawable.device_access_bluetooth);
                    
                    break;
                }
                break;
            case MESSAGE_WRITE:
            	/*if (mLocalEcho) {
            		byte[] writeBuf = (byte[]) msg.obj;
            		mEmulatorView.write(writeBuf, msg.arg1);
            	}
                
                break;
                */
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;              
                //mEmulatorView.write(readBuf, msg.arg1);
                
                // Convert received message to a string
                String receivedMessage = new String(Arrays.copyOfRange(readBuf, 0, msg.arg1));
                receivedMessage = receivedMessage.replace(" ", "");
                
                // Break the string up by ,
                String[] receivedMessageSplit = receivedMessage.split(",");
                
                for (String input : receivedMessageSplit) {
                	// Get the scale bar number that is being referenced
                	int scaleBarNumber = getTargetFeedbackElement(input);
                    int scaleBarValue;
                    
                    if(scaleBarNumber == 1) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback1.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 2) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback2.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 3) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback3.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 4) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback4.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 5) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback5.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 6) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback6.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 7) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback7.setProgress(scaleBarValue);
                    }
                    else if(scaleBarNumber == 8) {
                    	scaleBarValue = getTargetFeedbackValue(input);
                    	pbFeedback8.setProgress(scaleBarValue);
                    }
                    else {
                    	tvFeedback10.setText(receivedMessage);
                    }
                }
                
                break;                
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                //Toast.makeText(getApplicationContext(), "Connected to " + msg.getData().getString(DEVICE_NAME), Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    public int getConnectionState() {
		return mSerialService.getState();
	}
    private boolean sendString(String outputData) {
    	try {
    		BluetoothRemoteActivity.mSerialService.write(outputData.getBytes());
    		return true;
    	}
    	catch(Exception e) {
    		return false;
    	}
    }
    
    private int getTargetFeedbackElement(String unformattedString) {
    	int feedbackElement = -1;
    	int indexOfEqualsSign;
    	String stringFeedbackElement;
    	
    	// Get the index of the = sign if there was one
    	indexOfEqualsSign = unformattedString.indexOf('=');
    	
    	if(indexOfEqualsSign != -1) {
    		// Get everything on the left side of the equals sign
    		stringFeedbackElement = unformattedString.substring(0, indexOfEqualsSign);
    		
    		// If the first two characters match 'SB' the its all good
    		if(stringFeedbackElement.length() == 3 && stringFeedbackElement.charAt(0) == 'S' && stringFeedbackElement.charAt(1) == 'B') {
    			String temp = stringFeedbackElement.substring(2, 3);
    			feedbackElement = Integer.parseInt(temp);
    		}
    	}
    	
    	return feedbackElement;
    }
    
    /*
     * We can assume at this stage that the string must be properly formatted before the '=' sign
     */
    private int getTargetFeedbackValue(String unformattedString) {
    	int feedbackElementValue = 0;
    	
    	int indexOfEqualsSign;
    	int indexOfEOL;
    	String stringFeedbackElementValue;
    	
    	// Get the index of the = sign if there was one
    	indexOfEqualsSign = unformattedString.indexOf('=');
    	indexOfEOL = unformattedString.indexOf('\r');
    	
    	if(indexOfEqualsSign != -1) {
    		if(indexOfEOL != -1) {
    			// Get everything on the right side of the equals sign
        		stringFeedbackElementValue = unformattedString.substring(indexOfEqualsSign+1, indexOfEOL);
    		}
    		else {
    			// Get everything on the right side of the equals sign
        		stringFeedbackElementValue = unformattedString.substring(indexOfEqualsSign+1, unformattedString.length());
    		}
    		    		
    		// If its less a 1 or 2 digit number its all good
    		if(stringFeedbackElementValue.length() > 0 && stringFeedbackElementValue.length() < 5) {
    			try {
    				feedbackElementValue = Integer.parseInt(stringFeedbackElementValue);
    			}
    			catch (Exception e) {
    				feedbackElementValue = 0;
    			}
    			
    			if (feedbackElementValue > 100)
    				feedbackElementValue = 100;
    			else if (feedbackElementValue < 0) {
    				feedbackElementValue = 0;
    			}
    		}
    	}
    	
    	return feedbackElementValue;
    }
	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor mySensor = event.sensor;
		 
		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	        long curTime = System.currentTimeMillis();
	        
	        if ((curTime - lastUpdate) > 100) {
	            long diffTime = (curTime - lastUpdate);
	            lastUpdate = curTime;
	            
	            float x = event.values[0];
		        float y = event.values[1];
		        float z = event.values[2];
		        
	        }
	    }
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
