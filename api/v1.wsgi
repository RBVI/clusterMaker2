import site
import sys
from importlib import import_module
from multiprocessing import Manager

if __name__ == '__main__' or __name__.startswith('_mod_wsgi'):
    site.addsitedir('/usr/local/www/webservices/wsgi-scripts')
    print("v1.wsgi initializing", file=sys.stderr)
    manager = Manager()
    app = import_module("api.app")
    application = app.create_app(manager)
