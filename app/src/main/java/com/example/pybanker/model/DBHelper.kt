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
                "status TEXT NOT NULL," +
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
        contentvalues.put("name", name)
        contentvalues.put("balance", balance)
        contentvalues.put("lastoperated", java.time.LocalDate.now().toString())
        contentvalues.put("excludetotal", excludetotal)
        contentvalues.put("type", type)
        contentvalues.put("status", "Active")
        db.insert("Accounts", null, contentvalues)
    }

    val getAccounts:Cursor
    get() {
        val db = this.writableDatabase
        return db.rawQuery("SELECT name, lastoperated, printf('%.2f', balance) as balance, status, type " +
                                "FROM accounts ORDER BY status, type",
            null)
    }

    fun getAccountLast25(accountName: String?) : Cursor {
        val db = this.writableDatabase
        return db
            .rawQuery("SELECT opdate, description, category, printf('%.2f', credit) as credit, printf('%.2f',debit) as debit " +
                        "FROM transactions " +
                        "WHERE account = ? ORDER BY opdate DESC LIMIT 25",
                arrayOf(accountName))
    }

    fun addTransaction(opdate: String,
                       description: String,
                       category: String,
                       credit: Float,
                       debit: Float,
                       account: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("opdate", opdate)
        contentValues.put("description", description)
        contentValues.put("category", category)
        contentValues.put("credit", credit)
        contentValues.put("debit", debit)
        contentValues.put("account", account)
        db.insert("transactions", null, contentValues)
    }

    fun updateAccountBalance(account: String, amount: Float, trans_type: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        val oldbalance = getAccountBalance(account)
        val newbalance: Float

        newbalance = if (isCreditAccount(account)) {
                        if (trans_type == "IN") oldbalance - amount else oldbalance + amount
                     } else {
                        if (trans_type == "IN") oldbalance + amount else oldbalance - amount
                     }
        contentValues.put("balance", newbalance)

        db.update("accounts", contentValues, "name = ?", arrayOf(account))

    }

    fun getAccountBalance(account: String): Float {
        val db = this.writableDatabase
        val res = db.rawQuery("SELECT printf('%.2f', balance) as balance FROM accounts WHERE name = ?", arrayOf(account))
        res.moveToFirst()
        val balance = res.getString(0).toFloat()
        res.close()
        return balance
    }

    fun isCreditAccount(account: String) : Boolean {
        val db = this.writableDatabase
        val res = db.rawQuery("SELECT type FROM accounts WHERE name = ?", arrayOf(account))
        res.moveToFirst()
        val type = res.getString(0)
        res.close()
        return when (type) {
            "Credit Card" -> true
            else -> false
        }
    }

    fun getCategoryType(category: String): String {
        val db = this.writableDatabase
        val res = db.rawQuery("SELECT type FROM categories WHERE name = ?", arrayOf(category))
        res.moveToFirst()
        val type = res.getString(0)
        res.close()
        return type
    }

    fun listCategories(): ArrayList<String> {
        val categories = ArrayList<String>()
        val db = this.writableDatabase
        val res = db.rawQuery("SELECT name FROM categories ORDER BY type", null)
        while (res.moveToNext()) {
            categories.add(res.getString(0))
        }
        res.close()
        return categories
    }

    fun listActiveAccounts(): ArrayList<String> {
        val accounts = ArrayList<String>()
        val db = this.writableDatabase
        val res = db.rawQuery("SELECT name FROM accounts WHERE status != 'Closed' ORDER BY type", null)
        while (res.moveToNext()) {
            accounts.add(res.getString(0))
        }
        res.close()
        return accounts
    }

}