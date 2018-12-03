package com.example.android.bakingtime.ui.utlities;

import android.content.Context;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import com.example.android.bakingtime.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

//source:
// https://medium.com/tall-programmer/fullscreen-functionality-with-android-exoplayer-5fddad45509f
public class ExoPlayerVideoHandler
{

    private SimpleExoPlayer player;
    private Uri playerUri;

    public ExoPlayerVideoHandler(){}

    public void prepareExoPlayerForUri(Context context, Uri uri,
                                       boolean videoPlayOnLoad, long videoCurrentPlayingPosition,
                                       PlayerView exoPlayerView){

        if(context != null && uri != null && exoPlayerView != null){
            if(!uri.equals(playerUri) || player == null){
                if(player != null) {
                    releaseVideoPlayer();
                }

                player = ExoPlayerFactory.newSimpleInstance(context);

                playerUri = uri;

                // Produces DataSource instances through which media data is loaded.
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, context.getString(R.string.app_name)));

                // This is the MediaSource representing the media to be played.
                MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);

                // Prepare the player with the source.
                player.prepare(videoSource);
            }

            player.clearVideoSurface();
            player.setVideoTextureView(
                    (TextureView) exoPlayerView.getVideoSurfaceView());
            player.seekTo(videoCurrentPlayingPosition + 1);
            exoPlayerView.setPlayer(player);
            player.setPlayWhenReady(videoPlayOnLoad);
        }
    }

    public void releaseVideoPlayer(){
        if(player != null)
        {
            player.setPlayWhenReady(false);
            player.clearVideoSurface();
            player.release();
        }
        player = null;
    }

    public boolean isVideoPlaying() {
        if(player != null) {
            return player.getPlayWhenReady();
        }
        else return false;
    }

    public long getCurrentPosition() {
        if(player != null) {
            return player.getCurrentPosition();
        }
        else return 0;
    }
}
