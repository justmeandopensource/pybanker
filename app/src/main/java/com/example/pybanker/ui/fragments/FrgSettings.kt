package com.example.pybanker.ui.fragments


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.pybanker.model.DBHelper.Companion.DATABASE_NAME
import com.example.pybanker.R
import kotlinx.android.synthetic.main.frg_settings.*
import java.io.*
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgSettings : Fragment() {

    val externalStorageDir = Environment.getExternalStorageDirectory().toString() + "/pybanker/"
    val backupFile = externalStorageDir + "pybanker-dump.db"
    val importFile = externalStorageDir + "pybanker.db"
    var selectedFile: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frg_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingsOptions = listOf("Import Database from external storage",
            "Backup Database to external storage")

        settingsList.adapter = ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, settingsOptions)
        settingsList.setOnItemClickListener{
                _, _, position, _ ->
            if (settingsOptions[position] == "Import Database from external storage") {
                importDBFromExtStorage()
            } else if (settingsOptions[position] == "Backup Database to external storage") {
                backupDBToExtStorage()
            }
        }
    }

    fun importDBFromExtStorage() {
        try {
            val inputFile = File(importFile)
            val inputStream = FileInputStream(inputFile)
            val outputFile = File(context!!.getDatabasePath(DATABASE_NAME).canonicalPath)
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            Toast.makeText(context, "Database imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun backupDBToExtStorage() {
        if (File(backupFile).exists()) {
            File(backupFile).delete()
        }
        try {
            val outputFile = File(backupFile)
            val outputStream = FileOutputStream(outputFile)
            val inputFile = context!!.getDatabasePath(DATABASE_NAME).absoluteFile
            val inputStream = FileInputStream(inputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            Toast.makeText(context, "Database exported to /pybanker/pybanker-dump.db in external storage",
                Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            this.selectedFile = data?.data //The uri with the location of the file
        }
    }

}
