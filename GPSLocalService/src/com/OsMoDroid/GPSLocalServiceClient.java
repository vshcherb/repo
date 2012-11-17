package com.OsMoDroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import org.json.JSONException;
import org.json.JSONObject;

import com.OsMoDroid.LocalService.LocalBinder;
import com.OsMoDroid.R;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import android.text.ClipboardManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GPSLocalServiceClient extends Activity implements ResultsListener{
	public static String key = "";
	public String login = "";
	// private JSONObject commandJSON;
	private int speed;
	private int speedbearing_gpx;
	private int bearing_gpx;
	private long notifyperiod = 30000;
	private int hdop_gpx;
	private int period_gpx;
	private int distance_gpx;
	private boolean sendsound = false;
	private boolean playsound = false;
	private boolean started = false;
	private boolean vibrate;
	private boolean usecourse;
	private int vibratetime;
	LocalService mService;
	boolean mBound = false;
	private int speedbearing;
	private int bearing;
	private boolean gpx = false;
	private boolean live = true;
	private int hdop;
	private int period;
	private int distance;
	private String pass = "";
	private String hash;
	private int n;
	private String submiturl;
	private String viewurl;
	private String pdaviewurl;
	private String device="";
	private String devicename="";
	private String position;
	private String sendresult;
	// private Timer timer;
	BroadcastReceiver receiver;
	private int sendcounter;
	private boolean usebuffer = false;
	private boolean usewake = false;
	MenuItem mi4;
	MenuItem mi5;
	MenuItem mi6;
	MenuItem mi7;
	MenuItem mi8;
	String version="Unknown";
	SharedPreferences settings;
	
	private ServiceConnection conn = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			 Log.d(getClass().getSimpleName(), "onserviceconnected");
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			invokeService();
			started = true;
//			if (started && ( conn == null || mService == null)) {
//				Log.d(getClass().getSimpleName(), "��� ����� � �������� - startcommand");
//			} else {
//				Log.d(getClass().getSimpleName(), "����� startcommand");
//				mService.startcomand();
//				Log.d(getClass().getSimpleName(), "������ ������ startcomand");
//			}	
			
		}

		public void onServiceDisconnected(ComponentName arg0) {

			mBound = false;
			// Log.d(getClass().getSimpleName(), "onservicedisconnected");
		}
	};
	
	public static String unescape (String s)
	{
	    while (true)
	    {
	        int n=s.indexOf("&#");
	        if (n<0) break;
	        int m=s.indexOf(";",n+2);
	        if (m<0) break;
	        try
	        {
	            s=s.substring(0,n)+(char)(Integer.parseInt(s.substring(n+2,m)))+
	                s.substring(m+1);
	        }
	        catch (Exception e)
	        {
	            return s;
	        }
	    }
	    s=s.replace("&quot;","\"");
	    s=s.replace("&lt;","<");
	    s=s.replace("&gt;",">");
	    s=s.replace("&amp;","&");
	    return s;
	}


	// @Override
	// protected void onPause(){
	//
	// super.onPause();
	//
	//
	// SharedPreferences settings = PreferenceManager
	// .getDefaultSharedPreferences(this);
	// SharedPreferences.Editor editor = settings.edit();
	// editor.putString("sendresult", sendresult);
	// editor.putInt("sendcounter", sendcounter);
	//
	// editor.commit();
	//
	// }

	@Override
	protected void onStop() {

		//Log.d(getClass().getSimpleName(), "onstop() gpsclient");
		// if (timer != null) {
		// timer.cancel();
		// }
		

		super.onStop();

	}

	// @Override
	//
	// public void onSaveInstanceState(Bundle saveInstanceState) {
	//
	// // ��������� �������������
	//
	//
	// // ��������� ��� ���������
	// saveInstanceState.putString("sendresult", sendresult);
	// saveInstanceState.putInt("sendcounter", sendcounter);
	//
	// super.onSaveInstanceState(saveInstanceState);
	//
	// }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (savedInstanceState != null && savedInstanceState.
		// containsKey("sendresult") && savedInstanceState.
		// containsKey("sendcounter"))
		// {
		// sendresult= savedInstanceState.getString("sendresult");
		// sendcounter= savedInstanceState.getInt("sendcounter");
		//
		//
		// }
		//
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		PreferenceManager.setDefaultValues(this, R.xml.pref, true);
		settings = PreferenceManager
				.getDefaultSharedPreferences(this);
	
		String strVersionName = getString(R.string.Unknow);
	
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			strVersionName = packageInfo.packageName + " "
					+ packageInfo.versionName;
			version=packageInfo.versionName;
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
		}
		setContentView(R.layout.main);
		setTitle(strVersionName);
		Button start = (Button) findViewById(R.id.startButton);
		Button exit = (Button) findViewById(R.id.exitButton);
		Button copy = (Button) findViewById(R.id.copyButton);
		Button send = (Button) findViewById(R.id.SendButton);
		Button forcesend = (Button) findViewById(R.id.forcesendButton);
		start.setEnabled(false);
		exit.setEnabled(false);
		forcesend.setEnabled(false);
		exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// if (timer != null) {
				// timer.cancel();
				// }
			
					
				
