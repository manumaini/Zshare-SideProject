package com.zstudio.zshare;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

   

    private String videoId;
    private String TAG = "MainActivity";
    private String api_key = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private Boolean given = false;
    private TextView likes;
    private TextView dislikes;
    private TextView comments;
    private TextView views;
    private TextView loading_text;
    private TextView channel_name;
    private TextView title;
    private RelativeLayout sticker;
    private RelativeLayout loading;
    private RelativeLayout background;
    private ImageView thumbnail;
    private ImageView image_likes;
    private ImageView image_dislikes;
    private ImageView image_comments;
    private ImageView image_views;
    private Button button_story;
    private Button button_background;
    private Button button_likes;
    private Button button_dislikes;
    private Button button_comments;
    private Button button_views;
    private int bkey = 0;
    private Uri uri;
    private String videoUrl;
    private String backcolor;
    private Target target;
    private int REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //intialization
        likes = findViewById(R.id.text_like);
        dislikes = findViewById(R.id.text_dislike);
        comments = findViewById(R.id.text_comment);
        views = findViewById(R.id.text_views);
        sticker = findViewById(R.id.sticker);
        loading = findViewById(R.id.loading_screen);
        background = findViewById(R.id.background);
        thumbnail = findViewById(R.id.thumbnail);
        image_likes = findViewById(R.id.image_like);
        image_dislikes = findViewById(R.id.image_dislike);
        image_comments = findViewById(R.id.image_comment);
        image_views = findViewById(R.id.image_views);
        button_story = findViewById(R.id.button_story);
        button_background = findViewById(R.id.button_background);
        button_likes = findViewById(R.id.button_like);
        button_dislikes = findViewById(R.id.button_dislike);
        button_comments = findViewById(R.id.button_comments);
        button_views = findViewById(R.id.button_views);
        loading_text = findViewById(R.id.loading_text);
        channel_name = findViewById(R.id.channel_name);
        title = findViewById(R.id.title);

        loading_text.setText("Loading(statistics)...");


        Intent intent = getIntent();
        videoUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        videoId = getYouTubeId(videoUrl);
        Log.d(TAG, "onCreate: " + videoId);

        load_data();

        button_story.setOnClickListener(this);
        button_background.setOnClickListener(this);
        button_likes.setOnClickListener(this);
        button_dislikes.setOnClickListener(this);
        button_comments.setOnClickListener(this);
        button_views.setOnClickListener(this);


    }


    //method convert video url into video id
    private String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            onBackPressed();
            Toast.makeText(MainActivity.this, "incorrect Url Format", Toast.LENGTH_LONG).show();
            return "error";
        }
    }

    //load data from google api for likes dislikes comments and views
    private void load_data() {

        String url = "https://www.googleapis.com/youtube/v3/videos?part=statistics&id=" + videoId + "&key=XXXXXXXXXXXXXXXXXXXXXXXXXX";
        final String thumbnail_url = "http://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";

        //request_queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        //request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray items = response.getJSONArray("items");
                    JSONObject single = items.getJSONObject(0);
                    JSONObject statistics = single.getJSONObject("statistics");
                    Log.d(TAG, "onResponse:  :" + statistics.toString());
                    long[] numbers = new long[]{statistics.getLong("likeCount"), statistics.getLong("dislikeCount"), statistics.getLong("commentCount"), statistics.getLong("viewCount")};
                    String[] values = new String[numbers.length];
                    for (int i = 0; i < numbers.length; i++) {
                        long n = numbers[i];
                        values[i] = format(n);
                        Log.d(TAG, "onResponse: " + values[i]);
                    }
                    likes.setText(values[0]);
                    dislikes.setText(values[1]);
                    comments.setText(values[2]);
                    views.setText(values[3]);
                    loading_text.setText("loading(thumbnail)...");

                    target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            thumbnail.setImageBitmap(bitmap);
                            loading_text.setText("loading(title)...");
                            getVideoTitle();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Log.d(TAG, "onBitmapFailed: wrong video id ");

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };

                    Picasso.with(getApplicationContext()).load(thumbnail_url).into(target);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onResponse: error" + e.getMessage());
                    Toast.makeText(MainActivity.this, "VideoId is incorrect", Toast.LENGTH_LONG).show();
                    loading.setVisibility(View.GONE);
                    finish();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(MainActivity.this, "Internet Connection Error", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                    onBackPressed();
                } else if (error instanceof ServerError) {
                    Toast.makeText(MainActivity.this, "Internet Connection Error", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                    onBackPressed();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(MainActivity.this, "Internet Connection Error", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                    onBackPressed();
                } else {
                    Toast.makeText(MainActivity.this, "Unknown Error", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                    onBackPressed();
                }


            }
        });

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        jsonObjectRequest.setTag(TAG);

        requestQueue.add(jsonObjectRequest);


    }


    private void getVideoTitle() {

        String url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=" + videoId + "&key=AIzaSyBpXWuam_VGuO7MyPlUV4rKEWTb5fah5dY ";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray items = response.getJSONArray("items");
                    JSONObject single = items.getJSONObject(0);
                    JSONObject snippet = single.getJSONObject("snippet");
                    channel_name.setText("@" + snippet.getString("channelTitle"));
                    title.setText(snippet.getString("title"));
                    Log.d(TAG, "onResponse: title: " + snippet.getString("channelTitle") + "  " + snippet.getString("title"));
                    loading.setVisibility(View.GONE);
                    sticker.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "title cant be loaded", Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
                sticker.setVisibility(View.VISIBLE);
            }
        });

        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        requestQueue.add(request);


    }

    //for conversion of number into k or m
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.button_story:
                ColorDrawable viewColor = (ColorDrawable) background.getBackground();
                int colorId = viewColor.getColor();
                backcolor = String.format("#%06X", (0xFFFFFF & colorId));
                askForPermission(REQ_CODE);
                break;
            case R.id.button_background:
                bkey = (bkey + 1) % 11;
                switch (bkey) {
                    case 0:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                        break;
                    case 1:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color1));
                        break;
                    case 2:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color2));
                        break;
                    case 3:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color3));
                        break;
                    case 4:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color4));
                        break;
                    case 5:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color5));
                        break;
                    case 6:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color6));
                        break;
                    case 7:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color7));
                        break;
                    case 8:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color8));
                        break;
                    case 9:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color9));
                        break;
                    case 10:
                        background.setBackgroundColor(ContextCompat.getColor(this, R.color.color10));
                }
                break;

            case R.id.button_like:
                if (image_likes.getVisibility() == View.VISIBLE) {
                    image_likes.setVisibility(View.GONE);
                    likes.setVisibility(View.GONE);
                } else {
                    image_likes.setVisibility(View.VISIBLE);
                    likes.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.button_dislike:
                if (image_dislikes.getVisibility() == View.VISIBLE) {
                    image_dislikes.setVisibility(View.GONE);
                    dislikes.setVisibility(View.GONE);
                } else {
                    image_dislikes.setVisibility(View.VISIBLE);
                    dislikes.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.button_comments:
                if (image_comments.getVisibility() == View.VISIBLE) {
                    image_comments.setVisibility(View.GONE);
                    comments.setVisibility(View.GONE);
                } else {
                    image_comments.setVisibility(View.VISIBLE);
                    comments.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.button_views:
                if (image_views.getVisibility() == View.VISIBLE) {
                    image_views.setVisibility(View.GONE);
                    views.setVisibility(View.GONE);
                } else {
                    image_views.setVisibility(View.VISIBLE);
                    views.setVisibility(View.VISIBLE);
                }
                break;

        }

    }


    private void loadBitmapFromView(View v) {
        if (v.getMeasuredHeight() <= 0) {
            v.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.draw(c);

            getLocalBitmapUri(b);

        } else {
            Bitmap b = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            v.draw(c);

            getLocalBitmapUri(b);
        }
    }

    public void getLocalBitmapUri(Bitmap bmp) {
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Zshare.jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            uri = FileProvider.getUriForFile(MainActivity.this,getApplicationContext().getPackageName()+".provider",file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "savetolocal: uri :" + uri.toString());

        Intent intent = new Intent(MainActivity.this, shareActivity.class);
        intent.putExtra("uri", uri.toString());
        intent.putExtra("bkey", bkey);
        intent.putExtra("videoUrl", videoUrl);
        intent.putExtra("backcolor", backcolor);
        loading.setVisibility(View.GONE);
        startActivity(intent);

    }

    //permission code
    private void askForPermission(int requestCode) {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(WRITE_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            String[] permissions_array = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, permissions_array, REQ_CODE);
        } else {
            loading_text.setText("saving changes...");
            loading.setVisibility(View.VISIBLE);
            loadBitmapFromView(sticker);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == REQ_CODE) {
            int denied_count = 0;
            HashMap<String, Integer> permissionResults = new HashMap<>();

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    denied_count++;
                }
            }

            if (denied_count == 0) {
                loading_text.setText("saving changes...");
                loading.setVisibility(View.VISIBLE);
                loadBitmapFromView(sticker);
            } else {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        showDialog("", "This app needs these permissions to work without any problems.",
                                "Yes, Grant permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        askForPermission(REQ_CODE);
                                    }
                                },
                                "No, Exit app",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                    } else {
                        showDialog("",
                                "You have denied some permissions. Allow all the permissions at [Setting] > [Permissions]",
                                "Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, "No, Exit App",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                        break;
                    }
                }


            }


        }

    }


    private AlertDialog showDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnclick,
                                   String negativeLabel, DialogInterface.OnClickListener negativeOnclick,
                                   boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnclick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);
        builder.setCancelable(isCancelable);
        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

}
