package com.riku1227.spoilerd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

        const val releaseSignature = "308202c5308201ada003020102020439943dcc300d06092a864886f70d01010b050030133111300f0603550403130872696b7531323237301e170d3139313130393134323632365a170d3434313130323134323632365a30133111300f0603550403130872696b753132323730820122300d06092a864886f70d01010105000382010f003082010a02820101008364451d5c78d2b38c60c58a75d4b0c1c31d5a7e5bb98712adada0891b5095b6439fb39f415233a890121666585f87cdd5bef6f6834909b90a2564e076bc4d798a5465db496868f9024ed44b83962eef27c725f022c009204e63ef82d01156db11c0d070cad446f388d5ad10d053fed7c0861c2a73574cbe68be653bf86955e3a1d017e5889d179ec2af16be3886186f6fd81524f72278bd82bf369e4a4d7ed20b972bea77dfd15dd51636b3b21f31c2d14928453dc68122d8c2c5d64acf3de61492caf8df94f7f6614e9f4bd7b8c4f13770567032d79ff345465d93ca5266fa6d30c760153c8aa4f1535ce9a25b638b48fd7f88d0b73f9739533eb6d64700d50203010001a321301f301d0603551d0e041604143a5b96202ab092426304167fe1bdcf95cdf26398300d06092a864886f70d01010b050003820101002e632f0e6443bef62e4ac32f1526b8e31626c8a11e92d005969f09581a8f2847127616d1797dfd35777fa90deba51f511d77ce91375c549bc30fa800af0d929ec0ad6ad6d7cef8fff7cd3e194591e38ed13efeda37737540ac60abdf75a76e581e0b69187895745299656b60e97b873377935e1e55d3b29202e67c19e32b40afc953b13106c8ece448ecc9b1fbf38be8a3ccb5b77882d24465f04ed15d051237909a41ef400318d948400e71982f779a44de2e9f473a6eba5ae31bb20fb6b91bfd98efef59bb39073ba3c7e17723faebfb69fb92553d3fcfb239d38fee69efceee566d52fb849ad3aea0fc1ab6657b5d3feabba2bcf463eb7e7e03a0bef97cc7"
        const val debugSignature = "308202e2308201ca020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3139303532323039333335385a170d3439303531343039333335385a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330820122300d06092a864886f70d01010105000382010f003082010a02820101008c89a90022b7241e8ce8c69be667b2efd4d82239cbfd7883cb3ae163277e9cf952fbc38376a2fc976116ef32e7d848be5dca200ebc2f8c72acdfd6777b358b5b8b63edf9fc59b8941b4b095242da89f97ce0ed37ecc4340ef195324c8e212ae5ce6e02ae8b230cc7b8f666e59a9d0ddf2d4f7ae9f1a3088edc5b227372c7af9e968e8da16348bc2407b32883c67c362a46605a589a8d863519173f0371f5ff68fa860e9c2918668ce40eec3ecbd8f36d3189f9e6419f1219334edcec4016106a27999c5fcd2b4aa920f0182e8e5283e4a3b1b11070fcb6899dee8e67c27e95eeac7d3c4a0a5ee903548b45ed7807ca12a4ea32fae09ce2bc6fa5780bc48c7aeb0203010001300d06092a864886f70d0101050500038201010064e51367c7e2b546c3c391210d3b8cea6ef152f0b029aff53da9eceea9ad8d89e1124ff3fea15c07a4b03d877cbd56f2c73abefa9c54d458a09b784ac1eaebf415685fe757774d3a6bb738c6380202b98d5c9d85d0482853355987fd60daeb704f72cde6e1a5a43cc01f292d949466dbe818e37fa1403287f5dd83f2d054ae8f340f63d8caf225bfc2498f9834bc99c1ab3c19fcdd42a2f6d928459c9e65394c87bf513b3f406a42e0daa23df9a73262ea2debb76bd18511f1c7a51090524b55b8c1cbf88710de7bcd2e73e684d20e22e379978a00446917b55ba0803e42529be851b947db6c324da9d02090808c31daaab041e87cfc77adccafccad93076fec"

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
                        val isUpdate = (updateVersionCode > appVersionCode)

                        val updateFileUrl = if(getSignature(context) == releaseSignature) {
                            jsonObject.getString("file_url_release")
                        } else {
                            jsonObject.getString("file_url_debug_key")
                        }

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

        fun getSignature(context: Context): String {
            val signatures = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.apkContentsSigners
            } else {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
            }

            return signatures[0].toCharsString()
        }
    }
}