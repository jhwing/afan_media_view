package com.afan.media.demo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.afan.media.AfanMediaCntroller;
import com.afan.media.AfanVideoView;

public class MainActivity extends Activity {
    
    final String path = "http://img.spriteapp.cn/spritead/20150709/141727821723.mp4";
    
    RelativeLayout videoParent;
    
    MediaController controller;
    
    AfanMediaCntroller mediaCntroller;
    
    AfanVideoView videoView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        videoParent = (RelativeLayout) findViewById(R.id.videoParent);
        
        videoView = new AfanVideoView(this);
        mediaCntroller = new AfanMediaCntroller(this);
        controller = new MediaController(this, false);
        videoView.setVideoPath(path);
        videoView.setMediaController(mediaCntroller);
        videoView.setOnPreparedListener(new OnPreparedListener()
        {
            
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoParent.addView(videoView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
