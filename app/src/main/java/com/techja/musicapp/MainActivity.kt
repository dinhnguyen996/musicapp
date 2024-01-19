package com.techja.musicapp

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import com.techja.musicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var listSong: List<Song>
    var flag: Boolean=true // cờ đẻ thay đổi pause,play
    var flagrepeat:Boolean =true // cờ check repeat
    lateinit var imgSong: ImageView
    lateinit var titleSong: TextView
    lateinit var play: ImageView
    lateinit var stop: ImageView
    lateinit var nextMusic: ImageView
    lateinit var privious: ImageView
    lateinit var random: ImageView
    lateinit var repeat: ImageView
    override fun onResume() {
        super.onResume()

    }

    private val updateSongReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ServiceMusic.BROADCAST_UPDATE_SONG) {
                val songTitle = intent.getStringExtra(ServiceMusic.EXTRA_SONG_TITLE)
                val imageResId = intent.getIntExtra(ServiceMusic.EXTRA_SONG_IMAGE_RES_ID, 0)

                // Cập nhật giao diện người dùng với thông tin mới của bài hát
                updateUI(songTitle, imageResId)
            }
        }
    }
    fun updateUI(songTitle: String?, imageResId: Int) {
        // Cập nhật tên và ảnh của bài hát trong giao diện người dùng
        titleSong.text = songTitle
        imgSong.setImageResource(imageResId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //tên và ảnh bài hát
        imgSong=binding.imvSong
        titleSong=binding.titleSong

        play=binding.play
        stop=binding.stop
        nextMusic=binding.next
        privious=binding.previous
        random=binding.random
        repeat=binding.repeat

        // Đăng ký BroadcastReceiver
        val filter = IntentFilter(ServiceMusic.BROADCAST_UPDATE_SONG)
        registerReceiver(updateSongReceiver, filter)


       //chạy bài hát
        play.setOnClickListener {
           var intentStart: Intent = Intent(this,ServiceMusic::class.java)
            intentStart.action=ServiceMusic.ACTION_PLAY_PAUSE
            startService(intentStart) // sẽ chạy hàm onStartCommand

            if (flag){
                //kiểm tra trạng thái ban đầu để set ảnh phù hợp
                play.setImageResource(R.drawable.pause)
                flag=false
            }else{
                play.setImageResource(R.drawable.play)
                flag=true
            }
        }
        // dừng bài hát, dừng service
        stop.setOnClickListener {
            var intentStop=Intent(this,ServiceMusic::class.java)
            stopService(intentStop)// sẽ chạy hàm onDestroy
            //khi nó đang chạy sau khi nhấn stop sẽ set ảnh thành play , và sét lại cờ
            play.setImageResource(R.drawable.play)
            flag=true

        }
        //repeat
        repeat.setOnClickListener {
            flagrepeat = if (flagrepeat){
                repeat.setImageResource(R.drawable.repeattrue)

                false
            }else{
                repeat.setImageResource(R.drawable.repeat)
                true
            }
        }
        //Next bài
        nextMusic.setOnClickListener {
            var intentNext: Intent=Intent(this,ServiceMusic::class.java)
            intentNext.action=ServiceMusic.ACTION_NEXT
            startService(intentNext)
        }
        //privious
        privious.setOnClickListener {
            var intentPrivious=Intent(this,ServiceMusic::class.java)
            intentPrivious.action=ServiceMusic.ACTION_PREVIOUS
            startService(intentPrivious)
        }

    }

}