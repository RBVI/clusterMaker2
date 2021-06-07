"""
Fast Greedy cluster algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm

class FastGreedy(BaseAlgorithm):
    def get_args(self, req: falcon.Request) -> dict:
        return {}

    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        data = args['json_data']

        # Get our data file
        graph = utils.get_graph(data)
        part = graph.community_fastgreedy(weights="weights")

        result['partitions'] = utils.get_vertex_list(graph, part.as_clustering())

        status['status'] = 'done'

