package com.translator.app.speak.to.world.speaktoworld.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.translator.app.speak.to.world.speaktoworld.R
import com.translator.app.speak.to.world.speaktoworld.data.DataAppList


class AdapterAppList(val context: Context, private val list: List<DataAppList>) :
    RecyclerView.Adapter<AdapterAppList.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_app_list_vh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_app, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Glide.with(context).load(list[position].image).placeholder(R.drawable.a2).into(holder.image)
        Picasso.get().load(list[position].image).placeholder(R.drawable.a2).into(holder.image)
        holder.image.setOnClickListener() {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${list[position].link}"))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}