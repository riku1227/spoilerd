package com.riku1227.spoilerd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.*

class Update {
    companion object {
        data class Data(
            val isUpdate: Boolean,
            val updateVersion: String,
            val updateVersionCode: Long,
            val updateFileUrl: String,
            val currentVersion: String,
            val currentVersionCode: Long
        )

        @SuppressLint("WrongConstant")
        fun isNetworkConnect(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = cm.activeNetwork
                val networkCapabilities = cm.getNetworkCapabilities(activeNetwork)
                if(networkCapabilities != null) {
                    when {
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                } else {
                    false
                }
            } else {
                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                activeNetwork?.isConnectedOrConnecting ?: false
            }
        }

        private const val latestJsonUrl = "https://raw.githubusercontent.com/riku1227/spoilerd/master/update_files/latest.json"
        suspend fun checkUpdate(context: Context) = GlobalScope.async {
            var updateData: Data? = null
            if(isNetworkConnect(context)) {
                val request = Request.Builder().url(latestJsonUrl).build()
                val result = OkHttpClient().newCall(request).execute()
                if(result.isSuccessful) {
                    val body = result.body?.string()
                    if(body != null) {
                        val jsonObject = JSONObject(body)
                        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        val appVersionCode = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            packageInfo.longVersionCode
                        } else {
                            packageInfo.versionCode.toLong()
                        }

                        val updateVersion = jsonObject.getString("version")
                        val updateVersionCode = jsonObject.getLong("version_code")
                        val updateFileUrl = jsonObject.getString("file_url")
                        val isUpdate = (updateVersionCode > appVersionCode)

                        updateData = Data(isUpdate, updateVersion, updateVersionCode, updateFileUrl, packageInfo.versionName, appVersionCode)
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${result.body?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${context.resources.getString(R.string.network_is_not_connected)}", Toast.LENGTH_LONG).show()
                }
            }

            return@async updateData
        }.await()

        suspend fun downloadUpdateFile(url: String, context: Context) = GlobalScope.async {
            val request = Request.Builder().url(url).build()
            val result = OkHttpClient().newCall(request).execute()
            if(result.isSuccessful) {
                val inputStream = result.body?.byteStream()
                val externalCacheDir = context.externalCacheDir
                if(inputStream != null && externalCacheDir != null) {
                    val outPutDir = File(externalCacheDir, "update_file")
                    if(!outPutDir.exists()) {
                        outPutDir.mkdirs()
                    }
                    val outPutFile = File(outPutDir, "test.apk")
                    val outPutStream = FileOutputStream(outPutFile)
                    inputStream.copyTo(outPutStream)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(FileProvider.getUriForFile(context, context.packageName, outPutFile), "application/vnd.android.package-archive")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(intent)
                }
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${context.resources.getString(R.string.network_is_not_connected)}", Toast.LENGTH_LONG).show()
                }
            }
            return@async
        }.await()
    }
}