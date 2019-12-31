from flask import current_app as app
import sqlite3
import os

#DATABASE = os.path.join(app.config['SQLITE_DB_DIR'], app.config['SQLITE_DB_FILE'])
DATABASE = 'data/db/pybanker.db'
# Get accounts for dashboard table


def getAccounts(account='all'):
    db = sqlite3.connect(DATABASE)
    cursor = db.cursor()
    appendquery = ""
    if account != "all":
        appendquery = "WHERE name = '%s'" % account
    query = """
        SELECT name, balance, lastoperated, type, excludetotal
        FROM accounts
        ORDER BY type
        %s
        """ % appendquery
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
