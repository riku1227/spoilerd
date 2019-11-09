package com.riku1227.spoilerd

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(private val activity: MainActivity,private val context: Context, val imageUriList: ArrayList<String>, val imageFileName: ArrayList<String>) : RecyclerView.Adapter<ImageAdapter.Companion.ImageViewHolder>() {
    companion object {
        class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val thumbnailImage: ImageView = view.findViewById(R.id.thumbnailImage)
            val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_image, parent, false)
        return ImageViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return imageUriList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = Uri.parse(imageUriList[position])
        Glide.with(context)
            .load(uri)
            .fitCenter()
            .into(holder.thumbnailImage)

        updateCheckBox(holder, activity.isSelectedList[position])

        holder.thumbnailImage.setOnClickListener {
            activity.isSelectedList[position] = !activity.isSelectedList[position]
            updateCheckBox(holder, activity.isSelectedList[position])
        }

        holder.checkBox.setOnClickListener {
            activity.isSelectedList[position] = !activity.isSelectedList[position]
            updateCheckBox(holder, activity.isSelectedList[position])
        }
    }

    private fun updateCheckBox(holder: ImageViewHolder, checked: Boolean) {
        if(checked) {
            holder.checkBox.isChecked = true
            holder.checkBox.visibility = View.VISIBLE
        } else {
            holder.checkBox.isChecked = false
            holder.checkBox.visibility = View.GONE
        }
    }
}