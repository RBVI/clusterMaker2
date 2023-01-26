"""
Leiden cluster algorithm
"""
import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm

class Multilevel(BaseAlgorithm):
    def get_args(self, req: falcon.Request) -> dict:
        return {}

    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        data = args['json_data']

        # Get our data file
        graph = utils.get_graph(data)

        try:
          part = graph.community_multilevel(weights="weights")
        except Exception as e:
          exc = utils.parse_igraph_exception(repr(e))
          status['status'] = 'error'
          status['message'] = exc
          return

        result['partitions'] = utils.get_vertex_list(graph, part)

        status['status'] = 'done'

