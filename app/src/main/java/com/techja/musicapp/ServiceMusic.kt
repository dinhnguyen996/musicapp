package com.techja.musicapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ServiceMusic : Service() {
    private var isPaused = false //kiểm tra xem media có đang tạm dừng hay không
    var mediaPlayer: MediaPlayer? = null
    var isplaying: Boolean = false
    var currentSongIndex = 0
    lateinit var songList: Array<Song>
    lateinit var msong: Song

    companion object {
        const val MUSIC_CHANNEL_ID = "MUSIC_CHANNEL_ID"
        const val REQUEST_CODE = 123

        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_STOP = "action_stop"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
        const val ACTION_START = "action_start"
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    //khởi tạo đối tượng quản lý
    override fun onCreate() {
        super.onCreate()
        //tạo notification
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MUSIC_CHANNEL_ID, "Music Channel", NotificationManager.IMPORTANCE_HIGH
            )
            // Set âm thanh cho channel (nếu cần)
            channel.setSound(null, null)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isplaying = true//cho biến là true để hiẻn thị ảnh play
        //lấy bài hát từ main gửi sang
        if (intent != null && intent.extras != null) {
            songList = (intent.extras!!.getSerializable("song_list") as? Array<Song>) ?: emptyArray()
            if (songList.isNotEmpty()) {
                // Xử lý danh sách bài hát ở đây
                msong = songList[currentSongIndex]
                sennotification(msong)
                startMusic(msong)
                senActionActivity(ACTION_START)//gửi để hiển thị bài hát ngay từ đầu
            }
        }

        //nhận action từ broadcast
        when (intent?.action) {
            //action được gửi từ adapter
            ACTION_START -> {
                // Xử lý hành động start bài hát mới
                val msong = intent.getSerializableExtra("song_to_play") as? Song
                if (msong != null) {
                    startMusic(msong)
                    isplaying = true
                    sennotification(msong)

                }
            }
            ACTION_PLAY_PAUSE -> {
                // Xử lý hành động play/pause
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    isPaused = true
                    isplaying = false
                    sennotification(msong)
                } else {
                    mediaPlayer?.start()
                    Log.d("mediaPlayer", "mediaPlayer: ${mediaPlayer?.currentPosition}")
                    isplaying = true
                    isPaused = false
                    sennotification(msong)
                }
                senActionActivity(ACTION_PLAY_PAUSE)//gửi action sau khi thao tác trên notification
            }
            ACTION_STOP -> {
                // Xử lý hành động stop
                stopSelf()

                senActionActivity(ACTION_STOP)
            }
            ACTION_NEXT -> {
                if (mediaPlayer?.isPlaying == true || isPaused) {
                    // Nếu media đang phát hoặc tạm dừng, tạm dừng nó và chuyển sang bài hát tiếp theo
                    mediaPlayer?.stop()
                    mediaPlayer?.release() //sẽ k giữ trạng thái và có thẻ phát bài tiếp theo
                    mediaPlayer = null
                    isPaused =
                        true //giúp giải phóng media khi đang phát để sang bài mới k bị phát 2 bài
                    senActionActivity(ACTION_NEXT)
                }
                // Chuyển đến bài hát tiếp theo và bắt đầu phát
                currentSongIndex = (currentSongIndex + 1) % songList.size
                msong = songList[currentSongIndex]
                startMusic(msong)
                sennotification(msong)
                senActionActivity(ACTION_NEXT)
            }

            ACTION_PREVIOUS -> {
                if (currentSongIndex > 0) {
                    // Lùi lại bài hát trước đó nếu có
                    currentSongIndex = (currentSongIndex - 1) % songList.size
                } else {
                    // Nếu đang ở bài hát đầu tiên, chuyển sang bài hát cuối cùng
                    currentSongIndex = songList.size - 1
                }
                if (mediaPlayer?.isPlaying == true || isPaused) {
                    // Nếu media đang phát hoặc tạm dừng, tạm dừng nó và chuyển sang bài hát tiếp theo
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    isPaused = true
                }

                msong = songList[currentSongIndex]
                startMusic(msong)
                sennotification(msong)
                senActionActivity(ACTION_PREVIOUS)
            }
        }
        return START_NOT_STICKY
    }


    private fun startMusic(msong: Song) {
        if (mediaPlayer != null && isPaused) {
            // Đang phát nhưng tạm dừng, tiếp tục phát
            mediaPlayer?.start()
            isPaused = false
        } else {
            // Nếu có media đang chạy, giải phóng tài nguyên trước khi khởi tạo mới
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }

            mediaPlayer = MediaPlayer.create(this, msong.rawResId)
            mediaPlayer?.setOnCompletionListener {
                // Sự kiện khi bài hát kết thúc
                currentSongIndex = (currentSongIndex + 1) % songList.size
                var xsong = songList[currentSongIndex]
                startMusic(xsong)
                sennotification(xsong)

            }
            mediaPlayer?.start()
        }
    }

    //hàm này sẽ nhận action tương ứng từ sennotification sau đó gửi sang broadcast
    private fun createPendingintent(actionPendingIntent: String): PendingIntent {
        val intent = Intent(this, MyReciver::class.java)//khởi toạ nơi dữ liệu gửi đi
        intent.action = actionPendingIntent //gán action tung ứng để gửi đi cho broadcast
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    @SuppressLint("RemoteViewLayout")
    private fun sennotification(song: Song) {
        //khi nhấn vào notification sẽ vào activity
        val intent = Intent(this, ServiceMusic::class.java)
        var pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        var remotviews = RemoteViews(packageName, R.layout.notification_layout)//layout hiển thị
        remotviews.setTextViewText(R.id.tv_noti_title, song.title)
        remotviews.setTextViewText(R.id.tv_noti_name, song.name)
        remotviews.setTextViewText(R.id.tv_title_app,"Music app")
        var bitmap: Bitmap = BitmapFactory.decodeResource(resources, song.imageResId)
        remotviews.setImageViewBitmap(R.id.im_noti_song, bitmap)
        if (isplaying) {
            remotviews.setImageViewResource(R.id.start_or_pause_noti_song, R.drawable.pause)
        } else {
            remotviews.setImageViewResource(R.id.start_or_pause_noti_song, R.drawable.play)
        }
//        remotviews.setImageViewResource(R.id.start_or_pause_noti_song,R.drawable.play)

        //tạo các pendingintent tương ứng với ACTION được khai báo ban đầu
        val playPauseIntent = createPendingintent(ACTION_PLAY_PAUSE)
        val stopIntent = createPendingintent(ACTION_STOP)
        val nextIntent = createPendingintent(ACTION_NEXT)
        val previousIntent = createPendingintent(ACTION_PREVIOUS)

        //lắng nghe click item trên notification ,mỗi item set  với mỗi pendingintent tương ứng ở trên
        remotviews.setOnClickPendingIntent(R.id.start_or_pause_noti_song, playPauseIntent)
        remotviews.setOnClickPendingIntent(R.id.imv_close_service, stopIntent)
        remotviews.setOnClickPendingIntent(R.id.imv_next, nextIntent)
        remotviews.setOnClickPendingIntent(R.id.imv_privious, previousIntent)

        val notification: Notification =
            NotificationCompat.Builder(this, MUSIC_CHANNEL_ID).setSmallIcon(R.drawable.music7)
                .setContentIntent(pendingIntent).setCustomContentView(remotviews).setSound(null)
                .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }

    //fun truyền action đi để main biết đang thao tác gì
    fun senActionActivity(action: String) {
        var intent = Intent("sendata_to_activity")
        var bundle = Bundle()
        bundle.putSerializable("object_song_to_main", msong)//put bài hát
        bundle.putBoolean("status_play", isplaying) //put trạng thái có đang phat nhạc hay không
        bundle.putString("action", action) //put action được truyền vào hàm
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}