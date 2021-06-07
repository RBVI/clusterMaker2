import falcon
from falcon_multipart.middleware import MultipartMiddleware
from multiprocessing import Manager
from .algorithms.algorithms import Algorithms
from .jobs import Jobs

import logging
import sys

manager = None

def create_app(mgr: Manager):
    #logging.basicConfig(filename="/var/tmp/webservices_api.log",level=logging.INFO)
    #logging.info("Service initialized")
    print("Service initialized", file=sys.stderr)

    manager = mgr
    app = application = falcon.App(middleware=[MultipartMiddleware()])

    # Provided for backwards compatibility
    #scNetViz = ScNetVizHandler(mgr)
    #api.add_route('/scnetviz/api/v1/umap', scNetViz)
    #api.add_route('/scnetviz/api/v1/tsne', scNetViz)
    #api.add_route('/scnetviz/api/v1/drawgraph', scNetViz)
    #api.add_route('/scnetviz/api/v1/louvain', scNetViz)
    #api.add_route('/scnetviz/api/v1/leiden', scNetViz)

    # New API
    jobs = Jobs(mgr)
    app.add_route('/status/{job_id}', jobs)
    app.add_route('/fetch/{job_id}', jobs)
    app.add_route('/terminate/{job_id}', jobs)
    algorithms = Algorithms(jobs)
    app.add_route('/services', algorithms)
    for algorithm in algorithms.get_algorithms():
        app.add_route('/service/'+algorithm, algorithms.get_algorithm(algorithm))

    # ChimeraX web services
    from . import cxservices
    cxservices.add_routes(app)

    return application