//				if (mBound) {
//					unbindService(conn);
//					mBound = false;
//				}
				
				if (conn == null || mService == null) {
					Log.d(getClass().getSimpleName(), "��� ����� � �������� - stopcommand");
				} else {
					Log.d(getClass().getSimpleName(), "����� stopcommand");
					mService.stopcomand();
					Log.d(getClass().getSimpleName(), "������ ������ stopcomand");
				}	
				stop();
				Button start = (Button) findViewById(R.id.startButton);
				Button stop = (Button) findViewById(R.id.exitButton);
				Button forcesend = (Button) findViewById(R.id.forcesendButton);
				
				start.setEnabled(true);
				forcesend.setEnabled(false);
				stop.setEnabled(false);
				started = false;
				updateServiceStatus();

			}
		});
		copy.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				if (viewurl != null)
					clipboard.setText(viewurl);
				// clipboard.setPrimaryClip(clip);
			}
		});
		forcesend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(getClass().getSimpleName(), "forcesend click");
				if (conn == null || mService == null) {
					Log.d(getClass().getSimpleName(), "��� ����� � ��������");
				} else {
					Log.d(getClass().getSimpleName(), "����� �������� �������");
					mService.sendPosition();
					Log.d(getClass().getSimpleName(), "������ ������ �������� �������");
				}	
				
			}
		});
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Intent sendIntent = new
				// Intent(android.content.Intent.ACTION_SEND);
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("text/plain");
				sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, viewurl);
				startActivity(Intent.createChooser(sendIntent, "Email"));

			}
		});

		start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
startlocalservice();
//				bindService();
				Button start = (Button) findViewById(R.id.startButton);
				Button stop = (Button) findViewById(R.id.exitButton);
				Button forcesend = (Button) findViewById(R.id.forcesendButton);
				start.setEnabled(false);
				forcesend.setEnabled(true);
				stop.setEnabled(true);
				started = true;
			
				
				
