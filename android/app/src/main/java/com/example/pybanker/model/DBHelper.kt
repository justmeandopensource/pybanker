package com.example.pybanker.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


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

    fun getAccountLast100(accountName: String?) : Cursor {
        val db = this.writableDatabase
        return db
            .rawQuery("SELECT opdate, description, category, printf('%.2f', credit) as credit, printf('%.2f',debit) as debit " +
                        "FROM transactions " +
                        "WHERE account = ? ORDER BY opdate DESC LIMIT 100",
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
        if (category == "ALL EXPENSES" || category == "ALL INCOME") {
            return "doesn't matter"
        }
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

    fun getSearchResults(searchKeywords: String?, category: String?) : Cursor {
        val searchKeywordsList = searchKeywords?.split(" ")
        var query = "SELECT opdate, description, category, printf('%.2f', credit) as credit, " +
                "printf('%.2f', debit) as debit, account " +
                "FROM transactions WHERE "
        for (keyword in searchKeywordsList!!) {
            query += "description like '%$keyword%' AND "
        }

        if (!category.isNullOrEmpty()) {
            query += "category = '$category' AND "
        }

        query += "1 ORDER BY opdate DESC"
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

    fun getDistinctYear() : ArrayList<String> {
        val year = ArrayList<String>()
        val db = this.writableDatabase
        val res = db.rawQuery("SELECT DISTINCT strftime('%Y', opdate) AS year FROM transactions ORDER BY year DESC",
                            null)
        while (res.moveToNext()) {
            year.add(res.getString(0))
        }
        res.close()
        return year
    }

    fun listTransactionsByCategory(category: String, month: String?, year: String?): Cursor {

        val query: String

        when {
            (month.isNullOrEmpty() || year.isNullOrEmpty()) -> {
                query = "SELECT opdate, description, printf('%.2f', credit) as credit, " +
                        "printf('%.2f', debit) as debit, account " +
                        "FROM transactions " +
                        "WHERE category = ? " +
                        "ORDER BY opdate DESC " +
                        "LIMIT 50"
            } else -> {

                val monthshort = when (month) {
                    "January"   -> {
                        "01"
                    }
                    "February"  -> {
                        "02"
                    }
                    "March"     -> {
                        "03"
                    }
                    "April"     -> {
                        "04"
                    }
                    "May"       -> {
                        "05"
                    }
                    "June"      -> {
                        "06"
                    }
                    "July"      -> {
                        "07"
                    }
                    "August"    -> {
                        "08"
                    }
                    "September" -> {
                        "09"
                    }
                    "October"   -> {
                        "10"
                    }
                    "November"  -> {
                        "11"
                    }
                    else        -> {
                        "12"
                    }
                }
                query = "SELECT opdate, description, printf('%.2f', credit) as credit, " +
                        "printf('%.2f', debit) as debit, account " +
                        "FROM transactions " +
                        "WHERE category = ? AND " +
                        "strftime('%Y', opdate) = '$year' AND " +
                        "strftime('%m', opdate) = '$monthshort' " +
                        "ORDER BY opdate DESC"
                Log.d("PYBEDUGEEEE ====> ", query)
            }
        }

        val db = this.writableDatabase
        return db.rawQuery(query, arrayOf(category))
    }

    fun getCurrentMonthExpensesByCategory(): Cursor {
        val db = this.writableDatabase
        val query = "SELECT category, PRINTF('%.2f', debit) AS debit FROM (" +
                "SELECT category, SUM(debit) AS debit " +
                "FROM transactions t1 " +
                "INNER JOIN" +
                "(" +
                "SELECT * " +
                "FROM categories " +
                "WHERE type = 'EX' AND name NOT IN ('TRANSFER OUT')" +
                ") t2 " +
                "ON t1.category = t2.name " +
                "WHERE STRFTIME('%Y', opdate) = STRFTIME('%Y', 'now') and STRFTIME('%m', opdate) = STRFTIME('%m', 'now') " +
                "GROUP BY category ORDER BY debit DESC" +
                ")"
        return db.rawQuery(query, null)
    }

    fun transactionsTableExists(): Boolean {
        val db = this.writableDatabase
        try {
            val res = db.rawQuery("SELECT COUNT(*) FROM transactions", null)
            res.close()
        } catch (e: SQLiteException) {
            return false
        }
        return true
    }

    fun getCurrentMonthExpense(): String {
        val db = this.writableDatabase
        val query = "SELECT PRINTF('%.2f', SUM(debit)) " +
                    "FROM transactions " +
                    "WHERE category NOT IN ('TRANSFER OUT') " +
                    "AND STRFTIME('%Y', opdate) = STRFTIME('%Y', 'now') " +
                    "AND STRFTIME('%m', opdate) = STRFTIME('%m', 'now')"
        val res = db.rawQuery(query, null)
        return if (res.count > 0) {
            res.moveToFirst()
            val result = res.getString(0)
            res.close()
            result
        } else {
            ""
        }
    }

    fun getInExMonthlyTrend(): Cursor {
        val db = this.writableDatabase
        val query = "SELECT STRFTIME('%m-%Y', opdate) AS period, " +
                    "CAST(SUM(credit) AS INT) AS income, " +
                    "CAST(SUM(debit) AS INT) AS expense " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM accounts WHERE excludetotal = 'no') t2 " +
                    "ON t1.account = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER IN', 'TRANSFER OUT') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
        return db.rawQuery(query, null)
    }

    fun getInExYearlyTrend(): Cursor {
        val db = this.writableDatabase
        val query = "SELECT STRFTIME('%Y', opdate) AS period, " +
                "CAST(SUM(credit) AS INT) AS income, " +
                "CAST(SUM(debit) AS INT) AS expense " +
                "FROM transactions t1 " +
                "INNER JOIN (SELECT name FROM accounts WHERE excludetotal = 'no') t2 " +
                "ON t1.account = t2.name " +
                "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER IN', 'TRANSFER OUT') " +
                "GROUP BY period " +
                "ORDER BY STRFTIME('%Y', opdate)"
        return db.rawQuery(query, null)
    }

    fun getCatStatsMonthly(category: String): Cursor {
        val amountType = when (getCategoryType(category)) {
            "EX"    -> "debit"
            "IN"    -> "credit"
            else    -> null
        }
        val query: String
        when (category) {
            "ALL EXPENSES" -> query = "SELECT STRFTIME('%m-%Y', opdate) AS period, " +
                    "CAST(SUM(debit) AS INT) AS debit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'EX') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER OUT') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
            "ALL INCOME" -> query = "SELECT STRFTIME('%m-%Y', opdate) AS period, " +
                    "CAST(SUM(credit) AS INT) AS credit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'IN') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER IN') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
            else -> query = "SELECT STRFTIME('%m-%Y', opdate) AS period, " +
                    "CAST(SUM($amountType) AS INT) AS amount " +
                    "FROM transactions " +
                    "WHERE category = '$category' " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
        }
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

    fun getCatsStatsYearly(category: String): Cursor {
        val amountType = when (getCategoryType(category)) {
            "EX"    -> "debit"
            "IN"    -> "credit"
            else    -> null
        }
        val query: String
        when (category) {
            "ALL EXPENSES" -> query = "SELECT STRFTIME('%Y', opdate) AS period, " +
                    "CAST(SUM(debit) AS INT) AS debit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'EX') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER OUT') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate)"
            "ALL INCOME" -> query = "SELECT STRFTIME('%Y', opdate) AS period, " +
                    "CAST(SUM(credit) AS INT) AS credit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'IN') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER IN') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate)"
            else -> query = "SELECT STRFTIME('%Y', opdate) AS period, " +
                    "CAST(SUM($amountType) AS INT) AS amount " +
                    "FROM transactions " +
                    "WHERE category = '$category' " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate)"
        }
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

    fun getCatsStatsLast12(category: String): Cursor {
        val amountType = when (getCategoryType(category)) {
            "EX"    -> "debit"
            "IN"    -> "credit"
            else    -> null
        }
        val query: String
        when (category) {
            "ALL EXPENSES" -> query = "SELECT STRFTIME('%Y-%m', opdate) AS period, " +
                    "CAST(SUM(debit) AS INT) AS debit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'EX') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER OUT') " +
                    "AND opdate BETWEEN DATETIME('now', '-12 months', 'start of month') " +
                    "AND DATETIME('now', 'localtime') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
            "ALL INCOME" -> query = "SELECT STRFTIME('%Y-%m', opdate) AS period, " +
                    "CAST(SUM(credit) AS INT) AS credit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'IN') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER IN') " +
                    "AND opdate BETWEEN DATETIME('now', '-12 months', 'start of month') " +
                    "AND DATETIME('now', 'localtime') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
            else -> query = "SELECT STRFTIME('%Y-%m', opdate) AS period, " +
                    "CAST(SUM($amountType) AS INT) AS amount " +
                    "FROM transactions " +
                    "WHERE category = '$category' " +
                    "AND opdate BETWEEN DATETIME('now', '-12 months', 'start of month') " +
                    "AND DATETIME('now', 'localtime') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
        }
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

    fun getLast12MonthExpenses(): Cursor {
        val query = "SELECT STRFTIME('%Y-%m', opdate) AS period, " +
                    "CAST(SUM(debit) AS INT) AS debit " +
                    "FROM transactions t1 " +
                    "INNER JOIN (SELECT name FROM categories WHERE type = 'EX') t2 " +
                    "ON t1.category = t2.name " +
                    "WHERE category NOT IN ('OPENING BALANCE', 'TRANSFER OUT') " +
                        "AND opdate BETWEEN DATETIME('now', '-12 months', 'start of month') " +
                        "AND DATETIME('now', 'localtime') " +
                    "GROUP BY period " +
                    "ORDER BY STRFTIME('%Y', opdate), STRFTIME('%m', opdate)"
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

}