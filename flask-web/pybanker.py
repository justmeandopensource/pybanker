from flask import Flask, render_template, redirect, url_for, request, flash
from helper_modules.miscHelper import dbFilePresent
from helper_modules.dbHelper import (
    getAccounts, getTransactions, checkTotalAccounts, getCategories, addTransactionsDB,
    searchTransactions, getTransactionsForCategory, getAllCategoryStatsForMonth)
from helper_modules.reportHelper import (
    inexTrendAll, inexTrendYearlyAll, exTrendAll, categoryStats, categoryAllGraphDot)
import os
from datetime import date, datetime

# Initialize Flask Object
app = Flask(__name__)
app.secret_key = 'i234aessser54234lajdflkjasdlkjf;oiuqaewrlrl'

# Load config from file
app.config.from_object('config')

# Index Route
@app.route('/')
def index():
    if dbFilePresent():
        return redirect(url_for('dashboard'))
    else:
        category = ""
        if 'category' in request.args:
            category = request.args['category']
        return render_template('welcome.html', category=category)


# Dashboard Route
@app.route('/dashboard')
def dashboard():
    category = None
    if 'category' in request.args:
        category = request.args['category']
    accounts = getAccounts()
    inexAllGraph = inexTrendAll()
    inexYearlyAllGraph = inexTrendYearlyAll()
    exAllGraph = exTrendAll()
    return render_template('dashboard.html',
                           accounts=accounts,
                           inexAllGraph=inexAllGraph,
                           inexYearlyAllGraph=inexYearlyAllGraph,
                           exAllGraph=exAllGraph,
                           category=category)

# Import DB Route
# On successful import, redirect to dashboard
@app.route('/importdb', methods=['POST'])
def importdb():
    if request.method == 'POST':
        db_file = request.files['db_file']
        if db_file.filename == '':
            flash('No file selected. Have you gone nuts?')
            return redirect(url_for('index', category='alert alert-danger'))
        db_file.save(os.path.join(
            app.config['SQLITE_DB_DIR'], app.config['SQLITE_DB_FILE']))
        flash('Database successfully imported')
        return redirect(url_for('dashboard', category='alert alert-success'))

# Account Transactions Route
@app.route('/account/<accountname>/<period>', methods=['GET', 'POST'])
def account_transactions(accountname, period):
    accinfo = transactions = year = month = None
    curyear = datetime.now().year
    if accountname and period:
        if request.method == "POST":
            year = request.form['year']
            month = request.form['month']
        transactions = getTransactions(accountname, period, year, month)
        accinfo = getAccounts(account=accountname)
    return render_template('account-transactions.html', accinfo=accinfo, transactions=transactions, curyear=curyear)

# Add a new transaction Route
@app.route('/addtransaction', methods=['GET', 'POST'])
def addtransaction():
    if checkTotalAccounts() == 0:
        flash("Please add an account first before trying to add a transaction!!")
        return redirect(url_for('dashboard', category='alert alert-warning'))
    inc_categories, exp_categories = getCategories()
    categories = exp_categories + inc_categories
    accounts = getAccounts(status="Active")
    if request.method == "POST":
        account = request.form['account']
        category = request.form['category']
        amount = request.form['amount']
        date = request.form['date']
        notes = request.form['notes']
        flash(addTransactionsDB(date, notes, amount,
                                category, account))
    return render_template('addtransaction.html', categories=categories, accounts=accounts)

# Transfer funds Route
@app.route('/transferfunds', methods=['GET', 'POST'])
def transferfunds():
    if checkTotalAccounts() == 0:
        flash("Please add some accounts first before trying to transfer funds!!")
        return redirect(url_for('dashboard', category='alert alert-warning'))
    accounts = getAccounts(status="Active")
    if request.method == "POST":
        fromacc = request.form['fromaccount']
        toacc = request.form['toaccount']
        amount = request.form['amount']
        date = request.form['date']
        notes = request.form['notes']
        addTransactionsDB(date, notes, amount, "TRANSFER OUT", fromacc)
        addTransactionsDB(date, notes, amount, "TRANSFER IN", toacc)
        flash("Funds transferred from %s to %s successfully" % (fromacc, toacc))
    return render_template('transferfunds.html', accounts=accounts)

# Search Route
@app.route('/search', methods=['GET', 'POST'])
def search():
    searchresults = listresults = None
    curyear = datetime.now().year
    if request.method == "POST":
        if request.form['searchForm'] == "search":
            keyword = request.form['keyword']
            searchresults = searchTransactions(keyword)
        else:
            category = request.form['listcategory']
            period = request.form['period']
            year = request.form['year']
            month = request.form['month']
            if category == "Select":
                flash("Please choose a category")
            else:
                if "Select" in period and "Select" in year and "Select" in month:
                    listresults = getTransactionsForCategory(
                        category, None, None, None)
                elif not "Select" in period:
                    listresults = getTransactionsForCategory(
                        category, period, None, None)
                elif not "Select" in year and not "Select" in month:
                    listresults = getTransactionsForCategory(
                        category, None, year, month)
                else:
                    flash(
                        "Please choose period carefully. If you didn't select one of the predefined period, you have to select both year and month")
                if listresults is None:
                    flash("No transacations to list")
    categories = getCategories()
    return render_template('searchtransactions.html', searchresults=searchresults, listresults=listresults, categories=categories, curyear=curyear)

# Current vs Previous month expenses Route
@app.route('/curvsprevexpenses', methods=['GET'])
def curvsprevexpenses():
    if checkTotalAccounts() == 0:
        flash("No reports as you don't have any accounts setup. Please start adding your accounts")
        return redirect(url_for('dashboard', category='alert alert-warning'))
    prevmnthexpenses = getAllCategoryStatsForMonth(1)
    curmnthexpenses = getAllCategoryStatsForMonth(0)
    return render_template('curvsprevmonthexpenses.html',
                           prevmnthexpenses=prevmnthexpenses,
                           curmnthexpenses=curmnthexpenses)

# Category stats Route
@app.route('/categorystats', methods=['GET', 'POST'])
def categorystats():
    if checkTotalAccounts() == 0:
        flash("No reports as you don't have any accounts setup. Please start adding your accounts")
        return redirect(url_for('dashboard', category='alert alert-warning'))
    categoryStatsGraph = categoryStatsGraphYearly = None
    categoryStatsData = categoryStatsDataYearly = None
    categoryAllGraph = None
    if request.method == "POST":
        statcategory = request.form['statcategory']
        categoryStatsGraph, categoryStatsData = categoryStats(
            statcategory, "YEAR_MONTH")
        categoryStatsGraphYearly, categoryStatsDataYearly = categoryStats(
            statcategory, "YEAR")
        categoryAllGraph = categoryAllGraphDot(statcategory)
    categoriesRaw = getCategories()
    categories = ([x for x in categoriesRaw[0] if x != "TRANSFER IN"], [
                  x for x in categoriesRaw[1] if x != "TRANSFER OUT"])
    return render_template('categorystats.html',
                           categories=categories,
                           categoryStatsGraph=categoryStatsGraph,
                           categoryStatsData=categoryStatsData,
                           categoryStatsGraphYearly=categoryStatsGraphYearly,
                           categoryStatsDataYearly=categoryStatsDataYearly,
                           categoryAllGraph=categoryAllGraph)

# Under Construction Route
@app.route('/under_construction')
def under_construction():
    return render_template('under_construction.html')


# Main Function
if __name__ == '__main__':
    app.run(
        host=app.config['APP_IFADDR'],
        port=app.config['APP_PORT'],
        debug=app.config['APP_DEBUG'],
        threaded=app.config['APP_THREADED'])
