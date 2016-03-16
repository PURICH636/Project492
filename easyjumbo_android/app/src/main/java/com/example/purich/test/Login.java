package com.example.purich.test;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.StrictMode;
import android.widget.EditText;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.KITKAT;


public class Login extends ActionBarActivity {
    Database db = new Database(this);
    String SSID_Jumbo = "@JumboPlus";
    boolean sign = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = (Button) findViewById(R.id.button);
        Button exit = (Button) findViewById(R.id.button2);
        TextView TextFlow = (TextView)findViewById(R.id.textFlow);
        TextFlow.setSelected(true);
        // Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /*..........................Login Button.....................*/
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText user = (EditText) findViewById(R.id.txtUser);
                EditText pass = (EditText) findViewById(R.id.txtPass);
                Intent intent = new Intent(Login.this, Infomation.class);
                String username = user.getText().toString();
                String password = pass.getText().toString();
                        Log.d("pj492", username + " " + password);
                if ((!username.isEmpty())&&(!password.isEmpty())){
                    wifiManage(SSID_Jumbo, username, password);
                    auThen(username, password);
                    List<Contact> dbase = db.getAllContacts();
                    for (Contact contact : dbase) {
                        Log.d("pj492", "username, password " + contact.get_username()+ " " + contact.get_key());
                        if ((sign==true) && (username.equals(contact.get_username()))) {
                            intent.putExtra("username", contact.get_username());
                            intent.putExtra("key", contact.get_key());
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(Login.this, "You are registered!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else{
                    Toast.makeText(Login.this, "Please try again!", Toast.LENGTH_LONG).show();
                }
            }

        });

        /*..........................Exit Button.....................*/
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                dialog.setTitle("Exit");
                dialog.setIcon(R.drawable.ic_launcher);
                dialog.setCancelable(true);
                dialog.setMessage("Do you want to exit?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
    }

    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Exit");
        dialog.setIcon(R.drawable.ic_launcher);
        dialog.setCancelable(true);
        dialog.setMessage("Do you want to exit?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    //Authentication
    public void auThen(final String username, final String password){
        Log.d("pj492", "Authentication....");
                try {
                    URL url = new URL(".../index.php/userAuth");
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse httpResponse = null;
                    HttpPost httpPost = new HttpPost(url.toString());
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("username", username));
                    nameValuePairs.add(new BasicNameValuePair("password", password));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    httpResponse = httpClient.execute(httpPost);
                    String results = EntityUtils.toString(httpResponse.getEntity());
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        Log.d("pj492", "Http Code 200...");
                        JSONObject resultObj = new JSONObject(results);
                        if (resultObj.getString("error") == "false") {
                            Log.d("pj492", "Msg error false...");
                            ContentValues values = new ContentValues();
                            values.put("username", username);
                            values.put("key", resultObj.getString("key"));
                            List<Contact> dbase = db.getAllContacts();
                            if(dbase.size()==0) {
                                Log.d("dbase size 0...", "");
                                db.addContact(new Contact(username, resultObj.getString("key")));
                                Toast.makeText(Login.this, "Login Success...", Toast.LENGTH_LONG).show();
                                sign = true;
                            }
                            else if(dbase.size()==1) {
                                db.updateContact(new Contact(username, resultObj.getString("key")));
                                Toast.makeText(Login.this, "Login Success...", Toast.LENGTH_LONG).show();
                                sign = true;
                            }
                        }
                        else if(resultObj.getString("error") == "true"){
                            Log.d("pj492", "Msg error true...");
                            Toast.makeText(Login.this, "Registered or Username, password invalid!", Toast.LENGTH_LONG).show();
                            sign = false;
                        }
                        else {
                            Log.d("pj492", "Don't get massage");
                            sign = false;
                        }
                    }
                    else if(httpResponse.getStatusLine().getStatusCode() != 200){
                        Toast.makeText(Login.this, "Connection error, Please try again...", Toast.LENGTH_LONG).show();
                        sign = false;
                    }
                    else {
                        Log.d("pj492", "Don't get massage");
                        sign = false;
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void wifiManage(String ssid, String userName, String passWord){
        boolean ss = false;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (int i=0; i<list.size(); i++) {
            if(list.get(i).SSID.equalsIgnoreCase("\"@JumboPlus\"")){
                ss = true;
                Log.d("pj492", "Configurated");
            }
            Log.d("pj492", String.format("* GetConfigInfo: %d %s", i,list.get(i).SSID));
        }
        if (ss == false) {
            Log.d("pj492", "Configurating");
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            wifiConfiguration.SSID = "\"" + ssid + "\"";   //String ssid
            wifiConfiguration.allowedKeyManagement.set(3);  //IEEE8021X
            wifiConfiguration.allowedKeyManagement.set(2);  //WPA_EAP

            enterpriseConfig.setIdentity(userName);     //String username
            enterpriseConfig.setPhase2Method(3);        //MSCHAPV2
            enterpriseConfig.setEapMethod(0);           //PEAP
            enterpriseConfig.setPassword(passWord);     //String pass

            wifiConfiguration.enterpriseConfig = enterpriseConfig;
            wifiManager.addNetwork(wifiConfiguration);

            int netID = wifiConfiguration.networkId;    //24
            wifiManager.saveConfiguration();
            wifiManager.enableNetwork(netID, true);
            wifiManager.reconnect();
        }
    }
}