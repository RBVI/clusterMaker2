"""
IsoMAP dimensionality reduction algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.preprocessing import StandardScaler
from sklearn.manifold import Isomap
import pandas as pd

class IsoMapEmbedding(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['n_neighbors'] = utils.get_param_as_int(req, 'n_neighbors', 5)
        args['eigen_solver'] = utils.get_param_as_string(req, 'eigen_solver', 'auto')
        args['metric'] = utils.get_param_as_string(req, 'metric', 'minkowski')
        args['tol'] = utils.get_param_as_float(req, 'tol', 0)
        args['path_method'] = utils.get_param_as_string(req, 'path_method', 'auto')
        args['neighbors_algorithm'] = utils.get_param_as_string(req, 'neighbors_algorithm', 'auto')
        args['max_iter'] = utils.get_param_as_int(req, 'max_iter', 0)
        return args

    # This isn't really a community detection algorithm, but...
    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        n_neighbors = args['n_neighbors']
        eigen_solver = args['eigen_solver']
        metric = args['metric']
        tol = args['tol']
        path_method = args['path_method']
        neighbors_algorithm = args['neighbors_algorithm']
        max_iter = args['max_iter']
        data = args['json_data']

        df = utils.get_matrix(data)
        columns = df.columns.to_list()
        row_labels = df.index.to_list()

        # We need to drop any NaNs
        df = df.dropna()

        data = df[columns[1:]].values # skip over the label and just pull the data

        if max_iter == 0:
          max_iter = None
        isomap = Isomap(n_neighbors=n_neighbors, n_components=2, eigen_solver=eigen_solver,
                        tol=tol, path_method=path_method, neighbors_algorithm=neighbors_algorithm,
                        metric=metric, max_iter=max_iter, n_jobs=10)
        embedding = isomap.fit_transform(data)
        #print(str(embedding))

        result_df = pd.DataFrame(embedding, columns=['x','y'])
        result_df.insert(0,"Node",row_labels)
        #print(str(result_df))
        # result_df[columns[0]] = [str(x) for x in row_labels]
        result_list = []
        result_list.append(['Node','x','y'])
        for row in result_df.reset_index().values.tolist():
          result_list.append(row[1:])

        result['embedding'] = result_list

        status['status'] = 'done'

