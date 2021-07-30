"""
MDS (Multidimensional scaling) dimensionality reduction algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.preprocessing import StandardScaler
from sklearn import manifold
import pandas as pd

class MDS(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['metric'] = utils.get_param_as_bool(req, 'metric', True)
        args['n_init'] = utils.get_param_as_int(req, 'n_init', 4)
        args['max_iter'] = utils.get_param_as_int(req, 'max_iter', 300)
        args['eps'] = utils.get_param_as_float(req, 'eps', 1e-3)
        args['dissimilarity'] = utils.get_param_as_string(req, 'dissimilarity', 'euclidean')
        return args

    # This isn't really a community detection algorithm, but...
    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        metric = args['metric']
        n_init = args['n_init']
        max_iter = args['max_iter']
        eps = args['eps']
        dissimilarity = args['dissimilarity']
        data = args['json_data']

        df = utils.get_matrix(data)
        columns = df.columns.to_list()
        row_labels = df.index.to_list()

        # We need to drop any NaNs
        df = df.dropna()

        data = df[columns[1:]].values # skip over the label and just pull the data

        mds = manifold.MDS(n_components=2, n_init=n_init, eps=eps, dissimilarity=dissimilarity,
                           metric=metric, max_iter=max_iter, n_jobs=10)
        embedding = mds.fit_transform(data)
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