//				if (!key.equals("")){
//				String[] a={"device"};
//				String[] b={device};
//				String[] params = {netutil.buildcommand(GPSLocalServiceClient.this,"start",a,b),"false","","start"};
//				starttask=	new netutil.MyAsyncTask(GPSLocalServiceClient.this);
//				starttask.execute(params) ;
//				}
				
				//Log.d(getClass().getSimpleName(),buildcommand("start",a,b).toString());
				// timer = new Timer();
				// timer.scheduleAtFixedRate(new TimerTask() {
				// @Override
				// public void run() {
				// runOnUiThread(new Runnable() {
				// public void run() {
				// if (mBound)
				// invokeService();
				// }
				// });
				//
				// }
				// }, 0, 1000);
			}
		});

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				TextView t = (TextView) findViewById(R.id.Location);
				sendcounter = intent.getIntExtra("sendcounter", 0);
				position = intent.getStringExtra("position");
				sendresult = intent.getStringExtra("sendresult");
				String stat = intent.getStringExtra("stat");
				String startmessage = intent.getStringExtra("startmessage");
				if (!(startmessage==null)) {
					AlertDialog alertdialog = new AlertDialog.Builder(
							GPSLocalServiceClient.this).create();
					alertdialog.setTitle("��������� �� �������");

					alertdialog.setMessage(startmessage);
							
							

					alertdialog.setButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									
									return;
								}
							});
					
					alertdialog.show();
					
				}
				
				
				
				if (position == null){position = context.getString(R.string.NotDefined);}

				// Log.d("lbr", "Message received: " +
				// intent.getStringExtra("position") );
				// Log.d("lbr", "Message received: " +
				// intent.getStringExtra("sendcounter") );
				// Log.d("lbr", "Message received: " +
				// intent.getStringExtra("sendresult") );
				if (sendresult == null){	sendresult = "";}
				t.setText(
						//getString(R.string.location) + 
						position+"\n"+stat);
				TextView t2 = (TextView) findViewById(R.id.Send);

				updateServiceStatus();
				if (!(sendresult == null)){t2.setText(getString(R.string.Sended) + (sendresult));}
			}
			// sendresult=time+ " "+ sendresult;

			// Log.d("lbr", "Message received: " +
			// intent.getStringExtra("position") );

		};

		registerReceiver(receiver, new IntentFilter("OsMoDroid"));
		bindService();
		// Log.d(getClass().getSimpleName(), "onCreate() gpsclient");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Log.d(getClass().getSimpleName(), "onResume() gpsclient");
		ReadPref();
		//WritePref();
		
		started = checkStarted();
		updateServiceStatus();
		OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String keychanged) {
			    if (keychanged.equals("hash")) {
			    	Log.d(getClass().getSimpleName(), "�������� ���");
			    	device="";
			    	devicename="";
			    	
			    	SharedPreferences.Editor editor = settings.edit();
			    editor.remove("device");
			    editor.remove("devicename");
			    editor.commit();
			    }
			    if (keychanged.equals("login")) {
			    	
			    	Log.d(getClass().getSimpleName(), "�������� login");
			    	key="";}
			    if (started){bindService();}
			  }
			};

		
			settings.registerOnSharedPreferenceChangeListener(listener);

		
	
		if (started) {
			Button forcesend = (Button) findViewById(R.id.forcesendButton);
			Button start = (Button) findViewById(R.id.startButton);
			Button stop = (Button) findViewById(R.id.exitButton);
			forcesend.setEnabled(true);
			start.setEnabled(false);
			stop.setEnabled(true);
			bindService();
			// timer = new Timer();
			// timer.scheduleAtFixedRate(new TimerTask() {
			// @Override
			// public void run() {
			// runOnUiThread(new Runnable() {
			// public void run() {
			// if (mBound)
			// invokeService();
			// }
			// });
			//
			// }
			// }, 0, 1000);

		} else {
			Button forcesend = (Button) findViewById(R.id.forcesendButton);
			Button start = (Button) findViewById(R.id.startButton);
			Button stop = (Button) findViewById(R.id.exitButton);
			forcesend.setEnabled(false);
			start.setEnabled(true);
			stop.setEnabled(false);
		}

		if (hash.equals("") && live) {
			RequestAuthTask requestAuthTask = new RequestAuthTask();
			requestAuthTask.execute();
		}
		TextView t2 = (TextView) findViewById(R.id.URL);
		t2.setText("���: "+settings.getString("devicename", "")+". " +getString(R.string.Adres) + viewurl);
		//t2.setText(getString(R.string.Adres) + viewurl);
		Linkify.addLinks(t2, Linkify.ALL);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu menu1 = menu.addSubMenu(Menu.NONE, 11, 4,"�������");
		SubMenu menu2 = menu.addSubMenu(Menu.NONE, 12, 4,"�������������");
		
		MenuItem auth = menu2.add(0, 1, 0, R.string.RepeatAuth);
		MenuItem mi = menu.add(0, 2, 0, R.string.Settings);
		MenuItem mi3 = menu2.add(0, 3, 0, R.string.EqualsParameters);
		 mi4 = menu1.add(0, 4, 0, R.string.getkey);
		 mi5 = menu1.add(0, 5, 0, R.string.getadres);
		 mi6 = menu1.add(0, 6, 0, R.string.getdevice);
		 mi7 = menu1.add(0, 7, 0, R.string.enterchanel);
		 mi8 = menu1.add(0, 8, 0, "������������� ������");
		mi.setIntent(new Intent(this, PrefActivity.class));
		mi8.setIntent(new Intent(this, SimLinks.class));
		
		// Log.d(getClass().getSimpleName(), "onCreateOptionsmenu() gpsclient");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu (Menu menu){

		
		if (login.equals("")){ mi4.setEnabled(false);} else {mi4.setEnabled(true);}
		if (key.equals("")){ mi6.setEnabled(false); mi5.setEnabled(false);} else { mi6.setEnabled(true); mi5.setEnabled(true);}
		if (settings.getString("device", "").equals("")){
			mi7.setEnabled(false);
			mi8.setEnabled(false);
				
		}
		else {
			mi7.setEnabled(true);
			mi8.setEnabled(true);
			//mi7.isEnabled();
			}
		
		return super.onPrepareOptionsMenu(menu);}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			// case 1:
			AlertDialog alertdialog = new AlertDialog.Builder(
					GPSLocalServiceClient.this).create();
			alertdialog.setTitle(getString(R.string.AgreeRepeatAuth));

			alertdialog.setMessage(getString(R.string.ChangeAdresMonitor));

			alertdialog.setButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							RequestAuthTask requestAuthTask = new RequestAuthTask();
							requestAuthTask.execute();
							return;
						}
					});
			alertdialog.setButton2(getString(R.string.No),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							return;
						}
					});
			alertdialog.show();
		}
		if (item.getItemId() == 3) {
			AlertDialog alertdialog1 = new AlertDialog.Builder(
					GPSLocalServiceClient.this).create();
			alertdialog1.setTitle(getString(R.string.AgreeParameterEqual));

			alertdialog1
					.setMessage(getString(R.string.TrackRecordParameterChanges));

			alertdialog1.setButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							speedbearing_gpx = speedbearing;
							bearing_gpx = bearing;

							hdop_gpx = hdop;
							period_gpx = period;
							distance_gpx = distance;
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("period_gpx", Integer.toString(period_gpx));
							editor.putString("distance_gpx", Integer.toString(distance_gpx));
							editor.putString("speedbearing_gpx", Integer.toString(speedbearing_gpx));
							editor.putString("bearing_gpx", Integer.toString(bearing_gpx));
							editor.putString("hdop_gpx", Integer.toString(hdop_gpx));
							editor.commit();
						//	WritePref();
							return;
						}
					});

			alertdialog1.setButton2(getString(R.string.No),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							return;
						}
					});
			alertdialog1.show();

		}
		if (item.getItemId() == 4) {

			// final View textEntryView = factory.inflate(R.layout.dialog,
			// null);
			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			final TextView txv3 = new TextView(this);
			txv3.setText("����������� ������:");
			layout.addView(txv3);
			
			final EditText input = new EditText(this);
			//input2.setText("���� ���");
			layout.addView(input);
		//	final EditText input = new EditText(this);
			final TextView txv4 = new TextView(this);
			txv4.setText("����������� ������ ����� �������� �� ������ http://esya.ru/app.html?act=add ����� �����������");
			Linkify.addLinks(txv4, Linkify.ALL);
			layout.addView(txv4);
			
			AlertDialog alertdialog3 = new AlertDialog.Builder(
					GPSLocalServiceClient.this)
					.setTitle("����������� ����������")
					.setView(layout)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// pass=textEntryView.
									pass = input.getText().toString();
									/* User clicked OK so do some stuff */
									if (!(pass.equals(""))) {
										String[] params = {
												"http://auth.api.esya.ru",
												"true",
												"login=" + login + "&password="
														+ pass + "&key=G94y",
												"auth" };
										RequestCommandTask Rq = new RequestCommandTask();
										Rq.execute(params);
									} else {
										Toast.makeText(
												GPSLocalServiceClient.this,
												R.string.noappcode, 5).show();
									}
								}
							})
					.setNegativeButton(R.string.No,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked cancel so do some stuff */
								}
							}).create();

			alertdialog3.show();

			// 6564 2638 7281 2680

		}
		if (item.getItemId() == 5) {
			if (!(key.equals(""))) {
//				String[] params = {
//						"http://api.esya.ru/?system=om&action=device_link&hash="
//								+ hash
//								+ "&n="
//								+ n
//								+ "&key="
//								+ key
//								+ "&signature="
//								+ SHA1(
//										"system:om;action:device_link;hash:"
//												+ hash
//												+ ";n:"
//												+ n
//												+ ";key:"
//												+ key
//												+ ";"
//												+ "--"
//												+ "JGu473g9DFj3y_gsh463j48hdsgl34lqzkvnr420gdsg-32hafUehcDaw3516Ha-aghaerUhhvF42123na38Agqmznv_46bd-67ogpwuNaEv6")
//										.substring(1, 25), "false", "",
//						"device_link" };
				String[] a={"device"};
				String[] b={device};
				String[] params = {netutil.buildcommand(GPSLocalServiceClient.this,"device_link",a,b),"false","","device_link"};
				new netutil.MyAsyncTask(GPSLocalServiceClient.this).execute(params) ;
				
				
				
				
				// String[]
				// params={"http://api.esya.ru/?system=om&action=device&key="+commandJSON.optString("key")+"&signature="+SHA1("system:om;action:device;key:"+commandJSON.optString("key")+";"+"--"+"JGu473g9DFj3y_gsh463j48hdsgl34lqzkvnr420gdsg-32hafUehcDaw3516Ha-aghaerUhhvF42123na38Agqmznv_46bd-67ogpwuNaEv6").substring(1,
				// 25),"false",""};
				// +commandJSON.optString("key")+
			//	Log.d(getClass().getSimpleName(), params[0]);
			//	RequestCommandTask Rq = new RequestCommandTask();
			//	Rq.execute(params);
			} else {
				Toast.makeText(GPSLocalServiceClient.this, R.string.nokey, 5)
						.show();
			}

		}

		if (item.getItemId() == 6) {
			if (!(key.equals(""))) {
				String[] params = {
						"http://api.esya.ru/?system=om&action=device"
								+ "&key="
								+ key
								+ "&signature="
								+ SHA1(
										"system:om;action:device;key:"
												+ key
												+ ";"
												+ "--"
												+ "JGu473g9DFj3y_gsh463j48hdsgl34lqzkvnr420gdsg-32hafUehcDaw3516Ha-aghaerUhhvF42123na38Agqmznv_46bd-67ogpwuNaEv6")
										.substring(1, 25), "false", "",
						"device" };
				// String[]
				// params={"http://api.esya.ru/?system=om&action=device&key="+commandJSON.optString("key")+"&signature="+SHA1("system:om;action:device;key:"+commandJSON.optString("key")+";"+"--"+"JGu473g9DFj3y_gsh463j48hdsgl34lqzkvnr420gdsg-32hafUehcDaw3516Ha-aghaerUhhvF42123na38Agqmznv_46bd-67ogpwuNaEv6").substring(1,
				// 25),"false",""};
				// +commandJSON.optString("key")+
				Log.d(getClass().getSimpleName(), params[0]);
				RequestCommandTask Rq = new RequestCommandTask();
				Rq.execute(params);
			} else {
				Toast.makeText(GPSLocalServiceClient.this, R.string.nokey, 5)
						.show();
			}

		}
		if (item.getItemId() == 7) {
			if (!(settings.getString("device", "").equals(""))) { 
			
			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			final TextView txv3 = new TextView(this);
			txv3.setText("���� ���:");
			layout.addView(txv3);
			
			final EditText input2 = new EditText(this);
			//input2.setText("���� ���");
			layout.addView(input2);
			
final TextView txv1 = new TextView(this);
txv1.setText("��� ������:");
layout.addView(txv1);
			final EditText input = new EditText(this);
			//input.setText("��� ������");
			layout.addView(input);

			final CheckBox chb1 = new CheckBox(this);
			chb1.setText("��������� �����");
			
			layout.addView(chb1);
			
			final TextView txv2 = new TextView(this);
			txv2.setText("���� ������:");
			//txv2.setEnabled(false);
			layout.addView(txv2);
			
			
			
			final EditText input1 = new EditText(this);
			//input1.setText("���� ������");
			input1.setEnabled(false);
			layout.addView(input1);
			
			chb1.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
			   

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
				if (isChecked){
					input1.setEnabled(true);}
				if (!isChecked){
					input1.setEnabled(false);}

					
				}
			});
			
			
			
			
			
			AlertDialog alertdialog4 = new AlertDialog.Builder(
					GPSLocalServiceClient.this)
					.setTitle("����������� � �����")
					.setView(layout)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// pass=textEntryView.
									
									String canalid = input.getText().toString();
									String canalkey = input1.getText().toString();
									String canalname = input2.getText().toString();

									/* User clicked OK so do some stuff */
									if ( !(canalid.equals("")) && !(canalname.equals("")) && ( chb1.isChecked()&&(!(canalkey.equals(""))) || !chb1.isChecked() )  )
									{
										String[] params = {
												"http://api.esya.ru/?system=om&action=channel_enter"+"&code="+canalid
												+"&pass="+canalkey
												+"&device="+settings.getString("device", "")
												+"&name="+canalname
														+ "&key="
														+ key
														+ "&signature="
														+ SHA1(
																"system:om;action:channel_enter;code:"+canalid+";pass:"+canalkey+";device:"+settings.getString("device", "")+";name:"+canalname+ ";key:"
																		+ key
																		+ ";"
																		+ "--"
																		+ "JGu473g9DFj3y_gsh463j48hdsgl34lqzkvnr420gdsg-32hafUehcDaw3516Ha-aghaerUhhvF42123na38Agqmznv_46bd-67ogpwuNaEv6")
																.substring(1, 25),
												"false",
												"",
												"channel_enter" };
										RequestCommandTask Rq = new RequestCommandTask();
										Rq.execute(params);
									} else {
										Toast.makeText(
												GPSLocalServiceClient.this,
												"������� �� ��� ������", 5).show();
									}
								}
							})
					.setNegativeButton(R.string.No,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked cancel so do some stuff */
								}
							}).create();

			alertdialog4.show();

			// 6564 2638 7281 2680

		}else {Toast.makeText(
				GPSLocalServiceClient.this,
				"�������������� ��������� ����������", 5).show();}
		
		} 
		
		return super.onOptionsItemSelected(item);
		
	}

