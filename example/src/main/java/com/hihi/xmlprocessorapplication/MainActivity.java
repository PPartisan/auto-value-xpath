package com.hihi.xmlprocessorapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import static com.hihi.xmlprocessorapplication.R.id.text;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "XmlPathExtension";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Pet pojo = Pet.fromXml(Pet.XML);
        final String s = pojo.toString();

        this.<TextView>findViewById(text).setText(s);
        Log.d(TAG, s);
    }

}
