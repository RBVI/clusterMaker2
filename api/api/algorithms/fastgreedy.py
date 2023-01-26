"""
Fast Greedy cluster algorithm
"""

import falcon
import logging
import api.utils as utils
from api.jobs import Jobs
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

        try:
          # FastGreedy doesn't work for multigraphs
          graph = graph.simplify(multiple=True, loops=True, combine_edges=sum);
          part = graph.community_fastgreedy(weights="weights")
        except Exception as e:
          exc = utils.parse_igraph_exception(repr(e))
          status['status'] = 'error'
          status['message'] = exc
          return

        result['partitions'] = utils.get_vertex_list(graph, part.as_clustering())

        status['status'] = 'done'

