package com.example.youtube.model

import java.text.SimpleDateFormat
import java.util.*

data class Video(
    val id: String,
    val thumbnailUrl: String,
    val title: String,
    val publishedAt: Date,
    val viewCount: Long,
    val viewCountLabel: String,
    val duration: Int,
    val videoUrl: String,
    val publisher: Publisher
)

data class Publisher(
    val id: String,
    val name: String,
    val pictureProfileUrl: String
)

data class ListVideo(
    val status: Int,
    val data: List<Video>
)

class PublisherBuilder {
    var id: String = ""
    var name: String = ""
    var pictureProfileUrl: String = ""

    fun build() : Publisher =
        Publisher(id, name, pictureProfileUrl)
}

//Classe Builder, para criar objetos
class VideoBuilder {
    var id: String = ""
    var thumbnailUrl: String = ""
    var title: String = ""
    var publishedAt: Date = Date()
    var viewCount: Long = 0
    var viewCountLabel: String = ""
    var duration: Int = 0
    var videoUrl: String = ""
    var publisher: Publisher = PublisherBuilder().build()

    //Função build, cria efetivamente o objeto
    fun build() : Video =
        Video(id, thumbnailUrl, title, publishedAt, viewCount, viewCountLabel, duration, videoUrl, publisher)

    fun publisher(block: PublisherBuilder.() -> Unit) : Publisher =
        PublisherBuilder().apply(block).build()
}

//DSL: facilita a criação do objeto, tornando facilmente visível como um JSON
fun video(block: VideoBuilder.() -> Unit) : Video =
    VideoBuilder().apply(block).build()

//Utilização do DSL
fun videos() : List<Video> =
    arrayListOf(
        video {
            id = "aaaaa"
            title = "aaaaa"
            viewCount = 5346
            viewCountLabel = "5.3k"
            publishedAt = "2023-03-23".toDate()
            publisher {
                name = "Meu ovo 1"
            }
        },
        video {
            id = "bbbbb"
            title = "bbbbb"
            viewCount = 65432
            viewCountLabel = "65.4k"
            publishedAt = "2023-02-02".toDate()
            publisher {
                name = "Meu ovo 2"
            }
        },
        video {
            id = "ccccc"
            title = "ccccc"
            viewCount = 8434
            viewCountLabel = "8.4k"
            publishedAt = "2022-11-27".toDate()
            publisher {
                name = "Meu ovo 3"
            }
        },
        video {
            id = "ddddd"
            title = "ddddd"
            viewCount = 2345
            viewCountLabel = "2.3k"
            publishedAt = "2022-12-23".toDate()
            publisher {
                name = "Meu ovo 4"
            }
        }
    )

fun Date.formatted() : String =
    SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR")).format(this)

fun String.toDate() : Date =
    SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR")).parse(this) ?: Date()

fun Long.formatTime() : String {
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun Int.formatTime() : String {
    val minutes = this / 10 / 60
    val seconds = this / 10 % 60
    return String.format("%02d:%02d", minutes, seconds)
}