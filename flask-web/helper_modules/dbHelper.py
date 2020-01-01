from flask import current_app as app
import sqlite3
import os
from operator import itemgetter
import calendar


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
            advQuery = "AND opdate >= DATE('NOW', 'START OF MONTH')"
        elif 'lastmonth' in period:
            advQuery = "AND opdate BETWEEN DATE('NOW', 'START OF MONTH', '-1 MONTH') AND DATE('NOW', 'START OF MONTH')"
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
            advQuery = "AND opdate >= DATE('NOW', 'START OF MONTH')"
        elif 'lastmonth' in period:
            advQuery = "AND opdate BETWEEN DATE('NOW', 'START OF MONTH', '-1 MONTH') AND DATE('NOW', 'START OF MONTH')"
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

# Get category stats to fill previous and current month expenses in reports


def getAllCategoryStatsForMonth(month):
  # month: 0 - current, 1 - previous
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()

    advQuery = "1 = 1"
    if month == 0:
        advQuery = "opdate >= DATE('NOW', 'START OF MONTH')"
    else:
        advQuery = "opdate BETWEEN DATE('NOW', 'START OF MONTH', '-1 MONTH') AND DATE('NOW', 'START OF MONTH')"

    query = """
            SELECT category, SUM(debit) AS debit
            FROM transactions
            WHERE %s
                AND debit IS NOT NULL
                AND debit != "0.00"
                AND category NOT IN ('TRANSFER OUT')
            GROUP BY category
            ORDER BY debit DESC
            """ % advQuery
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get category stats for specific category


def getCategoryStats(category, period="YEAR_MONTH"):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    optype = "debit"
    opdateFormat = ""
    if getCategoryType(category) == "IN":
        optype = "credit"
    if period == 'YEAR_MONTH':
        opdateFormat = "STRFTIME('%Y%m', opdate)"
    else:
        opdateFormat = "STRFTIME('%Y', opdate)"

    query = """
            SELECT {0} AS period, SUM({1}) AS {1}
            FROM transactions
            WHERE category = '{2}'
                AND account NOT IN ({3})
                AND category NOT IN ('TRANSFER IN','TRANSFER OUT')
            GROUP BY period
            ORDER BY period
            """.format(opdateFormat, optype, category, getIgnoredAccounts())
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Do some maths to get more detailed category stats


def getDetailedCategoryStats(data, period="YEAR_MONTH"):
    if data is None:
        return None
    else:
        # Find total spent in this category since beginning
        totalSpent = sum(item[1] for item in data)
        totalSpent = "%.2f" % totalSpent
        periodAvg = float(totalSpent) / float(len(data))
        periodAvg = "%.2f" % periodAvg
        sortedData = sorted(data, key=itemgetter(1))
        if period == "YEAR_MONTH":
            lowestPeriod = "%s %s" % (
                calendar.month_name[int(sortedData[0][0]) % 100], str(sortedData[0][0])[:-2])
            highestPeriod = "%s %s" % (
                calendar.month_name[int(sortedData[-1][0]) % 100], str(sortedData[-1][0])[:-2])
        else:
            lowestPeriod = sortedData[0][0]
            highestPeriod = sortedData[-1][0]
        lowest = [lowestPeriod, "%.2f" % sortedData[0][1]]
        highest = [highestPeriod, "%.2f" % sortedData[-1][1]]
        categoryStatsData = [totalSpent, periodAvg, highest, lowest]
        return categoryStatsData

# Get category stats for each year in a separate list for all years
# It will be list of lists


def getCategoryStatsAllYears(category):
    years = getTransactionYearsCategory(category)
    data = []
    if years:
        for year in years:
            data.append(getCategoryStatsForYear(category, year[0]))
        return data
    else:
        return None

# Get category stats for specific category for specific user for specific year for dot graph


def getCategoryStatsForYear(category, year):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    optype = "debit"
    if getCategoryType(category) == "IN":
        optype = "credit"

    query = """
            SELECT SUM_DATA.year, COALESCE(SUM_DATA.{0}, 0.00) AS {0}
            FROM months
            LEFT JOIN (
                SELECT STRFTIME('%Y', opdate) AS year, STRFTIME('%m', opdate) AS month, SUM({0}) AS {0}
                FROM transactions
                WHERE STRFTIME('%Y', opdate) = '{1}'
                    AND category = '{2}'
                    AND category NOT IN ('TRANSFER IN','TRANSFER OUT')
                    AND account NOT IN ({3})
                GROUP BY STRFTIME('%m', opdate)
            ) SUM_DATA
            ON months.name = SUM_DATA.month
            ORDER BY months.name
            """.format(optype, year, category, getIgnoredAccounts())
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data

# Get distinct year where we have transactions for the give category


def getTransactionYearsCategory(category):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    query = """
            SELECT DISTINCT(STRFTIME('%Y', opdate))
            FROM transactions
            WHERE category = '{0}'
                AND STRFTIME('%Y', opdate) > 2013
            ORDER BY STRFTIME('%Y', opdate) DESC
            """.format(category)
    cursor.execute(query)
    data = cursor.fetchall()
    db.close()
    return data
