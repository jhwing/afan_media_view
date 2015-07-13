package com.afan.media.listener;

/**
 * 播放统计
 * @author jihongwen
 *
 */
public class PlayStateAgent {
    
    public enum PlayEvent {
        START_Play, END_PLAY
    }
    
    public static void onEvent(PlayEvent event) {
        switch (event) {
            case START_Play:
                
                break;
            case END_PLAY:
                
                break;
            
            default:
                break;
        }
    }
}
