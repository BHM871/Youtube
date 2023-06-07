package com.example.youtube.commun

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.view.SurfaceHolder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

//Gerenciamento do CallBack do SurfaceHolder
class YoutubePlayer(private val context: Context) : SurfaceHolder.Callback {

    private var mediaPlayer: ExoPlayer? = null
    var youtubePlayerListener: YoutubePlayerListener? = null
    private lateinit var runnable: Runnable
    private var handler = Handler()

    override fun surfaceCreated(holder: SurfaceHolder) {
        if(mediaPlayer == null) {
            mediaPlayer = SimpleExoPlayer.Builder(context).build()
            mediaPlayer?.setVideoSurfaceHolder(holder)
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        mediaPlayer?.release()
    }

    //Função que prepara a URL para o video ser baixado
    fun setUrl(url: String) {
        mediaPlayer?.let {
            val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "utube"))

            /*Aqui se cria a Media a partir da URL para ser reproduzido, mas
            poderia ser um video ou audio locais*/
            val videoSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

            //Prepara a media
            it.setMediaSource(videoSource)
            it.prepare()
            it.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        trackTime()
                    }
                }
            })
            play()
        }
    }

    private fun trackTime() {
        mediaPlayer?.let {
            youtubePlayerListener?.onTrackTime(it.currentPosition, it.duration, it.currentPosition * 100 / it.duration)
            if(it.isPlaying) {
                runnable = Runnable {
                    trackTime()
                }
                handler.postDelayed(runnable, 500)
            }
        }
    }

    //Faz a Media rodar
    fun play() {
        mediaPlayer?.playWhenReady = true
    }

    //Faz a Media pausar
    fun pause() {
        mediaPlayer?.playWhenReady = false
    }

    //Mata a Media
    fun release() {
        mediaPlayer?.release()
    }

    fun seek(progress: Long) {
        if (progress > 0) {
            mediaPlayer?.let{
                val seek = progress * it.duration / 100
                it.seekTo(seek)
            }
        }
    }

    interface YoutubePlayerListener {
        fun onPrepared()
        fun onTrackTime(currentPosition: Long, totalTime: Long, percente: Long)
    }

}