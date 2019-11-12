package com.riku1227.spoilerd

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_app_info.*

class AppInfoActivity : AppCompatActivity() {

    val webSiteUrl = "https://riku1227.github.io/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = resources.getString(R.string.app_info)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
        appVersion.text = resources.getString(R.string.app_version, packageManager.getPackageInfo(packageName, 0).versionName, versionCode.toString())
        val signatureType = if(Update.getSignature(baseContext) == Update.releaseSignature) {
            "Release key"
        } else {
            "Debug key"
        }
        appSignature.text = resources.getString(R.string.signature_type, signatureType)

        websiteButton.setOnClickListener {
            val uri = Uri.parse(webSiteUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        oSSLicenceButton.setOnClickListener {
            val intent = Intent(baseContext, OSSLicenceActivity::class.java)
            startActivity(intent)
        }

        changeLogButton.setOnClickListener {
            val uri = Uri.parse("https://raw.githubusercontent.com/riku1227/spoilerd/master/update_files/changelog.txt")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}
