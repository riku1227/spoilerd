package com.riku1227.spoilerd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.android.synthetic.main.activity_osslicence.*

class OSSLicenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_osslicence)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = resources.getString(R.string.oss_licence)

        val libraryDataList= arrayListOf<LibraryData>()
        libraryDataList.add(LibraryData("Android AppCompat Library", "Apache License 2.0", "apache_license_2.0.txt"))
        libraryDataList.add(LibraryData("Android ConstraintLayout", "Apache License 2.0", "apache_license_2.0.txt"))
        libraryDataList.add(LibraryData("Android Support RecyclerView", "Apache License 2.0", "apache_license_2.0.txt"))
        libraryDataList.add(LibraryData("Core Kotlin Extensions", "Apache License 2.0", "apache_license_2.0.txt"))
        libraryDataList.add(LibraryData("Glide", "View licence", "glide_licence.txt"))
        libraryDataList.add(LibraryData("Kotlin Standard Library JDK 7", "Apache License 2.0", "apache_license_2.0.txt"))
        libraryDataList.add(LibraryData("Material Components for Android", "Apache License 2.0", "apache_license_2.0.txt"))
        libraryDataList.add(LibraryData("OkHttp", "Apache License 2.0", "apache_license_2.0.txt"))

        val linearLayoutManager = LinearLayoutManager(baseContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        licenceRecyclerView.layoutManager = linearLayoutManager
        licenceRecyclerView.adapter = LibraryDataAdapter(baseContext, libraryDataList)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}
