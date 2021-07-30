"""
MDS (Multidimensional scaling) dimensionality reduction algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.preprocessing import StandardScaler
from sklearn import manifold
import pandas as pd

class SpectralEmbedding(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['affinity'] = utils.get_param_as_string(req, 'affinity', 'nearest_neighbors')
        args['gamma'] = utils.get_param_as_float(req, 'gamma', None)
        args['eigen_solver'] = utils.get_param_as_string(req, 'eigen_solver', None)
        args['n_neighbors'] = utils.get_param_as_int(req, 'n_neighbors', None)
        return args

    # This isn't really a community detection algorithm, but...
    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        affinity = args['affinity']
        gamma = args['gamma']
        eigen_solver = args['eigen_solver']
        n_neighbors = args['n_neighbors']
        data = args['json_data']

        df = utils.get_matrix(data)
        columns = df.columns.to_list()
        row_labels = df.index.to_list()

        # We need to drop any NaNs
        df = df.dropna()

        data = df[columns[1:]].values # skip over the label and just pull the data

        spectral = manifold.SpectralEmbedding(n_components=2, affinity=affinity, gamma=gamma,
                                              eigen_solver=eigen_solver,n_neighbors=n_neighbors, n_jobs=10)
        embedding = spectral.fit_transform(data)
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

