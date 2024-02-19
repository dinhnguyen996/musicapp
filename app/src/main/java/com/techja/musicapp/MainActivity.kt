package com.techja.musicapp

import android.annotation.SuppressLint
import android.app.ActivityManager
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
import android.widget.Adapter
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techja.musicapp.adapter.AdapterMusic
import com.techja.musicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var adapter: AdapterMusic
    lateinit var recyclerView: RecyclerView
    var isPlaying: Boolean=false //trạng thái phát nhạc
    lateinit var imgSong: ImageView
    private lateinit var titleSong: TextView
    private lateinit var tv_name: TextView
    lateinit var play: ImageView
    private lateinit var stop: ImageView
    lateinit var nextMusic: ImageView
    private lateinit var privious: ImageView
    lateinit var random: ImageView
    lateinit var repeat: ImageView
    lateinit var songList:MutableList<Song>
    //các biến để lấy data  broadcast gửi về
    lateinit var msong: Song
    //Dữ liệu nhận về từ broadcast
    private var broadcastReceiver: BroadcastReceiver=object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            var bundle= intent?.extras
            if (bundle == null){
                return
            }
            msong= bundle.getSerializable("object_song_to_main") as Song
            isPlaying=bundle.getBoolean("status_play")
            var action: String? =bundle.getString("action")
            handlerLayoutMussic(action) //hàm xử lý action nhận về từ broadcast

        }
    }
    //khi thao tác trên notification
    private fun handlerLayoutMussic(action: String?) {
        when(action){

            ServiceMusic.ACTION_PLAY_PAUSE ->{
                showInforSong()
                setStatusPlayOrPause()
            }
            ServiceMusic.ACTION_NEXT ->{
                showInforSong()
                setStatusPlayOrPause()

            }
            ServiceMusic.ACTION_START ->{
                showInforSong()
            }
            ServiceMusic.ACTION_PREVIOUS ->{
                showInforSong()
                setStatusPlayOrPause()
            }
            ServiceMusic.ACTION_STOP ->{

            }
        }

    }
    //hiển thị bài hát
    fun showInforSong(){
        imgSong.setImageResource(msong.imageResId)
        titleSong.text=msong.title
        tv_name.text=msong.name
    }
    fun setStatusPlayOrPause(){
        if (isPlaying){
            play.setImageResource(R.drawable.pause)
        }else{
            play.setImageResource(R.drawable.play)

        }
    }
    override fun onResume() {
        super.onResume()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //đăng ký broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendata_to_activity"))
        //chú ý tên intent filter trùng tên intent bên gửi sang

        //tên và ảnh bài hát
        imgSong=binding.imvSong
        titleSong=binding.titleSong
        titleSong.isSelected = true //kích hoạt chữ chạy
        tv_name=binding.tvName
        play=binding.play
        stop=binding.stop
        nextMusic=binding.next
        privious=binding.previous

        //khởi tạo songList
        songList= mutableListOf(

            Song("WaitingForYou","Mono",R.drawable.music6,R.raw.waitingforyou_mono),
            Song("biển nhớ"," Lân nhã",R.drawable.music5,R.raw.biennho_lannha),
            Song("Tha thứ lỗi lầm","Tuấn Hưng",R.drawable.music3,R.raw.thathuloilam),
            Song("WaitingForYou","Mono",R.drawable.music6,R.raw.waitingforyou_mono),
            Song("biển nhớ"," Lân nhã",R.drawable.music5,R.raw.biennho_lannha),
            Song("Tha thứ lỗi lầm","Tuấn Hưng",R.drawable.music3,R.raw.thathuloilam),
            Song("Cung bạc sầu","MrSiro",R.drawable.music1,R.raw.cungbacsau_mrsiro),
            Song("Gặp em lúc tan vỡ ","Trung Quân idol",R.drawable.music3,R.raw.gapemluctanvo_trungquanidol),
            Song("Ai chung tình được  ","dinh tung huy",R.drawable.music2,R.raw.aichungtinhduocmaidauem_dinhtunghuy),
        )
        //hiển thị lên recycleview
        recyclerView=binding.rcvListMusic
        adapter=AdapterMusic(this,songList)
        recyclerView.adapter=adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //hiển thị bài hát đầu
        imgSong.setImageResource(songList[0].imageResId)
        titleSong.text=songList[0].title
        tv_name.text=songList[0].name

        //hiển thị bài hát được click từ adapter thông qua interface
        adapter.onItemClickListener=object : AdapterMusic.OnItemClickListener{
            override fun onItemClick(song: Song) {
                imgSong.setImageResource(song.imageResId)
                tv_name.text=song.name
                titleSong.text=song.title
                play.setImageResource(R.drawable.pause)
            }

        }
       //chạy bài hát
        play.setOnClickListener {
            if (!isPlaying){
                //nếu !isPlaying bằng true thì
                //vì isPlaying ban đầu =false => phủ địh của nó là true nêu thoả mãn
                startMusicService()
                play.setImageResource(R.drawable.pause)
            }else{
                pauseMusicService()
                play.setImageResource(R.drawable.play)
            }
        }
        stop.setOnClickListener {
            var intentStop: Intent = Intent(this,ServiceMusic::class.java)
            stopService(intentStop)
            play.setImageResource(R.drawable.play)
        }
        //next
        nextMusic.setOnClickListener {
            if (isServiceRunning(ServiceMusic::class.java)){
                var intentNext=Intent(this,ServiceMusic::class.java)
                intentNext.action=ServiceMusic.ACTION_NEXT
                startService(intentNext)
            }

        }
        privious.setOnClickListener {
            if (isServiceRunning(ServiceMusic::class.java)){
            var intentNext=Intent(this,ServiceMusic::class.java)
            intentNext.action=ServiceMusic.ACTION_PREVIOUS
            startService(intentNext)
            }
        }

    }
    fun startMusicService() {
        var intentStart: Intent = Intent(this,ServiceMusic::class.java)
        var bundle:Bundle=Bundle()
        bundle.putSerializable("song_list",songList.toTypedArray())
        intentStart.putExtras(bundle)
        startService(intentStart) // sẽ chạy hàm onStartCommand

    }
    //hàm kiểm tra service đã được chạy hay chưa
    @SuppressLint("ServiceCast")
    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun pauseMusicService() {
        val intentPause: Intent = Intent(this, ServiceMusic::class.java)
        // Gửi một intent để dịch vụ biết là muốn tạm dừng bài hát
        intentPause.action = ServiceMusic.ACTION_PLAY_PAUSE
        startService(intentPause)
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

}