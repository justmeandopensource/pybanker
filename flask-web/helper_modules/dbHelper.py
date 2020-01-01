from flask import current_app as app
import sqlite3
import os

#DATABASE = os.path.join(app.config['SQLITE_DB_DIR'], app.config['SQLITE_DB_FILE'])
DATABASE = 'data/db/pybanker.db'
# Get accounts for dashboard table


def getAccounts(account='all', status='all'):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    appendquery = statusquery = ""
    if account != "all":
        appendquery = "AND name = '%s'" % account
    if status != "all":
        statusquery = "AND status = '%s'" % status
    query = """
        SELECT name, balance, lastoperated, type, excludetotal, status
        FROM accounts
        WHERE 1 = 1 %s %s
        ORDER BY type
        """ % (appendquery, statusquery)
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get income/expense monthly/or since beginning for a user


def getInEx(year, period="selective"):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    if period == "selective":
        # TODO query not tested
        query = """
            SELECT name, COALESCE(SUM_DATA.credit, 0.00) AS credit, COALESCE(SUM_DATA.debit, 0.00) AS debit
            FROM months
            LEFT JOIN (
                SELECT MONTH(opdate) AS mnth, SUM(credit) AS credit, SUM(debit) AS debit
                FROM transactions
                WHERE YEAR(opdate) = %s
                    AND account NOT IN (%s)
                    AND category NOT IN ('OPENING BALANCE','TRANSFER IN','TRANSFER OUT')
                GROUP BY MONTH(opdate)
            ) SUM_DATA
            ON months.name = SUM_DATA.mnth
            ORDER BY months.name
            """ % (year, getIgnoredAccounts())
    else:
        query = """
            SELECT STRFTIME('%Y%m', opdate) AS period, printf('%0.2f',SUM(credit)) AS credit, printf('%0.2f', SUM(debit)) AS debit
            FROM transactions
            WHERE account NOT IN ({0})
                AND category NOT IN ('OPENING BALANCE','TRANSFER IN','TRANSFER OUT')
            GROUP BY period
            ORDER BY period
            """.format(getIgnoredAccounts())

    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get accounts that are excluded


def getIgnoredAccounts():
    ignoreAccounts = []
    accounts = getAccounts()
    for account in accounts:
        if account[4] == 'yes':
            ignoreAccounts.append('"%s"' % account[0])
    return ",".join(ignoreAccounts)

# Get income/expense yearly since beginning for a user


def getInExYearly():
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    query = """
            SELECT STRFTIME('%Y', opdate) AS year, SUM(credit), SUM(debit)
            FROM transactions
            WHERE year > 2013
                AND account NOT IN ({0})
                AND category NOT IN ('OPENING BALANCE','TRANSFER IN','TRANSFER OUT')
            GROUP BY year
            ORDER BY year
            """.format(getIgnoredAccounts())
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get expense monthly since beginning


def getEx():
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    query = """
            SELECT STRFTIME('%Y', opdate) AS year, SUM(debit)
            FROM transactions
            WHERE year > 2013
                AND account NOT IN ({0})
                AND category NOT IN ('TRANSFER IN','TRANSFER OUT')
            GROUP BY STRFTIME('%Y%m', opdate)
            ORDER BY STRFTIME('%Y%m', opdate)
            """.format(getIgnoredAccounts())
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get account transactions


def getTransactions(accountname, period, year, month):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    advQuery = limitQuery = ''

    if 'normal' in period:
        limitQuery = 'LIMIT 20'

    if 'PRE_' in period:
        if 'thisweek' in period:
            advQuery = "AND STRFTIME('%Y%W', opdate) = STRFTIME('%Y%W', DATE('NOW'))"
        elif 'thismonth' in period:
            advQuery = "AND opdate BETWEEN DATE('NOW', 'START OF MONTH') AND DATE('NOW')"
        elif 'lastmonth' in period:
            advQuery = "AND opdate BETWEEN DATE('NOW', 'START OF MONTH', '-1 MONTH') AND DATE('NOW')"
        elif 'last5days' in period:
            advQuery = "AND opdate >= DATE('NOW', '-5 DAYS')"
        elif 'last30days' in period:
            advQuery = "AND opdate >= DATE('NOW', '-30 DAYS')"
    elif 'selective' in period:
        advQuery = "AND STRFTIME('%Y', opdate) = '{0}' AND STRFTIME('%m', opdate) = '{1}'".format(
            year, month)

    query = "SELECT opdate, description, credit, debit, category \
            FROM transactions \
            WHERE account = '%s' %s \
            ORDER BY opdate DESC %s" \
            % (accountname, advQuery, limitQuery)

    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Check number of accounts


