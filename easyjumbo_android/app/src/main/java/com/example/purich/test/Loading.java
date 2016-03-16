package com.example.purich.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.purich.test.R;

/**
 * Created by Purich on 24/4/2558.
 */
public class Loading extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent input_intent = getIntent();
        Bundle data = input_intent.getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        final String username =(String) data.get("username");
        final String key =(String) data.get("key");
        Intent intent3 = new Intent(Loading.this, Infomation.class);
        intent3.putExtra("username", username);
        intent3.putExtra("key", key);
        startActivity(intent3);
    }
}
