package com.riku1227.spoilerd

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

class LibraryDataAdapter (private val context: Context,private val licenceList: ArrayList<LibraryData>): RecyclerView.Adapter<LibraryDataAdapter.Companion.LibraryDataViewHolder>() {
    companion object {
        class LibraryDataViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val libraryName: TextView = view.findViewById(R.id.libraryName)
            val libraryLicenceButton: Button = view.findViewById(R.id.libraryLicenceButton)
            val licenceText: TextView = view.findViewById(R.id.licenceText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryDataViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_licence_card, parent, false)
        return LibraryDataViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return licenceList.size
    }

    override fun onBindViewHolder(holder: LibraryDataViewHolder, position: Int) {
        holder.libraryName.text = licenceList[position].libraryName
        holder.libraryLicenceButton.text = licenceList[position].libraryLicenceName
        val licenceText = try {
            val licenceTextFile = context.assets.open(licenceList[position].libraryLicenceFileName)
            val bufferedReader = BufferedReader(InputStreamReader(licenceTextFile))
            bufferedReader.use {
                it.readText()
            }

        } catch (e: Exception) {
            "Error"
        }

        holder.licenceText.text = licenceText
        holder.licenceText.movementMethod = ScrollingMovementMethod.getInstance()
        holder.libraryLicenceButton.setOnClickListener {
            if(holder.licenceText.visibility == View.VISIBLE) {
                holder.licenceText.visibility = View.GONE
            } else {
                holder.licenceText.visibility = View.VISIBLE
            }
        }
    }
}