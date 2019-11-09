package com.riku1227.spoilerd

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
            .setTitle(R.string.update_available)
            .setMessage("${resources.getString(R.string.update_version)}: ${arguments?.getString("updateVersion")}\n${resources.getString(R.string.current_version)}: ${arguments?.getString("currentVersion")}")
            .setPositiveButton(R.string.update_button) { _, _ ->
                GlobalScope.launch {
                    Update.downloadUpdateFile(arguments?.getString("updateFileUrl")!!, context!!)
                }
            }
            .setNeutralButton(R.string.view_changelog_button) { _, _ ->
                val uri = Uri.parse("https://raw.githubusercontent.com/riku1227/spoilerd/master/update_files/changelog.txt")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel_button) { _, _ -> }
            .create()
    }
}