package com.example.pybanker.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(val context: Context?) : SQLiteOpenHelper(context,
    DATABASE_NAME, null, 1) {

    override fun onOpen(db: SQLiteDatabase?) {
        db!!.disableWriteAheadLogging()
        super.onOpen(db)
    }

    override fun onCreate(db: SQLiteDatabase) {

        val accountsTblCreationQuery = "CREATE TABLE IF NOT EXISTS Accounts " +
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

    }

    companion object {
        const val DATABASE_NAME = "pybanker"
    }

    fun addAccount(name:String, balance:Float, excludetotal:String, type:String) {
        val db = this.writableDatabase
        val contentvalues = ContentValues()
        contentvalues.put("name",name)
        contentvalues.put("balance", balance)
        contentvalues.put("lastoperated", java.time.LocalDate.now().toString())
        contentvalues.put("excludetotal", excludetotal)
        contentvalues.put("type", type)
        db.insert("Accounts", null, contentvalues)
    }

    val getAccounts:Cursor
    get() {
        val db = this.writableDatabase
        return db.rawQuery("SELECT name, lastoperated, printf('%.2f', balance) as balance FROM Accounts", null)
    }

}