//	private void WritePref() {
//		// Log.d(getClass().getSimpleName(), "onWrightPref() gpsclient");
//		
//		SharedPreferences.Editor editor = settings.edit();
//		editor.putString("speed", Integer.toString(speed));
//		editor.putString("period", Integer.toString(period));
//		editor.putString("distance", Integer.toString(distance));
//		editor.putString("hash", hash);
//		editor.putString("n", Integer.toString(n));
//		editor.putString("submit-url", submiturl);
//		editor.putString("view-url", viewurl);
//		editor.putString("pda-view-url", pdaviewurl);
//		editor.putString("speedbearing", Integer.toString(speedbearing));
//		editor.putString("bearing", Integer.toString(bearing));
//		editor.putBoolean("gpx", gpx);
//		editor.putBoolean("live", live);
//		editor.putString("hdop", Integer.toString(hdop));
//		editor.putString("vibratetime", Integer.toString(vibratetime));
//		editor.putBoolean("vibrate", vibrate);
//		editor.putBoolean("playsound", playsound);
//		editor.putBoolean("usecourse", usecourse);
//		editor.putString("period_gpx", Integer.toString(period_gpx));
//		editor.putString("distance_gpx", Integer.toString(distance_gpx));
//		editor.putString("speedbearing_gpx", Integer.toString(speedbearing_gpx));
//		editor.putString("bearing_gpx", Integer.toString(bearing_gpx));
//		editor.putString("hdop_gpx", Integer.toString(hdop_gpx));
//		editor.putBoolean("usewake", usewake);
//		editor.putBoolean("usebuffer", usebuffer);
//		editor.putBoolean("sendsound", sendsound);
//		editor.putString("notifyperiod", Long.toString(notifyperiod));
//		// editor.putString("pass", pass);
//		editor.putString("login", login);
//		editor.putString("key", key);
//		editor.commit();
//
//	}

	private boolean checkStarted() {
		
		// Log.d(getClass().getSimpleName(), "oncheckstartedy() gpsclient");
		return settings.getBoolean("started", false);

	}

	private void ReadPref() {
		// Log.d(getClass().getSimpleName(), "readpref() gpsclient");
		

		speed =  Integer.parseInt(settings.getString("speed", "3").equals(
				"") ? "3" : settings.getString("speed", "3"));
		period = Integer.parseInt(settings.getString("period", "10000").equals(
				"") ? "10000" : settings.getString("period", "10000"));
		distance = Integer.parseInt(settings.getString("distance", "50")
				.equals("") ? "50" : settings.getString("distance", "50"));
		hash = settings.getString("hash", "");
		n = Integer.parseInt(settings.getString("n", "0").equals("") ? "0"
				: settings.getString("n", "0"));
		submiturl = settings.getString("submit-url", "");
		viewurl = settings.getString("view-url", "");
		pdaviewurl = settings.getString("pda-view-url", "");
		speedbearing = Integer.parseInt(settings.getString("speedbearing", "2")
				.equals("") ? "2" : settings.getString("speedbearing", "2"));
		bearing = Integer.parseInt(settings.getString("bearing", "10").equals(
				"") ? "10" : settings.getString("bearing", "2"));
		hdop = Integer
				.parseInt(settings.getString("hdop", "30").equals("") ? "30"
						: settings.getString("hdop", "30"));
		gpx = settings.getBoolean("gpx", false);
		live = settings.getBoolean("live", true);
		vibrate = settings.getBoolean("vibrate", false);
		usecourse = settings.getBoolean("usecourse", false);
		vibratetime = Integer.parseInt(settings.getString("vibratetime", "200")
				.equals("") ? "200" : settings.getString("vibratetime", "0"));
		playsound = settings.getBoolean("playsound", false);
		period_gpx = Integer.parseInt(settings.getString("period_gpx", "0")
				.equals("") ? "0" : settings.getString("period_gpx", "0"));
		distance_gpx = Integer.parseInt(settings.getString("distance_gpx", "0")
				.equals("") ? "0" : settings.getString("distance_gpx", "0"));
		speedbearing_gpx = Integer.parseInt(settings.getString(
				"speedbearing_gpx", "0").equals("") ? "0" : settings.getString(
				"speedbearing_gpx", "0"));
		bearing_gpx = Integer.parseInt(settings.getString("bearing_gpx", "0")
				.equals("") ? "0" : settings.getString("bearing", "0"));
		hdop_gpx = Integer.parseInt(settings.getString("hdop_gpx", "30")
				.equals("") ? "30" : settings.getString("hdop_gpx", "30"));
		usebuffer = settings.getBoolean("usebuffer", false);
		usewake = settings.getBoolean("usewake", false);
		notifyperiod = Integer.parseInt(settings.getString("notifyperiod",
				"30000").equals("") ? "30000" : settings.getString(
				"notifyperiod", "30000"));
		sendsound = settings.getBoolean("sendsound", false);
		// pass = settings.getString("pass", "");
		login = settings.getString("login", "");
		key = settings.getString("key", "");
		device = settings.getString("device", "");
	}

	private void startlocalservice(){
		Intent i = new Intent(this, LocalService.class);

		startService(i);
		started = true;
	}
	
	private void bindService() {
		// Log.d(getClass().getSimpleName(), "bimdservice() gpsclient");
		// if(!mBound ) {
		Intent i = new Intent(this, LocalService.class);

		//startService(i);
		// Log.d(getClass().getSimpleName(),
		// "��������� �����������() gpsclient");
		// Log.d(getClass().getSimpleName(), "conn=" + conn);
		bindService(i, conn, Context.BIND_AUTO_CREATE);
		mBound = true;
		
		updateServiceStatus();
	}

	private void stop() {
		 Log.d(getClass().getSimpleName(), "stop() gpsclient");
		mService.stopServiceWork();
		 Intent i = new Intent(this, LocalService.class);
		stopService(i);

	}

	private void invokeService() {
		// Log.d(getClass().getSimpleName(), "invokeservice() gpsclient");

		if (conn == null || mService == null) {

		} else {

			mService.refresh();
//			position = mService.getPosition();
//			sendresult = mService.getSendResult();
//			sendcounter = mService.getSendCounter();
//			TextView t = (TextView) findViewById(R.id.Location);
//			t.setText(getString(R.string.location) + position);
//			TextView t2 = (TextView) findViewById(R.id.Send);
//			if (sendresult == null)
//				sendresult = "";
//
//			t2.setText(getString(R.string.Sended) + sendresult);
			updateServiceStatus();

		}
	}

	private void updateServiceStatus() {
		// Log.d(getClass().getSimpleName(), "updateservicestatus() gpsclient");
		String startStatus = started ? getString(R.string.Running)
				: getString(R.string.NotRunning);
		String statusText = getString(R.string.Status) + startStatus
				+ getString(R.string.Sendcount) + sendcounter;
		TextView t = (TextView) findViewById(R.id.serviceStatus);
		t.setText(statusText);
	}

	@Override
	protected void onDestroy() {
if (mBound) {
			
			try {
				unbindService(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(getClass().getSimpleName(), "���������� ��� ������������ �� �������");
				e.printStackTrace();
			}
		}
		
		conn = null;
		// releaseService();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		// Log.d(getClass().getSimpleName(), "onDestroy() gpsclient");
		super.onDestroy();
	}

	public String getPage(String adr, boolean dopost, String post)
			throws IOException {
		// Log.d(getClass().getSimpleName(), "getpage() gpsclient");
		Log.d(getClass().getSimpleName(), adr);
		HttpURLConnection con;
		int portOfProxy = android.net.Proxy.getDefaultPort();
		if (portOfProxy > 0) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
					android.net.Proxy.getDefaultHost(), portOfProxy));
			con = (HttpURLConnection) new URL(adr).openConnection(proxy);
		} else {
			con = (HttpURLConnection) new URL(adr).openConnection();
		}
		con.setReadTimeout(10000);
		con.setConnectTimeout(30000);
		if (dopost) {
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			OutputStream os = con.getOutputStream();
			os.write(post.getBytes());
			Log.d(this.getClass().getName(), "��� POST��" + post);
			os.flush();
			os.close();
		}

		con.connect();
		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			// String str=inputStreamToString(con.getInputStream());
			return inputStreamToString(con.getInputStream());
		} else {
			Log.d(this.getClass().getName(),
					Integer.toString(con.getResponseCode()));
			// String str=inputStreamToString(con.getInputStream());
			// Log.d(this.getClass().getName(),str);
			// return str;
			return getString(R.string.ErrorRecieve);

		}

	}

	public String inputStreamToString(InputStream in) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(in));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	public static String bytesToHex(byte[] b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}

	public static String SHA1(String text) {
		//Log.d(this.getClass().getName(), text);
		MessageDigest md;
		byte[] sha1hash = new byte[40];
		try {
			md = MessageDigest.getInstance("SHA-1");

			// md.update(text.getBytes());//, 0, text.length());
			sha1hash = md.digest(text.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bytesToHex(sha1hash);
	}

	private class RequestAuthTask extends AsyncTask<Void, Void, Void> {
		private String authtext;
		String adevice;
		private Boolean Err = true;
		ProgressDialog dialog = ProgressDialog.show(GPSLocalServiceClient.this,
				"", "������ �����������, ��������� ����������...", true);

		// Dialog dial = Dialog.

		protected void onPreExecute() {
			// dialog.dismiss();
			dialog.show();
		}

		protected void onPostExecute(Void params) {
			// Log.d(this.getClass().getName(), "������� �������� ����������.");
			dialog.dismiss();
			if (Err) {
				Toast.makeText(GPSLocalServiceClient.this,
						getString(R.string.CheckInternet), 5).show();
			} else {
				
				
				//WritePref();
				SharedPreferences.Editor editor = settings.edit();

				editor.putString("hash", hash);
				editor.commit();
				editor.putString("n", Integer.toString(n));
				editor.putString("submit-url", submiturl);
				editor.putString("view-url", viewurl);
				editor.putString("pda-view-url", pdaviewurl);
				editor.putString("device", adevice);
				editor.commit();
				
				
				
				ReadPref();
				findViewById(R.id.startButton).setEnabled(true);
				TextView t2 = (TextView) findViewById(R.id.URL);
				t2.setText(getString(R.string.Adres) + viewurl);
				Linkify.addLinks(t2, Linkify.ALL);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				// Log.d(this.getClass().getName(),
				// "�������� ������ �����������.");
				//authtext = getPage("http://auth.t.esya.ru/?who=OsMoDroid",false, "");
				authtext = getPage("http://auth.t.esya.ru/?act=new&who=OsMoDroid&ver="+version,
						false, "");
				
				JSONObject auth = new JSONObject(authtext);
				Log.d(this.getClass().getName(), auth.toString());
				hash = auth.getString("hash");
				n = auth.getInt("n");
				submiturl = auth.getString("submit-url");
				viewurl = auth.getString("view-url");
				pdaviewurl = auth.getString("pda-view-url");
				adevice= auth.getString("device");
				// Log.d(this.getClass().getName(), "����������� �����������.");
				if (hash.equals("")) {
					// Log.d(this.getClass().getName(), "�����.");
					Err = true;
				}
				Err = false;
			} catch (IOException e) {

				e.printStackTrace();
				// Log.d(this.getClass().getName(), "�����2.");
				Err = true;
				// finish();

			} catch (JSONException e) {
				e.printStackTrace();
				// Log.d(this.getClass().getName(), "�����3.");
				Err = true;

			}
			return null;
		}
	}

	public class RequestCommandTask extends AsyncTask<String, Void, String> {
		private String Commandtext;
		private String adevice;
		private String akey;
		private String aviewurl;
		private String adevicename;
		// private Boolean Err = true;
		ProgressDialog dialog = ProgressDialog.show(GPSLocalServiceClient.this,
				"", "���������� �������, ��������� ����������...", true);

		// Dialog dial = Dialog.

		protected void onPreExecute() {
			// dialog.dismiss();
			dialog.show();
		}

		@Override
		protected void onPostExecute(String resultString) {

			dialog.dismiss();
			if (resultString == null) {
				Toast.makeText(GPSLocalServiceClient.this,
						getString(R.string.CheckInternet), 5).show();
			} else {
				// commandJSON=resultJSON;
if (!(adevice==null)){device=adevice;
SharedPreferences.Editor editor = settings.edit();
editor.putString("device", adevice);
editor.commit();}
if (!(adevicename==null)){devicename=unescape(adevicename);
SharedPreferences.Editor editor = settings.edit();
editor.putString("devicename", unescape(adevicename));
editor.commit();

}
if (!(akey==null))
{key=akey;
SharedPreferences.Editor editor = settings.edit();
editor.putString("key", key);
editor.commit();}
if (!(aviewurl==null)){viewurl=aviewurl;}

				TextView t2 = (TextView) findViewById(R.id.URL);
				t2.setText("���: "+devicename+". " +getString(R.string.Adres) + viewurl);
				Linkify.addLinks(t2, Linkify.ALL);

				SharedPreferences.Editor editor = settings.edit();

				editor.putString("view-url", viewurl);


				editor.commit();

				ReadPref();
				Toast.makeText(GPSLocalServiceClient.this,
						resultString.toString(), 5).show();
			}

		}

		@Override
		protected String doInBackground(String... params) {
			JSONObject resJSON = null;
			String returnstr = null;
			try {
				// Log.d(this.getClass().getName(),
				// "�������� ������ �����������.");
				Commandtext = getPage(params[0],
						Boolean.parseBoolean(params[1]), params[2]);
				Log.d(this.getClass().getName(), Commandtext);
				resJSON = new JSONObject(Commandtext);
				//Toast.makeText(this,resJSON.optString("state")+" "+ resJSON.optString("error_description"),5).show();
				// return new JSONObject(Commandtext);

				// Log.d(this.getClass().getName(), "����������� �����������.");

			} catch (IOException e) {

				e.printStackTrace();
				return null;
				// Log.d(this.getClass().getName(), "�����2.");
				// Err = true;
				// finish();

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
				// Log.d(this.getClass().getName(), "�����3.");
				// Err = true;

			}
			if (resJSON == null)
				return null;
			else {
				// commandJSON=resultJSON;
				if (params[3].equals("auth")) {
					if (!(resJSON.optString("key").equals(""))) {
						akey = resJSON.optString("key");
						returnstr = resJSON.optString("state")+" "+ resJSON.optString("error_description");
					}
				}

				if (params[3].equals("device_link")) {
					if (!(resJSON.optString("url").equals(""))) {
						aviewurl = resJSON.optString("url");
						returnstr = resJSON.optString("state")+" "+ resJSON.optString("error_description");
					}
				}

				if (params[3].equals("device")) {
					if (!(resJSON.optJSONArray("devices") == null)) {
						for (int i = 0; i < resJSON.optJSONArray("devices")
								.length(); i++) {
							try {
								if (resJSON.optJSONArray("devices")
										.getJSONObject(i).getString("hash")
										.equals(hash)) {
									adevice = resJSON.optJSONArray("devices")
											.getJSONObject(i).getString("u");
									adevicename=resJSON.optJSONArray("devices")
											.getJSONObject(i).getString("name");
									Log.d(this.getClass().getName(), adevice);
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						returnstr = resJSON.optString("state")+" "+ resJSON.optString("error_description");
//						if (adevice == null) {
//							returnstr = "���������� ������ �� �������";
//						} else {
//							
//							returnstr = "���������� �������";
//						}

					}

				}

				if (params[3].equals("channel_enter")) {
					if (!(resJSON.optString("state").equals(""))) {
						returnstr = resJSON.optString("state")+" "+ resJSON.optString("error_description");
						
					} else {
						returnstr = "��������� ����� � ����� �� �������";
					}
				}
				
				if (params[3].equals("get_device_links")) {
					if (!(resJSON.optString("state").equals(""))) {
						returnstr = resJSON.optJSONArray("links")+" "+ resJSON.optString("error_description");
						
					} else {
						returnstr = "������ �� �������";
					}
				}
				
				// Toast.makeText(GPSLocalServiceClient.this,resJSON.toString(),5).show();

			}

			return returnstr;

			// return null;
		}
	}

	
	
	public void onResultsSucceeded(APIComResult result) {
		
		if (result.Jo==null&&result.ja==null ){Toast.makeText(this,"�� ������� �������� ����� �� �������",5).show();}
		
		

			 
		if (result.Command.equals("device_link")&& !(result.Jo==null)) {
			Toast.makeText(this,result.Jo.optString("state")+" "+ result.Jo.optString("error_description"),5).show();
			if (!(result.Jo.optString("url").equals(""))) {
				viewurl = result.Jo.optString("url");
				TextView t2 = (TextView) findViewById(R.id.URL);
				t2.setText("���: "+devicename+". " +getString(R.string.Adres) + viewurl);
				Linkify.addLinks(t2, Linkify.ALL);

				SharedPreferences.Editor editor = settings.edit();

				editor.putString("view-url", viewurl);


				editor.commit();
				
				//returnstr = "URL ������";
			} else {
				Toast.makeText(this, "URL ������ �� �������",Toast.LENGTH_LONG ).show();
			}
		}
			
			//Log.d(getClass().getSimpleName(),"��������� ����");
		
			 
			 
			 
			
	}

}
