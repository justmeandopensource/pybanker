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
    return data
    
