from flask import current_app as app
import os.path


def dbFilePresent():
    return os.path.isfile("%s/%s" % (app.config['SQLITE_DB_DIR'], app.config['SQLITE_DB_FILE']))
