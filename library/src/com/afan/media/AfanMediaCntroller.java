package com.afan.media;

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * 播放器控制器
 * 
 * 暂停 进度显示 进度拖动
 * 
 * @author jihongwen
 *
 */
public class AfanMediaCntroller extends RelativeLayout {
    
    public static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    public static final int MAX_VIDEO_PROGRESS = 1000;
    public static final int SKIP_VIDEO_PROGRESS = MAX_VIDEO_PROGRESS / 10;
    
    /** 是否可拖动 */
    private boolean mDragging;
    private boolean mShowing;
    private MediaPlayerControl mPlayer;
    private Context mContext;
    private View mRoot;
    private View anchor;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private ImageView mPauseButton;
    
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    
    public AfanMediaCntroller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public AfanMediaCntroller(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public AfanMediaCntroller(Context context) {
        super(context);
        mContext = context;
        mRoot = this;
    }
    
    @Override
    protected void onFinishInflate() {
        if (mRoot != null) {
            LayoutInflater.from(getContext()).inflate(R.layout.view_media_controller, this);
            initControllerView(mRoot);
        }
    }
    
    public void setAnchorView(View view) {
        anchor = view;
        RelativeLayout.LayoutParams frameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(anchor.getWidth(), RelativeLayout.LayoutParams.MATCH_PARENT);
        ((RelativeLayout) anchor).addView(this, params);
        setVisibility(GONE);
        mShowing = false;
        //show();
    }
    
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.view_media_controller, null);
        initControllerView(mRoot);
        return mRoot;
    }
    
    private void initControllerView(View root) {
        root.findViewById(R.id.controller).setOnTouchListener(new OnTouchListener()
        {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        
        mPauseButton = (ImageView) root.findViewById(R.id.videoPause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mCurrentTime = (TextView) root.findViewById(R.id.currentTime);
        mEndTime = (TextView) root.findViewById(R.id.endTime);
        mProgress = (SeekBar) root.findViewById(R.id.play_seekbar);
        if (mProgress != null) {
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
        }
        
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(DEFAULT_TIMEOUT);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }
        show(DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(DEFAULT_TIMEOUT);
        return false;
    }
    
    private OnClickListener mPauseListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v) {
            doPauseResume();
            show(DEFAULT_TIMEOUT);
        }
    };
    
    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
    }
    
    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;
        
        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.video_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.video_play);
        }
    }
    
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener()
    {
        
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
        }
        
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }
        
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_TIMEOUT);
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };
    
    public int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }
        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));
        return position;
    }
    
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
    
    /***************************** 控制方法 ***************************************/
    
    public void hide() {
        if (anchor == null)
            return;
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                setVisibility(View.INVISIBLE);
            } catch (Exception e) {
            }
            mShowing = false;
        }
    }
    
    public void show() {
        show(DEFAULT_TIMEOUT);
    }
    
    public void show(int timeout) {
        if (!mShowing && anchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            setVisibility(View.VISIBLE);
            mShowing = true;
        }
        
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
        
    }
    
    public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
        mPlayer = playerControl;
    }
    
    public boolean isShowing() {
        return mShowing;
    }
    
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                
                default:
                    break;
            }
        }
    };
}
