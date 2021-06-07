"""
Infomap cluster algorithm
"""
import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm

class Infomap(BaseAlgorithm):
    def get_args(self, req: falcon.Request) -> dict:
        args = {}
        args['trials'] = utils.get_param_as_int(req, 'trials', 10)
        return args

    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        trials = args['trials']
        data = args['json_data']

        graph = utils.get_graph(data)

        part = graph.community_infomap(edge_weights="weights", trials=trials)

        result['partitions'] = utils.get_vertex_list(graph, part)

        status['status'] = 'done'

