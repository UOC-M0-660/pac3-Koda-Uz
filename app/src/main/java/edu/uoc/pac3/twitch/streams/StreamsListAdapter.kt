package edu.uoc.pac3.twitch.streams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.uoc.pac3.R
import edu.uoc.pac3.data.streams.Stream

class StreamsListAdapter: RecyclerView.Adapter<StreamsListAdapter.ViewHolder>() {

    private var streams = ArrayList<Stream>()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val userName: TextView = view.findViewById(R.id.user_name)
        val thumbnailImage: ImageView = view.findViewById(R.id.user_icon)
    }

    fun addStreams(streams: List<Stream>) {
        this.streams.addAll(streams)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_holder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stream = streams[position]
        holder.title.text = stream.title
        holder.userName.text = stream.userName
        var url = stream.thumnailUrl
        url = url?.replace("{width}", "854")
        url = url?.replace("{height}", "480")
        Glide.with(holder.view.context)
                .load(url)
                .into(holder.thumbnailImage)
    }

    override fun getItemCount(): Int {
        return streams.size
    }
}