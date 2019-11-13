package com.riku1227.spoilerd

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*

class MainActivity : AppCompatActivity() {

    private val requestCode = 4815
    private var cachePath = ""
    private var imageAdapter: ImageAdapter? = null
    val isSelectedList = ArrayList<Boolean>()
    private var notGrantedTextView: TextView? = null
    private var notGrantedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        cachePath = "${baseContext.cacheDir.path}/image/"

        notGrantedTextView = textView
        notGrantedButton = grantPermissionButton

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            textView.visibility = View.VISIBLE
            grantPermissionButton.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            setupRecycler(recyclerView)
        }

        shortcutAction()

        grantPermissionButton.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(rootLayout, resources.getString(R.string.grant_permission_ftom_settings), Snackbar.LENGTH_LONG)
                        .setAction(resources.getString(R.string.open_settings)) {
                            val intent = Intent()
                            intent.action = (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", baseContext.packageName, null)
                            startActivity(intent)
                        }
                        .show()
                } else {
                    this.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001)
                }
            }
        }

        selectImageSAF.setOnClickListener {
            if(checkDiscord(baseContext)) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        type = "image/*"
                    }

                    startActivityForResult(intent, requestCode)
                }
            }
        }

        clearCacheButton.setOnClickListener {
            clearCache()
            Snackbar.make(rootLayout, resources.getString(R.string.cache_deleted), Snackbar.LENGTH_SHORT).show()
        }

        floatingActionButton.setOnClickListener {
            if(checkDiscord(baseContext)) {
                val selectedImageFileNameList = arrayListOf<String>()
                for(i in 0 until isSelectedList.size) {
                    if(isSelectedList[i]) {
                        val fileName = "SPOILER_${imageAdapter!!.imageFileName[i]}"
                        copyFile(contentResolver.openInputStream(Uri.parse(imageAdapter!!.imageUriList[i]))!!, fileName)
                        selectedImageFileNameList.add(fileName)
                    }
                }

                selectedImageFileNameList.apply {
                    if(!isEmpty()) {
                        if(size == 1) {
                            intentToDiscord(baseContext, this[0])
                        } else {
                            intentToDiscord(baseContext, null, this)
                        }
                    }
                }
            }
        }

        val settingsPreferences = getSharedPreferences("${BuildConfig.APPLICATION_ID}_preferences", Context.MODE_PRIVATE)

        if(settingsPreferences.getBoolean("update_check_on_auto", true)) {
            val preferences = getSharedPreferences("app_data", Context.MODE_PRIVATE)
            val nowTime = System.currentTimeMillis() / 1000
            val lastCheckUpdateTime = preferences.getLong("last_check_update_time", 0)
            if((nowTime - lastCheckUpdateTime) > 21600) {
                checkUpdate(false, settingsPreferences.getBoolean("auto_update_check_on_wifi_only", true), true)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK && intentData != null) {
            intentData.apply {
                if(data != null) {
                    val fileName = "SPOILER_${getFileNameFromURI(baseContext, data!!)}"
                    copyFile(baseContext.contentResolver.openInputStream(data!!)!!, fileName)
                    intentToDiscord(baseContext, fileName)
                } else {
                    if(clipData != null) {
                        val fileNameList = arrayListOf<String>()
                        for(i in 0 until clipData!!.itemCount) {
                            val uri = clipData?.getItemAt(i)?.uri
                            val fileName = "SPOILER_${getFileNameFromURI(baseContext, uri!!)}"
                            copyFile(baseContext.contentResolver.openInputStream(uri)!!, fileName)
                            fileNameList.add(fileName)
                        }
                        intentToDiscord(baseContext, null, fileNameList)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 1001 && grantResults.isNotEmpty()) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(rootLayout, resources.getString(R.string.permission_is_not_granted), Snackbar.LENGTH_SHORT).show()
                return
            }
            notGrantedTextView?.visibility = View.GONE
            notGrantedButton?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            setupRecycler(recyclerView)
        } else {
            Snackbar.make(rootLayout, resources.getString(R.string.permission_is_not_granted), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.open_app_info -> {
                val intent = Intent(baseContext, AppInfoActivity::class.java)
                startActivity(intent)
            }

            R.id.check_update -> {
                checkUpdate()
            }

            R.id.app_settings -> {
                val intent = Intent(baseContext, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkUpdate(showSnackBar: Boolean = true, isWiFiOnly: Boolean = false, isAutoUpdateCheck: Boolean = false) {
        GlobalScope.launch {
            val result = Update.checkUpdate(baseContext, isWiFiOnly, isAutoUpdateCheck)

            if(result != null) {
                val preferences = getSharedPreferences("app_data", Context.MODE_PRIVATE)
                preferences.edit().let {
                    val nowTime = System.currentTimeMillis() / 1000
                    it.putLong("last_check_update_time", nowTime)
                    it.apply()
                }

                if(result.isUpdate) {
                    val dialog = UpdateDialog()
                    val bundle = Bundle()
                    bundle.putString("updateVersion", result.updateVersion)
                    bundle.putString("currentVersion", result.currentVersion)
                    bundle.putString("updateFileUrl", result.updateFileUrl)
                    dialog.arguments = bundle
                    dialog.isCancelable = false
                    dialog.show(supportFragmentManager, "Test")
                } else {
                    if(showSnackBar) {
                        Snackbar.make(rootLayout, resources.getString(R.string.app_is_latest_version), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getFileNameFromURI(context: Context, uri: Uri): String {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        var name = ""
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(0)
            }
            cursor.close()
        }
        return name
    }

    private fun copyFile(input: InputStream, outputFileName: String) {
        File(cachePath).apply {
            if(!this.exists()) {
                this.mkdirs()
            }
        }
        val outputPath = "$cachePath$outputFileName"
        val bufferedInput = BufferedInputStream(input)
        val bufferedOutput = BufferedOutputStream(FileOutputStream(outputPath))
        var i = 0
        val b = ByteArray(8192)
        while ({i = bufferedInput.read(b); i}() != -1) {
            bufferedOutput.write(b,0,i)
        }
        bufferedOutput.flush()
        bufferedInput.close()
        bufferedOutput.close()
    }

    private fun intentToDiscord(context: Context, singleFileName: String? = null, multiFileNames: ArrayList<String>? = null) {
        if(singleFileName != null) {
            val contentUri = FileProvider.getUriForFile(context, this.packageName, File("$cachePath$singleFileName"))
            Intent(Intent.ACTION_SEND).apply {
                setClassName("com.discord", "com.discord.app.AppActivity\$AppAction")
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                startActivity(this)
            }
        } else if(multiFileNames != null) {
            val uriList = arrayListOf<Uri>()
            for(fileName in multiFileNames) {
                val contentUri = FileProvider.getUriForFile(context, this.packageName, File("$cachePath$fileName"))
                uriList.add(contentUri)
            }
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                setClassName("com.discord", "com.discord.app.AppActivity\$AppAction")
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uriList)
                startActivity(this)
            }
        }
    }

    private fun clearCache() {
        val cacheDir = File(cachePath)
        cacheDir.deleteRecursively()
        val externalCacheDir = baseContext.externalCacheDir
        if(externalCacheDir != null) {
            val updateFileDir = File(externalCacheDir, "update_file")
            updateFileDir.deleteRecursively()
        }
    }

    private fun getAllImage(activity: Activity): HashMap<String, ArrayList<String>> {
        val allImageHashMap = HashMap<String, ArrayList<String>>()

        val imageUriList = ArrayList<String>()
        val imageFileNameList = ArrayList<String>()

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DISPLAY_NAME
        )
        val c = activity.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    c.getLong(0))

                imageUriList.add(contentUri.toString())
                imageFileNameList.add(c.getString(1))
            }
            c.close()

            allImageHashMap["URIList"] = imageUriList
            allImageHashMap["DISPLAY_NAME"] = imageFileNameList

        }
        return allImageHashMap
    }

    private fun checkDiscord(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.discord", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Snackbar.make(rootLayout, resources.getString(R.string.discord_is_not_installed), Snackbar.LENGTH_SHORT).show()
            false
        }
    }

    private fun setupRecycler(recyclerView: RecyclerView) {
        val imageHashMap = getAllImage(this)
        if(imageHashMap["URIList"]!!.isNotEmpty()) {
            val uriList = ArrayList(imageHashMap["URIList"]!!.reversed())
            val fileNameList = ArrayList(imageHashMap["DISPLAY_NAME"]!!.reversed())
            for(i in 0 until imageHashMap["URIList"]!!.size) {
                isSelectedList.add(false)
            }
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(baseContext, 3)
            imageAdapter = ImageAdapter(this, baseContext, uriList, fileNameList)
            recyclerView.adapter = imageAdapter
        }
    }

    private fun shortcutAction() {
        if(!intent.hasExtra("shortcut_id")) {
            return
        }
        when (intent.getStringExtra("shortcut_id")) {
            "select_image_saf" -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    type = "image/*"
                }

                startActivityForResult(intent, requestCode)
            }
        }
    }
}
