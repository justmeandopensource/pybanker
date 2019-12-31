from flask import Flask, render_template, redirect, url_for, request, flash
from helper_modules.miscHelper import dbFilePresent
from helper_modules.dbHelper import getAccounts
from helper_modules.reportHelper import (
    inexTrendAll, inexTrendYearlyAll, exTrendAll)
import os

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
