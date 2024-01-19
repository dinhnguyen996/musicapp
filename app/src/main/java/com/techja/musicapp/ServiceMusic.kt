package com.techja.musicapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteView
import androidx.core.app.NotificationCompat

class ServiceMusic : Service() {

    companion object {
        //biến sử dụng các action
        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"

        // đặt biến để sử dụng cho broadcast
        const val BROADCAST_UPDATE_SONG = "com.techja.musicapp.UPDATE_SONG"//tên broadcast
        const val EXTRA_SONG_TITLE = "extra_song_title" //trường được gửi đi
        const val EXTRA_SONG_IMAGE_RES_ID = "extra_song_image_res_id"
    }
    lateinit var mediaPlayer: MediaPlayer
    lateinit var listSong: List<Song>
    var currentSongIndex: Int =0


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
//khởi tạo đối tượng quản lý
    override fun onCreate() {
        super.onCreate()
    //tạo notification
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "music_channel",
            "Music Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    //khơi tạo danh sách bài hát
    listSong= listOf(
        Song("Ai chung tình được mãi đâu em",R.drawable.music1,R.raw.aichungtinhduocmaidauem_dinhtunghuy),
    Song("Anh Luôn Như vậy",R.drawable.music2,R.raw.anhluonnhuvay_bray),
    Song("Em Xinh",R.drawable.music3,R.raw.emxinh_momo),
    Song("Biển nhớ",R.drawable.music4,R.raw.biennho_lannha)
    )
    sendUpdateBroadcast(listSong[currentSongIndex].title, listSong[currentSongIndex].imageResId)
    currentSongIndex=0
    initMediaPlayer()
    }
//đối tượng được quản lý
    @SuppressLint("RemoteViewLayout")
    private fun initMediaPlayer() {
        mediaPlayer=MediaPlayer.create(this,listSong[currentSongIndex].rawResId)//quản lý đội tượng đâu tiên có index 0
    // Lấy thông tin từ đối tượng Song và hiển thị
    val currentSong = listSong[currentSongIndex]
    sendUpdateBroadcast(currentSong.title,currentSong.imageResId)


        mediaPlayer.setOnCompletionListener {
            // Xử lý sự kiện khi một bài hát kết thúc
            //tuy nằm trong oncreate nhưng chưa chạy cho tới khi có bài hát được phát và kết thúc
            playNextSong()
        }
    // Tạo notification
    val notificationIntent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    var bitmap: Bitmap=BitmapFactory.decodeResource(resources,currentSong.imageResId)
    var remotviews=RemoteViews(packageName,R.layout.notification)
    remotviews.setTextViewText(R.id.tv_noti_title,currentSong.title)
    remotviews.setImageViewBitmap(R.id.im_noti_song,bitmap)

    remotviews.setImageViewResource(R.id.start_or_pause_noti_song,R.drawable.play)

    val builder = NotificationCompat.Builder(this, "music_channel")
//        .setContentTitle("Music App")
//        .setContentText(listSong[currentSongIndex].title)
        .setSmallIcon(listSong[currentSongIndex].imageResId)
        .setContentIntent(pendingIntent)
        .setCustomContentView(remotviews)
        .setOngoing(true)  // Notification sẽ không bị hủy khi người dùng chạm vào nó
//        .addAction(R.drawable.previous, "Previous", getPendingIntent(ACTION_PREVIOUS))
//        .addAction(if (mediaPlayer.isPlaying) R.drawable.pause else R.drawable.play, "Pause", getPendingIntent(ACTION_PLAY_PAUSE))
//        .addAction(R.drawable.next, "Next", getPendingIntent(ACTION_NEXT))

    startForeground(1, builder.build())
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, ServiceMusic::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    //phương thức gửi broadcast
    private fun sendUpdateBroadcast(title: String, imageResId: Int) {
        val intent = Intent(BROADCAST_UPDATE_SONG)//tên broadcast được gửi đi
        intent.putExtra(EXTRA_SONG_TITLE, title)
        intent.putExtra(EXTRA_SONG_IMAGE_RES_ID, imageResId)
        sendBroadcast(intent)
    }

    private fun playNextSong() {
        currentSongIndex = (currentSongIndex + 1) % listSong.size
        mediaPlayer.reset()
        initMediaPlayer()
        mediaPlayer.start()
    }
    private fun playPreviousSong() {
        currentSongIndex = (currentSongIndex - 1 + listSong.size) % listSong.size
        mediaPlayer.reset()
        initMediaPlayer()
        mediaPlayer.start()
    }

    //hàm khởi động đối tượgn mà service quản lý
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.start()
                }
            }
            ACTION_NEXT -> playNextSong()
            ACTION_PREVIOUS -> playPreviousSong()
        }

        return super.onStartCommand(intent, flags, startId)
    }
    //hàm để dừng đội tượng mà service quản lý
    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }
}