package com.zstudio.zshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class shareActivity extends AppCompatActivity implements View.OnClickListener {


    private String TAG = "shareActivity";
    private Button facebook;
    private Button instagram;
    private Button snapchat;
    private Button whatsapp;
    private Uri uri;
    private String path;
    private int bkey;
    private String backcolor;
    private final String AppId = "com.zstudio.zshare";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        path = intent.getStringExtra("videoUrl");
        uri = Uri.parse(intent.getStringExtra("uri"));
        bkey = intent.getIntExtra("bkey", 10);
        backcolor = intent.getStringExtra("backcolor");
        Log.d(TAG, "onCreate: backcolor :" + backcolor);

        Log.d(TAG, "onCreate: uri :" + uri.toString());

        //initialization
        facebook = findViewById(R.id.facebook);
        instagram = findViewById(R.id.instagram);
        snapchat = findViewById(R.id.snapchat);
        whatsapp = findViewById(R.id.whatsapp);


        facebook.setOnClickListener(this);
        instagram.setOnClickListener(this);
        snapchat.setOnClickListener(this);
        whatsapp.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.facebook:
                Intent facebook_intent = new Intent("com.facebook.stories.ADD_TO_STORY");
                facebook_intent.setType("image/*");
                facebook_intent.putExtra("com.facebook.platform.extra.APPLICATION_ID",AppId);
                facebook_intent.putExtra("interactive_asset_uri", uri);
                facebook_intent.putExtra("content_url", path);
                facebook_intent.putExtra("top_background_color", backcolor);
                facebook_intent.putExtra("bottom_background_color", backcolor);
                startActivity(Intent.createChooser(facebook_intent, "Share"));
                break;
            case R.id.instagram:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setPackage("com.instagram.android");
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_TEXT,path);
                startActivity(Intent.createChooser(intent, "Share to"));
                break;
            case R.id.snapchat:
                Intent snapchat_intent = new Intent(Intent.ACTION_SEND);
                snapchat_intent.setType("image/*");
                snapchat_intent.putExtra(Intent.EXTRA_STREAM, uri);
                snapchat_intent.setPackage("com.snapchat.android");
                startActivity(Intent.createChooser(snapchat_intent, "share"));
                break;
            case R.id.whatsapp:
                Intent whatsapp_intent = new Intent();
                whatsapp_intent.setAction(Intent.ACTION_SEND);
                whatsapp_intent.putExtra(Intent.EXTRA_TEXT,path);
                whatsapp_intent.setType("text/plain");
                whatsapp_intent.putExtra(Intent.EXTRA_STREAM,uri);
                whatsapp_intent.setType("image/jpeg");
                whatsapp_intent.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(whatsapp_intent, "share"));
                break;
        }

    }
}
