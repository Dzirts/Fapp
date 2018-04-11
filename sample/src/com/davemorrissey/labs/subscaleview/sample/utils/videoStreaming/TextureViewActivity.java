package com.davemorrissey.labs.subscaleview.sample.utils.videoStreaming;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.sample.R;
import com.scanlibrary.ScanConstants;
import com.scanlibrary.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Elbit on 3/18/2018.
 */
public class TextureViewActivity extends Activity
        implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener {
    private MediaPlayer mp;
    private TextureView tv;
//    rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov
    public static String MY_VIDEO = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";//https://www.rmp-streaming.com/media/bbb-360p.mp4";
    public static String IP_FILE_PATH = "cameraIpPath.txt";
    public static String TAG = "TextureViewActivity";
    private Bitmap bm = null;
    private HashMap<String,String> cameraIpHash;
    private Surface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_view);
        cameraIpHash= new HashMap<String,String>();
        readCameraIPsFromFile(IP_FILE_PATH);
        addDistanceButtons();
//        createVideoViews();

        tv = (TextureView) findViewById(R.id.textureView);
        tv.setSurfaceTextureListener(this);

//        048412646
    }

    private void addDistanceButtons() {
        LinearLayout buttonsLayout = (LinearLayout)findViewById(R.id.buttonsLayout);
        int i=1;
        Set<String> keys = cameraIpHash.keySet();
        ArrayList<String> cameraRanges = new ArrayList<String>(keys);
        Collections.sort(cameraRanges);
        for (final String ip: cameraRanges) {
            Button bt = new Button(this);
            bt.setText(ip);
            bt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,1.0f));
            bt.setGravity(Gravity.CENTER);

            bt.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mp.isPlaying()){
                        mp.stop();
                        mp.reset();
                        mp = null;
                    }
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                buildNewMediaPlayer(cameraIpHash.get(ip));
                            }
                        }).start();
//                        buildNewMediaPlayer(cameraIpHash.get(ip));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            buttonsLayout.addView(bt,i);
            i++;

        }
    }





    private void readCameraIPsFromFile(String fileName) {
        File cameraIpFile = new File(Environment.getExternalStorageDirectory()+ "/Elbit Mark Target/Infrastructure/"+IP_FILE_PATH);
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(cameraIpFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] lineElements = line.split(";");
                        cameraIpHash.put(lineElements[1],lineElements[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getBitmap(TextureView vv) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        String mPath = Environment.getExternalStorageDirectory().toString()
                + "/Pictures/" + sdf.toLocalizedPattern()+ ".png";
        Toast.makeText(getApplicationContext(), "Capturing Screenshot: " + mPath, Toast.LENGTH_SHORT).show();

        bm = vv.getBitmap();
        if (bm == null)
            Log.e(TAG, "bitmap is null");

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
        }
    }

    private void closeActivity(){
        Intent data = new Intent();
        if (bm == null) {
            Toast.makeText(TextureViewActivity.this,"First capture an image",Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Utils.getUri(TextureViewActivity.this, bm);
        data.putExtra(VideoStreamConstants.CAPTURED_IMAGE_RESULT, uri);
        TextureViewActivity.this.setResult(Activity.RESULT_OK, data);
        System.gc();
        TextureViewActivity.this.finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(menu.media_player_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = new Surface(surface);
        new Thread(new Runnable() {
            public void run() {
                buildNewMediaPlayer(MY_VIDEO);
            }
        }).start();


        ImageButton captureBtn = (ImageButton) findViewById(R.id.btnCapture);
        captureBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextureViewActivity.this.getBitmap(tv);
            }
        });

        ImageButton nextBtn = (ImageButton) findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mp.stop();
                TextureViewActivity.this.closeActivity();
            }
        });

    }

    private void buildNewMediaPlayer(String path){
        try{
            mp = new MediaPlayer();
            mp.setDataSource(path);
            mp.setSurface(this.surface);
            mp.prepare();

            mp.setOnBufferingUpdateListener(this);
            mp.setOnCompletionListener(this);
            mp.setOnPreparedListener(this);
            mp.setOnVideoSizeChangedListener(this);

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.start();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {

    }
}

