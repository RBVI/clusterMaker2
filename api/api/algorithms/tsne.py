"""
tSNE dimensionality reduction algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.manifold import TSNE
import pandas as pd

class TSNE(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['perplexity'] = utils.get_param_as_float(req, 'perplexity', 30.0)
        args['early_exaggeration'] = utils.get_param_as_float(req, 'early_exaggeration', 12.0)
        args['metric'] = utils.get_param_as_string(req, 'metric', 'euclidean')
        args['learning_rate'] = utils.get_param_as_float(req, 'learning_rate', '200.0')
        args['n_iter'] = utils.get_param_as_int(req, 'n_iter', 1000)
        args['init'] = utils.get_param_as_string(req, 'init', 'pca')
        return args

    # This isn't really a community detection algorithm, but...
    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        perplexity = args['perplexity']
        early_ex = args['early_exaggeration']
        learning_rate = args['learning_rate']
        n_iter = args['n_iter']
        metric = args['metric']
        init = args['init']
        data = args['json_data']

        df = utils.get_matrix(data)
        columns = df.columns.to_list()
        row_labels = df.index.to_list()

        # We need to drop any NaNs
        df = df.dropna()

        data = df[columns[1:]].values # skip over the label and just pull the data
        tsne = TSNE(n_components=2, perplexity=perplexity,early_exggageration=early_ex, learning_rage=learning_rate,
                    metric=metric, init=init, n_jobs=10)
        embedding = tsne.fit_transform(data)

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

