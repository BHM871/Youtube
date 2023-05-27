package com.example.youtube.commun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.youtube.R
import com.example.youtube.model.ListVideo
import com.example.youtube.model.Video
import com.example.youtube.model.formatted
import com.squareup.picasso.Picasso

class VideoAdapter(
    private val list: List<Video>?,
    private val isDetail: Boolean,
    val onClick: (Video) -> Unit
) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val viewHolder = if (isDetail) {
            VideoViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_similar, parent, false)
            )
        } else {
            VideoViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item_video, parent, false)
            )
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        if (list != null) holder.bind(list[position])
    }

    override fun getItemCount(): Int = list?.size!!

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(video: Video) {
            with(itemView) {
                setOnClickListener {
                    onClick.invoke(video)
                }

                if (isDetail) {
                    findViewById<AppCompatTextView>(R.id.item_similar_txt_title).text = video.title
                    findViewById<AppCompatTextView>(R.id.item_similar_txt_info).text = context.getString(
                        R.string.info_similar,
                        video.publisher.name,
                        video.viewCountLabel,
                        video.publishedAt.formatted()
                    )
                } else {
                    Picasso.get().load(video.thumbnailUrl)
                        .into(findViewById<AppCompatImageView>(R.id.item_video_thumbnail))
                    Picasso.get().load(video.publisher.pictureProfileUrl)
                        .into(findViewById<AppCompatImageView>(R.id.item_video_author))

                    findViewById<AppCompatTextView>(R.id.item_video_title).text = video.title
                    findViewById<AppCompatTextView>(R.id.item_video_info).text = context.getString(
                        R.string.info,
                        video.publisher.name,
                        video.viewCount,
                        video.publishedAt.formatted()
                    )
                }
            }
        }
    }
}