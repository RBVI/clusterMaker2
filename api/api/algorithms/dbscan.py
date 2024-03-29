"""
DBScan cluster algorithm
"""

import numpy as np
import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import DBSCAN
from scipy.sparse import csgraph

class DBScan(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['eps'] = utils.get_param_as_float(req, 'eps', 0.5)
        args['min_samples'] = utils.get_param_as_int(req, 'min_samples', 5)
        args['metric'] = utils.get_param_as_string(req, 'metric', 'euclidean')
        args['algorithm'] = utils.get_param_as_string(req, 'algorithm', 'auto')
        args['leaf_size'] = utils.get_param_as_int(req, 'leaf_size', 30)
        args['p'] = utils.get_param_as_float(req, 'p', 2)

        return args

    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        eps = args['eps']
        min_samples = args['min_samples']
        metric = args['metric']
        algorithm = args['algorithm']
        leaf_size = args['leaf_size']
        p = args['p']
        data = args['json_data']

        # We handle two different types of data -- either a square matrix with
        # weights in the cells or a matrix with columns representing features
        # If the metric is 'precomputed' then we're dealing with a square matrix
        # otherwise, we assume we have a standard column matrix
        if metric == 'precomputed':
          # Get the graph
          graph = utils.get_graph(data)
          # Get the matrix from the graph
          data = graph.get_adjacency_sparse(attribute="weights")
        else:
          df = utils.get_matrix(data)
          columns = df.columns.to_list()

          # We need to drop any NaNs
          df = df.dropna()

          data = df[columns[1:]].values # skip over the label and just pull the data

          if (args['scale']):
            data = StandardScaler().fit_transform(data)


        db = DBSCAN(eps=eps, min_samples=min_samples, metric=metric,metric_params=None,
                    algorithm=algorithm,leaf_size=leaf_size,p=p,n_jobs=None).fit(data) 

        core_samples_mask = np.zeros_like(db.labels_, dtype=bool)
        core_samples_mask[db.core_sample_indices_] = True
        # List of labels -- each element refers to a row
        labels = db.labels_

        result['partitions'] = labels

        status['status'] = 'done'