def checkTotalAccounts():
    accountsTotal = 0
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    query = "SELECT COUNT(*) FROM accounts"
    cursor.execute(query)
    accountsTotal = cursor.fetchone()[0]
    db.close()
    return accountsTotal

# Get list of categories


def getCategories():
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    inc_categories = []
    exp_categories = []

    cursor.execute('SELECT name FROM categories WHERE type="IN"')
    for item in cursor.fetchall():
        inc_categories.append(item[0])
    cursor.execute('SELECT name FROM categories WHERE type="EX"')
    for item in cursor.fetchall():
        exp_categories.append(item[0])

    db.close()
    return inc_categories, exp_categories


# Add transaction
def addTransactionsDB(date, notes, amount, category, account):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    credit, debit, updatetype = ["NULL", amount, "debit"]
    if getCategoryType(category) == "IN":
        credit, debit, updatetype = [amount, "NULL", "credit"]

    query = """
        INSERT INTO transactions 
        VALUES('%s', '%s', '%s', %s, %s, '%s')""" % (date, notes, category, credit, debit, account)
    cursor.execute(query)
    data = cursor.fetchall()
    db.commit()
    db.close()
    if len(data) == 0:
        if updateAccounts(account, amount, updatetype):
            returnString = "Transaction added successfully"
        else:
            returnString = "Failed to update accounts table. But transaction recorded"
    else:
        returnString = str(data[0])

    return returnString

# Update Balance in Accounts table


def updateAccounts(name, amount, updatetype):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    sign, operator = ["+", "-"]
    isassetAcc = checkAccountType(name)
    if not isassetAcc:
        sign = "-"
    if updatetype == "credit":
        operator = "+"

    query = """UPDATE accounts
            SET balance = balance %s %s%s, lastoperated = DATE('NOW')
            WHERE name = '%s'""" % (operator, sign, amount, name)
    cursor.execute(query)
    db.commit()
    db.close()
    return True

# Get account Type


def checkAccountType(account):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    isassetAcc = True

    query = "SELECT type FROM accounts WHERE name = '%s'" % account
    cursor.execute(query)
    data = cursor.fetchone()

    db.close()

    if data[0] == "Credit Card":
        isassetAcc = False
    return isassetAcc

# Check category type


def getCategoryType(category):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    query = "SELECT type FROM categories WHERE name = '%s'" % category
    cursor.execute(query)
    data = cursor.fetchone()
    db.close()
    return data[0]

# Get transactions for keyword search


def searchTransactions(keyword):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    query = """
            SELECT opdate, description, credit, debit, category, account \
            FROM transactions \
            WHERE description like '%%%s%%' \
            ORDER BY opdate DESC
            """ % keyword
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get account transactions for a category


def getTransactionsForCategory(category, period=None, year=None, month=None):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    advQuery = limitQuery = ''

    if period:
        if 'thisweek' in period:
            advQuery = "AND STRFTIME('%Y%W', opdate) = STRFTIME('%Y%W', DATE('NOW'))"
        elif 'thismonth' in period:
            advQuery = "AND opdate BETWEEN DATE('NOW', 'START OF MONTH') AND DATE('NOW')"
        elif 'lastmonth' in period:
            advQuery = "AND opdate BETWEEN DATE('NOW', 'START OF MONTH', '-1 MONTH') AND DATE('NOW')"
        elif 'last5days' in period:
            advQuery = "AND opdate >= DATE('NOW', '-5 DAYS')"
        elif 'last30days' in period:
            advQuery = "AND opdate >= DATE('NOW', '-30 DAYS')"
    else:
        if year and month:
            advQuery = "AND STRFTIME('%Y', opdate) = '{0}' AND STRFTIME('%m', opdate) = '{1}'".format(
                year, month)
        else:
            limitQuery = "LIMIT 20"

    query = """
            SELECT opdate, description, credit, debit, account
            FROM transactions
            WHERE category = '%s' %s
            ORDER BY opdate DESC %s
            """ % (category, advQuery, limitQuery)
    print(query)
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    if len(data) == 0:
        data = None
    return data
