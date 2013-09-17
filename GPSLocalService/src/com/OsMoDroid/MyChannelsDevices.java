package com.OsMoDroid;import java.text.SimpleDateFormat;import java.util.ArrayList;import java.util.HashMap;import java.util.Arrays;import java.util.Iterator;import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import com.OsMoDroid.GPSLocalServiceClient.RequestCommandTask;import android.app.Activity;import android.app.AlertDialog;import android.app.ListActivity;import android.content.Context;import android.content.DialogInterface;import android.content.Intent;import android.content.SharedPreferences;import android.net.Uri;import android.os.Bundle;import android.preference.PreferenceManager;import android.text.ClipboardManager;import android.text.format.Time;import android.text.util.Linkify;import android.util.Log;import android.view.ContextMenu;import android.view.Menu;import android.view.ContextMenu.ContextMenuInfo;import android.view.MenuItem;import android.view.View;import android.view.View.OnClickListener;import android.widget.AdapterView;import android.widget.CheckBox;import android.widget.CompoundButton;import android.widget.AdapterView.AdapterContextMenuInfo;import android.widget.AdapterView.OnItemClickListener;import android.widget.CompoundButton.OnCheckedChangeListener;import android.widget.ArrayAdapter;import android.widget.Button;import android.widget.EditText;import android.widget.LinearLayout;import android.widget.ListView;import android.widget.TextView;import android.widget.Toast;public class MyChannelsDevices extends Activity implements ResultsListener{	 ListView lv1;	 ListView lv2;	 SharedPreferences settings;	Button sendButton;	EditText input;		 final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");	 //	public MyChannelsDevices() {		// TODO Auto-generated constructor stub	}		/** Called when the activity is first created. */	@Override	public void onCreate(Bundle savedInstanceState) {	    super.onCreate(savedInstanceState);	    setContentView(R.layout.mychannelsdevices);	    settings  = PreferenceManager.getDefaultSharedPreferences(this);	    	   LocalService.currentchanneldeviceList= LocalService.channelList.get(getIntent().getIntExtra("CHANNELPOS", -1)).deviceList;	   LocalService.currentChannel= LocalService.channelList.get(getIntent().getIntExtra("CHANNELPOS", -1)); 	   	    LocalService.channelsDevicesAdapter = new ChannelsDevicesAdapter(getApplicationContext(),R.layout.channelsdeviceitem,  LocalService.currentchanneldeviceList);LocalService.channelsmessagesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, LocalService.currentChannel.messagesstringList );	    lv1 = (ListView) findViewById(R.id.mychannelsdeviceslistView);	    lv2 = (ListView) findViewById(R.id.mychannelsmessages);	    input =(EditText) findViewById(R.id.mychannelsdeviceseditText1);	    input.requestFocus();	    sendButton= (Button) findViewById(R.id.mychanneldevicesendButton);sendButton.setOnClickListener(new OnClickListener() {	public void onClick(View v) {		if (!(input.getText().toString().equals(""))) {			JSONObject postjson = new JSONObject();			try {			postjson.put("text", input.getText().toString());			postjson.put("channel", LocalService.currentChannel.u);			postjson.put("device", settings.getString("device", ""));			//http://apim.esya.ru/?key=H8&query=om_channel_chat_post&format=jsonp			//json={"channel":"51","device":"40","text":"789"}			netutil.newapicommand((Context)MyChannelsDevices.this, "om_channel_chat_post","json="+postjson.toString());			input.setText("");			} catch (JSONException e) {				// TODO Auto-generated catch block				e.printStackTrace();			}		}	}});	       lv1.setAdapter(LocalService.channelsDevicesAdapter);	       lv2.setAdapter(LocalService.channelsmessagesAdapter);	       if (LocalService.channelsDevicesAdapter!=null) {LocalService.channelsDevicesAdapter.notifyDataSetChanged();}	       if (LocalService.channelsmessagesAdapter!=null) {LocalService.channelsmessagesAdapter.notifyDataSetChanged();}	       registerForContextMenu(lv1);	  lv1.setOnItemClickListener(new OnItemClickListener() {		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,				long arg3) {						arg0.showContextMenuForChild(arg1);						}	});	    Button refsimlinkbutton = (Button) findViewById(R.id.refreshchannelsbutton);	    refsimlinkbutton.setOnClickListener(new OnClickListener() {			public void onClick(View v) {				netutil.newapicommand((ResultsListener)LocalService.serContext, "om_device_channel_adaptive:"+settings.getString("device", ""));				netutil.newapicommand((Context)MyChannelsDevices.this, "om_channel_chat_get:"+LocalService.currentChannel.u);			}});	netutil.newapicommand((ResultsListener)LocalService.serContext, "om_device_channel_adaptive:"+settings.getString("device", ""));	netutil.newapicommand((Context)MyChannelsDevices.this, "om_channel_chat_get:"+LocalService.currentChannel.u);	}	 @Override	protected void onDestroy() {		 LocalService.currentchanneldeviceList.clear();		   LocalService.currentChannel=null;		 		 		super.onDestroy();	}	@Override	  public void onCreateContextMenu(ContextMenu menu, View v,	      ContextMenuInfo menuInfo) {	    super.onCreateContextMenu(menu, v, menuInfo);	    menu.add(0, 1, 0, "Показать на картах").setIcon(android.R.drawable.ic_menu_mylocation);;	  //  menu.add(0, 2, 0, "Проверить связь").setIcon(android.R.drawable.ic_menu_delete);;	  //  menu.add(0, 3, 0, "Проверить батарею").setIcon(android.R.drawable.ic_menu_edit);;	  }	  @Override	  public boolean onContextItemSelected(MenuItem item) {		  final AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();		  if (item.getItemId() == 1) 		  {			String latitude =Float.toString(LocalService.channelsDevicesAdapter.getItem(acmi.position).lat);			String longitude=Float.toString(LocalService.channelsDevicesAdapter.getItem(acmi.position).lon);;			Intent intent= new Intent(android.content.Intent.ACTION_VIEW,Uri.parse("geo:"+latitude+","+longitude+"?z=10"));			try {				startActivity(intent);			} catch (Exception e) {				Toast.makeText(getApplicationContext(), "Не установленно ни одного приложения типа Карты", Toast.LENGTH_SHORT).show();				e.printStackTrace();			}  			return super.onContextItemSelected(item);		  }		  return super.onContextItemSelected(item);	  }	public void onResultsSucceeded(APIComResult result) {				Log.d(getClass().getSimpleName(),"OnResultListener Command:"+result.Command+",Jo="+result.Jo);		if (!(result.Jo==null)  ) {			Toast.makeText(this,result.Jo.optString("state")+" "+ result.Jo.optString("error_description"),5).show();					}				//"om_channel_chat_get:"+LocalService.currentChannel.u		if (result.Jo.has("om_channel_chat_get:"+LocalService.currentChannel.u)){						LocalService.currentChannel.messagesstringList.clear();			try {				  JSONArray a = result.Jo.getJSONArray("om_channel_chat_get:"+LocalService.currentChannel.u);		 		  Log.d(getClass().getSimpleName(), a.toString());		 		 for (int i = 0; i < a.length(); i++) {		 			JSONObject jsonObject = a.getJSONObject(i);				 					 			LocalService.currentChannel.messagesstringList.add(0, jsonObject.optString("text"));						 if (LocalService.channelsmessagesAdapter!=null) {LocalService.channelsmessagesAdapter.notifyDataSetChanged();}			}				 		 				} catch (Exception e) {					 Log.d(getClass().getSimpleName(), "om_device_channel_adaptive эксепшн"+e.getMessage());					e.printStackTrace();				}				}								}		}