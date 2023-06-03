package com.example.youtube

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube.commun.VideoAdapter
import com.example.youtube.commun.YoutubePlayer
import com.example.youtube.databinding.ActivityMainBinding
import com.example.youtube.databinding.VideoDetailBinding
import com.example.youtube.model.ListVideo
import com.example.youtube.model.Video
import com.example.youtube.model.formatTime
import com.example.youtube.model.videos
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mergeB: VideoDetailBinding

    private lateinit var videosAdapter: VideoAdapter

    private lateinit var youtuberPlayer: YoutubePlayer

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        mergeB = VideoDetailBinding.bind(binding.root)

        setContentView(binding.root)

        youtuberPlayer = YoutubePlayer(this)

        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.title = ""

        val videos = mutableListOf<Video>()
        videosAdapter = VideoAdapter(videos, false) {
            showOverlayView(it)
        }

        mergeB.viewLayer.alpha = 0.0f

        binding.mainRv.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.mainRv.adapter = videosAdapter

        CoroutineScope(Dispatchers.IO).launch {
            val res = async { getVideos() }
            val listVideos = res.await()
            withContext(Dispatchers.Main) {
                listVideos?.let {
                    videos.clear()
                    videos.addAll(it.data)
                    videosAdapter.notifyDataSetChanged()
                    binding.mainProgress.alpha = 0f
                }
            }
        }

        preparePlayer()
    }

    override fun onPause() {
        youtuberPlayer.pause()
        super.onPause()
    }

    override fun onDestroy() {
        youtuberPlayer.release()
        super.onDestroy()
    }

    /*Função que prepara o tocador de video, o CallBack notica as principais ações,
    a classe YoutubePlayer é filha de SurfaceHolder.Callback e prepara o video, como o RecyclerView.Holder.*/

    private fun preparePlayer() {
        mergeB.surfacePlayer.holder.addCallback(youtuberPlayer)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showOverlayView(video: Video) {
        val layer = mergeB.viewLayer

        layer.animate().apply {
            duration = 400
            alpha(0.5f)
        }

        binding.container.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                if (progress > 0.5f) {
                    layer.alpha = 1.0f - progress
                } else {
                    layer.alpha = 0.5f
                }
            }

            override fun onTransitionCompleted(
                motionLayout: MotionLayout?,
                currentId: Int
            ) {
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                targetId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }
        })

        mergeB.videoPlayer.visibility = View.GONE
        youtuberPlayer.setUrl(video.videoUrl)
        youtuberPlayer.youtubePlayerListener = object : YoutubePlayer.YoutubePlayerListener {
            override fun onPrepared() {
            }

            override fun onTrackTime(currrentPosition: Long, totalTime: Long) {
                mergeB.seekBar.progress = currrentPosition.toInt()
                mergeB.currentPlayer.text = currrentPosition.formatTime(totalTime)
                mergeB.durationPlayer.text = totalTime.formatTime()
            }
        }

        //mergeB.durationPlayer.text = video.duration.formatTime()

        val similarAdapter = VideoAdapter(videos(), true){}
        mergeB.inc.videoContentRvSimilar.layoutManager = LinearLayoutManager(this@MainActivity)
        mergeB.inc.videoContentRvSimilar.adapter = similarAdapter

        mergeB.miniVideoTitle.text = video.title
        mergeB.miniVideoSubtitle.text = video.publisher.name

        mergeB.inc.videoContentTxtTitle.text = video.title
        mergeB.inc.videoContentTxtViews.text = video.viewCountLabel
        mergeB.inc.videoContentTxtChannel.text = video.publisher.name
        Picasso.get().load(video.publisher.pictureProfileUrl).into(mergeB.inc.videoContentImgChannel)

        similarAdapter.notifyDataSetChanged()
    }

    private fun getVideos(): ListVideo? {
        val client = OkHttpClient.Builder()
            .build()

        val request = Request.Builder()
            .get()
            .url("https://tiagoaguiar.co/api/youtube-videos")
            .build()

        return try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                GsonBuilder().create()
                    .fromJson(response.body()?.string(), ListVideo::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}