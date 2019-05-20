package com.example.pybanker


import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.pybanker.DBHelper.Companion.DATABASE_NAME
import kotlinx.android.synthetic.main.frg_settings.*
import java.io.*
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgSettings : Fragment() {

    val externalStorageDir = Environment.getExternalStorageDirectory().toString() + "/pybanker/"
    val backupFile = externalStorageDir + "pybanker.db"

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
            val inputFile = File(backupFile)
            val inputStream = FileInputStream(inputFile)
            val outputFile = File(context!!.getDatabasePath(DATABASE_NAME).canonicalPath)
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            Toast.makeText(context, "Database imported", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun backupDBToExtStorage() {
        if (File(backupFile).exists()) {
            File(backupFile).delete()
        }
        try {

            val sd = File(Environment.getExternalStorageDirectory().absolutePath)
            val data = File(Environment.getDataDirectory().absolutePath)

            if (sd.canWrite()) {
                val currentDBPath = "//data//com.example.pybanker//databases//pybanker"
                val backupDBPath = "pybanker.db"
                val currentDB = File(data, currentDBPath)
                val backupDB = File(sd, backupDBPath)

                if (currentDB.exists()) {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                }
                Toast.makeText(context, "Database exported", Toast.LENGTH_SHORT).show()
            }
            /*

            val outputFile = File(backupFile)
            val outputStream = FileOutputStream(outputFile)
            val inputFile = File(context!!.getDatabasePath(DATABASE_NAME).canonicalPath)
            val inputStream = FileInputStream(inputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.flush()
            outputStream.close() */

        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

}
