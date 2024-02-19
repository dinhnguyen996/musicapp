package com.techja.musicapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.techja.musicapp.R
import com.techja.musicapp.ServiceMusic
import com.techja.musicapp.Song

class AdapterMusic(var context:Context,var listSong: MutableList<Song>):
    RecyclerView.Adapter<AdapterMusic.MyViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val tv_title:TextView=itemView.findViewById(R.id.tv_title_item)
        val tv_single:TextView=itemView.findViewById(R.id.tv_single_item)
        val imv_song:ImageView=itemView.findViewById(R.id.imv_song_item)
    }
    //interface để thực hiện hiển thị bài hát dc click
    interface OnItemClickListener {
        fun onItemClick(song: Song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.item_adapter_layout,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var curent=listSong[position]
        holder.itemView.setOnClickListener{
            onItemClickListener?.onItemClick(curent)//gửi bài hát được chọn qua
            // Gửi Intent để bắt đầu chơi bài hát khi item được click
        val playIntent = Intent(context, ServiceMusic::class.java)
        playIntent.action = ServiceMusic.ACTION_START
        playIntent.putExtra("song_to_play", curent)
        context.startService(playIntent)
        }


        //hiển thị
        holder.tv_title.text=curent.title
        holder.tv_single.text=curent.name
        holder.imv_song.setImageResource(curent.imageResId)
    }
}