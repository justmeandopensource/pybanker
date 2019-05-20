package com.example.pybanker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(val context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        val accountsTblCreationQuery = "CREATE TABLE IF NOT EXISTS accounts " +
                "(" +
                "name TEXT NOT NULL," +
                "balance REAL NOT NULL," +
                "lastoperated TEXT NOT NULL," +
                "excludetotal TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "PRIMARY KEY(name)" +
                ")"

        db.execSQL(accountsTblCreationQuery)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS accounts")
    }

    companion object {
        const val DATABASE_NAME = "pybanker"
    }

    fun addAccount(name:String, balance:Float, excludetotal:String, type:String) {
        val db = this.writableDatabase
        val contentvalues = ContentValues()
        contentvalues.put("name",name)
        contentvalues.put("balance", balance)
        contentvalues.put("lastoperated", "2019-05-18")
        contentvalues.put("excludetotal", excludetotal)
        contentvalues.put("type", type)
        db.insert("accounts", null, contentvalues)
    }

    val getAccounts:Cursor
    get() {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM accounts", null)
    }

}