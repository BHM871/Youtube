package com.example.youtube

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youtube.commun.VideoAdapter
import com.example.youtube.databinding.ActivityMainBinding
import com.example.youtube.model.ListVideo
import com.example.youtube.model.Video
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var videosAdapter: VideoAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.title = ""

        val videos = mutableListOf<Video>()
        videosAdapter = VideoAdapter(videos) {
            showOverlayView(Video)
        }

        binding.include.viewLayer.alpha = 0.0f

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
                    binding.mainProgress.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun showOverlayView(video: Video) {
        val layer = binding.include.viewLayer

        layer.animate().apply {
            duration = 400
            alpha(0.5f)
        }

        binding.container.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?,
                                             startId: Int,
                                             endId: Int) {
            }

            override fun onTransitionChange(motionLayout: MotionLayout?,
                                            startId: Int,
                                            endId: Int,
                                            progress: Float) {
                if (progress > 0.5f)
                    layer.alpha = 1.0f - progress
                else
                    layer.alpha = 0.5f
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?,
                                               currentId: Int) {
            }

            override fun onTransitionTrigger(motionLayout: MotionLayout?,
                                             targetId: Int,
                                             positive: Boolean,
                                             progress: Float) {
            }
        })
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
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            null
        }
    }

}