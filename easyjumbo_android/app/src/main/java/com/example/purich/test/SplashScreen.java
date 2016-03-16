package com.example.purich.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Purich on 25/4/2558.
 */
public class SplashScreen extends Activity {
    AnimationDrawable splash;
    Database db = new Database(this);
    public ArrayList<Activity> activities=new ArrayList<Activity>();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        ImageView image = (ImageView) findViewById(R.id.imageView1);
        splash = (AnimationDrawable) image.getBackground();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1370);
                } catch (InterruptedException e) { }
                Intent intent_login = new Intent(getApplicationContext(), Login.class);
                Intent intent_info = new Intent(getApplicationContext(), Infomation.class);
                List<Contact> dbase = db.getAllContacts();
                if (dbase.isEmpty()){
                    startActivity(intent_login);
                    finish();
                }
                else{
                    for (Contact contact : dbase) {
                        intent_info.putExtra("username", contact.get_username());
                        intent_info.putExtra("key", contact.get_key());
                        startActivity(intent_info);
                        finish();
                    }
                }
            }
        }).start();
        image.post(new Starter());
    }

    class Starter implements Runnable {
        public void run() {
            splash.start();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Intent intent_login = new Intent(getApplicationContext(), Login.class);
            Intent intent_info = new Intent(getApplicationContext(), Infomation.class);
            List<Contact> dbase = db.getAllContacts();
            if (dbase.isEmpty()){
                startActivity(intent_login);
                finish();
            }
            else{
                for (Contact contact : dbase) {
                    intent_info.putExtra("username", contact.get_username());
                    intent_info.putExtra("key", contact.get_key());
                        startActivity(intent_info);
                    finish();
                }
                finish();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
