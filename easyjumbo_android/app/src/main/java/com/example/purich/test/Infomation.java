package com.example.purich.test;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Purich on 4/2/2558.
 */
public class Infomation extends ActionBarActivity {

    private ListView lv;
    ProgressDialog progressDialog;
    List<RowItem> rowItems;
    CustomListViewAdapter adapter;
    Database db = new Database(this);

    WifiScanReceiver wifiReciever;
    WifiManager mainWifiObj;
    List<ScanResult> wifiScanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Get Intent
        Intent input_intent = getIntent();
        Bundle data = input_intent.getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        // Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //Set Tab
        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.animate();////////////////////////////////////
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("TabInfo");


        final String username =(String) data.get("username");
        final String key =(String) data.get("key");
        final TextView showUser = (TextView) findViewById(R.id.showUser);
        final Button buttonLogout = (Button) findViewById(R.id.ButtonLogoutInfo);
        final Button buttonReport = (Button) findViewById(R.id.ButtonSubmit);
        final EditText editReports = (EditText) findViewById(R.id.editTextReport);

        lv = (ListView)findViewById(R.id.listView);

        //Tab view
        if (data != null) {
            tabSpec.setContent(R.id.TabInfo);
            tabSpec.setIndicator("อุปกรณ์ของผู้ใช้");
            tabHost.addTab(tabSpec);
            tabSpec = tabHost.newTabSpec("TabWifi");
            tabSpec.setContent(R.id.TabWifi);
            tabSpec.setIndicator("รายงานปัญหา");
            tabHost.addTab(tabSpec);
            showUser.setText(username); //show username
        }

