"""
MDS (Multidimensional scaling) dimensionality reduction algorithm
"""

import falcon
import api.utils as utils
from .base_algorithm import BaseAlgorithm
from sklearn.preprocessing import StandardScaler
from sklearn.manifold import LocallyLinearEmbedding
import pandas as pd

class LLE(BaseAlgorithm):

    def get_args(self, req: falcon.Request) -> dict:
        """ Get the arguments """

        # Get our parameters
        args = {}
        args['n_neighbors'] = utils.get_param_as_int(req, 'n_neighbors', 5)
        args['reg'] = utils.get_param_as_float(req, 'reg', 1e-3)
        args['eigen_solver'] = utils.get_param_as_string(req, 'eigen_solver', 'auto')
        args['tol'] = utils.get_param_as_float(req, 'tol', 1e-6)
        args['max_iter'] = utils.get_param_as_int(req, 'max_iter', 100)
        args['method'] = utils.get_param_as_string(req, 'method', 'standard')
        args['hessian_tol'] = utils.get_param_as_float(req, 'hessian_tol', 1e-4)
        args['modified_tol'] = utils.get_param_as_float(req, 'modified_tol', 1e-12)
        args['neighbors_algorithm'] = utils.get_param_as_string(req, 'neighbors_algorithm', 'auto')
        return args

    # This isn't really a community detection algorithm, but...
    def community_detection(self, args:dict, status:dict, result:dict):
        status['status'] = 'running'

        # Get our parameters
        n_neighbors = args['n_neighbors']
        reg = args['reg']
        eigen_solver = args['eigen_solver']
        tol = args['tol']
        max_iter = args['max_iter']
        method = args['method']
        hessian_tol = args['hessian_tol']
        modified_tol = args['modified_tol']
        neighbors_algorithm = args['neighbors_algorithm']

        data = args['json_data']

        df = utils.get_matrix(data)
        columns = df.columns.to_list()
        row_labels = df.index.to_list()

        # We need to drop any NaNs
        df = df.dropna()

        data = df[columns[1:]].values # skip over the label and just pull the data

        try:
          lle = LocallyLinearEmbedding(n_components=2, n_neighbors=n_neighbors,reg=reg,eigen_solver=eigen_solver,
                                       tol=tol,max_iter=max_iter, method=method, hessian_tol=hessian_tol, 
                                       modified_tol=modified_tol, neighbors_algorithm=neighbors_algorithm, n_jobs=10)
          embedding = lle.fit_transform(data)
        except Exception as e:
          exc = utils.parse_sklearn_exception(repr(e))
          status['status'] = 'error'
          status['message'] = exc
          return
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

