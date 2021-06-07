"""
Leiden cluster algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm

class Leiden(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['obj_func'] = utils.get_param_as_string(req, 'objective_function', 'modularity')
        args['resolution_parameter'] = utils.get_param_as_float(req, 'resolution', 1.0)
        args['beta'] = utils.get_param_as_float(req, 'beta', 0.01)
        args['iterations'] = utils.get_param_as_int(req, 'iterations', 2)
        return args

    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        obj_func = args['obj_func']
        resolution_parameter = args['resolution_parameter']
        beta = args['beta']
        iterations = args['iterations']
        data = args['json_data']

        graph = utils.get_graph(data)

        part = graph.community_leiden(objective_function=obj_func, weights="weights",
                                      resolution_parameter=resolution_parameter, beta=beta,
                                      n_iterations=iterations)

        result['partitions'] = utils.get_vertex_list(graph, part)

        status['status'] = 'done'