        listDevice(username, key);
        buttonReport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                progressDialog = progressDialog.show(Infomation.this, "", "Loading...");
                mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                wifiReciever = new WifiScanReceiver();
                mainWifiObj.startScan();
                registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                Thread thread = new Thread(new Runnable(){
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    public void run(){
                        try {
                            TimeUnit.SECONDS.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                        String ss = wifiReciever.Data_json;
                        Log.d("pj492", "Sending... wifi data...");
                        String editReport = editReports.getText().toString();
                        wifiData(username, key, ss, editReport);
                    }
                });
                thread.start();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Contact> dbase = db.getAllContacts();
                for(Contact contact:dbase) {
                    db.deleteContact(new Contact(contact.get_id(), contact.get_username(), contact.get_key()));
                    Log.d("pj492", "Contact: "+contact.get_username());
                }
                finish();
            }
        });

    }

    private void listDevice(final String username, final String key) {
        HttpResponse httpResponse = null;
        final ArrayList<String> listDes = new ArrayList<String>();
        final ArrayList<String> listMac = new ArrayList<String>();
        final ArrayList<Integer> listId = new ArrayList<Integer>();
        final Intent intent2 = new Intent(Infomation.this, Loading.class);
        try {
            URL url = new URL(".../index.php/listDevice");
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url.toString());
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("key", key));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            httpResponse = httpClient.execute(httpPost);
            String results = EntityUtils.toString(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                JSONObject resultObj = new JSONObject(results);
                String sdata = resultObj.getString("data");
                JSONArray jsonArray = new JSONArray(sdata);
                for (int i=0; i<jsonArray.length(); i++){
                    String adata = jsonArray.get(i).toString();
                    JSONObject jsonObject = new JSONObject(adata);
                    final String des = jsonObject.getString("description");
                    final String mac = jsonObject.getString("macaddress");
                    listId.add(i);
                    listDes.add(des);
                    listMac.add(mac);
                }
                rowItems = new ArrayList<RowItem>();
                for (int i = 0; i < listDes.size(); i++) {
                    RowItem item = new RowItem(listId.get(i), listDes.get(i), listMac.get(i));
                    rowItems.add(item);
                }
                if (resultObj.getString("error") == "false") {
                    adapter = new CustomListViewAdapter(this, R.layout.listview_display, rowItems);
                    lv.setAdapter(adapter);
                    registerForContextMenu(lv);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            final AlertDialog.Builder adb = new AlertDialog.Builder(Infomation.this);
                            adb.setTitle("Delete?");
                            adb.setMessage("Are you sure delete \"" + listDes.get(position) +"\"");
                            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.remove(adapter.getItem(position));
                                    deleteDevice(username, key,listMac.get(position), listDes.get(position));
                                    intent2.putExtra("username", username);
                                    intent2.putExtra("key", key);
                                    startActivity(intent2);
                                }
                            });
                            adb.setNegativeButton("Cancel", null);
                            adb.show();
                        }
                    });
                }
            }
        }catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // We know that each row in the adapter is a Map
        RowItem map =  (RowItem) adapter.getItem(aInfo.position);
        menu.setHeaderTitle("Options for "+map.getDes());
        menu.add(1, 1, 1, "Edit");
        menu.add(1, 2, 2, "Delete");
    }
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        Intent input_intent = getIntent();
        final Bundle data = input_intent.getExtras();
        final String username =(String) data.get("username");
        final String key =(String) data.get("key");
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int id = info.targetView.getId();
        final String macaddress = rowItems.get(info.position).getMac();
        final String description = rowItems.get(info.position).getDes();
        final Intent intent2 = new Intent(Infomation.this, Loading.class);
        Log.d("pj492", "ID:"+ macaddress);
        if(item.getTitle()=="Edit"){
            rowItems = new ArrayList<RowItem>();
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View promptsView = inflater.inflate(R.layout.activity_icon_edit, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Infomation.this);
            alertDialogBuilder.setView(promptsView);
            final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    final Editable new_desc = userInput.getText();
                                    Log.d("Values: ", macaddress);
                                    adapter.getItem(info.position).setDes(new_desc.toString());
                                    adapter.notifyDataSetChanged();
                                    editDevice(username, key, macaddress, new_desc);
                                    intent2.putExtra("username", username);
                                    intent2.putExtra("key", key);
                                    startActivity(intent2);
                                    Toast.makeText(Infomation.this, "Edit " + new_desc + " Success...", Toast.LENGTH_LONG).show();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else if(item.getTitle()=="Delete"){
            rowItems = new ArrayList<RowItem>();
            final AlertDialog.Builder adb = new AlertDialog.Builder(Infomation.this);
            adb.setTitle("Delete?");
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    adapter.remove(adapter.getItem(info.position));
                    deleteDevice(username, key, macaddress, description);
                    intent2.putExtra("username", username);
                    intent2.putExtra("key", key);
                    startActivity(intent2);
                    Toast.makeText(Infomation.this, "Delete " + description + " Success...", Toast.LENGTH_LONG).show();
                }
            });
            adb.setNegativeButton("Cancel", null);
            adb.show();
        }
        else {
            return false;
        }
        return true;
    }


    private void deleteDevice(String username, String key, String macaddress, String description) {
        Log.d("pj492", "test"+username+" "+" "+key+" "+description);
        try {
            URL url = new URL(".../index.php/deleteDevice");
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = null;
            HttpPost httpPost = new HttpPost(url.toString());
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("key", key));
            nameValuePairs.add(new BasicNameValuePair("macaddress", macaddress));
            nameValuePairs.add(new BasicNameValuePair("description", description));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            httpResponse = httpClient.execute(httpPost);
            String results = EntityUtils.toString(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                JSONObject resultObj = new JSONObject(results);
                if (resultObj.getString("error") == "false") {
//                    Toast.makeText(Infomation.this, "Successfuly...", Toast.LENGTH_LONG).show();
                    Toast.makeText(Infomation.this, "Your Delete " + description, Toast.LENGTH_LONG).show();
                }
                else{Toast.makeText(Infomation.this, "Failed, Please try again...", Toast.LENGTH_LONG).show();}
            }
            else if (httpResponse.getStatusLine().getStatusCode() != 200){
                Toast.makeText(Infomation.this, "Connected error, Please try again...", Toast.LENGTH_LONG).show();
            }
        }catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void editDevice(String username, String key, String macaddress, Editable ss) {
        Log.d("pj492", "test"+username+" "+" "+key+" "+macaddress+" "+ss);
        try {
            URL url = new URL(".../index.php/editDevice");
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = null;
            HttpPost httpPost = new HttpPost(url.toString());
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("key", key));
            nameValuePairs.add(new BasicNameValuePair("macaddress", macaddress));
            nameValuePairs.add(new BasicNameValuePair("description", ss.toString()));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            httpResponse = httpClient.execute(httpPost);
            String results = EntityUtils.toString(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                JSONObject resultObj = new JSONObject(results);
                if (resultObj.getString("error") == "false") {
//                    Toast.makeText(Infomation.this, "Successfuly...", Toast.LENGTH_LONG).show();
                    Toast.makeText(Infomation.this, "Your Delete " + macaddress, Toast.LENGTH_LONG).show();
                }
                else{Toast.makeText(Infomation.this, "Failed, Please try again...", Toast.LENGTH_LONG).show();}
            }
            else if (httpResponse.getStatusLine().getStatusCode() != 200){
                Toast.makeText(Infomation.this, "Connected error, Please try again...", Toast.LENGTH_LONG).show();
            }
        }catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void wifiData(String username, String key, String data, String report) {
        HttpResponse httpResponse = null;
        Log.d("pj492","report"+ report);
        try {
            URL url = new URL(".../index.php/reportDevice");
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url.toString());
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("key", key));
            nameValuePairs.add(new BasicNameValuePair("wifidata", data));
            nameValuePairs.add(new BasicNameValuePair("textreport", report));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            httpResponse = httpClient.execute(httpPost);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class WifiScanReceiver extends BroadcastReceiver {
        JSONArray jsonArray = new JSONArray();
        JSONObject mainObj = new JSONObject();
        JSONObject mainObj2 = new JSONObject();
        ArrayList<String> ssidList = new ArrayList<>();
        ArrayList<Integer> rssiList = new ArrayList<>();
        public String Data_json;
        public void onReceive(Context c, Intent intent) {
            for (int j=0; j<20; j++) {
                wifiScanList = mainWifiObj.getScanResults();
                for (ScanResult config : wifiScanList) {
                    //only add SSID to jsonArray if it has not been added already
                    if (!ssidList.contains(config.SSID)) {
                        ssidList.add(config.SSID);
                        rssiList.add(config.level);
                    }
                }
                mainObj = convertToJSON(ssidList, rssiList);
                wifiScanList.clear();
                ssidList.clear();
                rssiList.clear();
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainWifiObj.startScan();
                jsonArray.put(mainObj);
            }
            try {
                mainObj2.put("data", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("pj492_mainObj", mainObj2.toString());
            unregisterReceiver(wifiReciever);
            Data_json = mainObj2.toString();
        }

        private JSONObject convertToJSON(ArrayList<String> ssidList, ArrayList<Integer> rssiList) {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            JSONObject mainObj = new JSONObject();
                for (int i = 0; i < ssidList.size(); i++) {
                    try {
                        jsonObject.put("ssid", ssidList.get(i));
                        jsonObject.put("rssi", rssiList.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(jsonObject);
                    jsonObject = new JSONObject();
                }
            try {
                mainObj.put("count", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mainObj;
        }
    }
}
