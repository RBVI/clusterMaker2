"""
Various utility functions
"""

import igraph as ig
import pandas as pd
import numpy as np
import json


def get_param_as_string(req, param, default):
    if req.has_param(param):
        return str(req.get_param(param))
    else:
        return default

def get_param_as_float(req, param, default):
    if req.has_param(param):
        return req.get_param_as_float(param)
    else:
        return default

def get_param_as_int(req, param, default):
    if req.has_param(param):
        return req.get_param_as_int(param)
    else:
        return default

def get_param_as_bool(req, param, default):
    if req.has_param(param):
        return req.get_param_as_bool(param)

def get_matrix(json_data: str) -> pd.DataFrame:
    """ Convert a json string to a pandas dataframe.
        JSON format:
          { 
            columns: [label, col1, col2, col3],
            data: [[lbl, val1, val2, val3],[lbl, val1, val2, val3],...]
          }
    """

    # Get the columns array
    columns = json_data['columns']
    rows = json_data['data']

    # Create the pandas dataframe
    df = pd.DataFrame(data=rows, columns=columns)
    df.set_index(columns[0], inplace=True)
    df = df.astype(np.float)
    return df

def get_graph(json_data: str) -> ig.Graph:
    """ Convert a json string to a dictionary of
        vertices and edges """
    vertices = json_data['nodes']
    edges = json_data['edges']
    ncol = []
    for edge in edges:
        ncol.append((edge[0],edge[1],float(edge[2])))

    g = ig.Graph.TupleList(ncol,edge_attrs="weights")
    return g

def get_json_graph(data: str) -> ig.Graph:
    """ Convert a json data stream to a dictionary of
        vertices and edges """
    json_data = json.load(data)
    return get_graph(json_data)

def get_json_result(status: dict, result: dict) -> str:
    json_data = {}
    for key, value in status.items():
        json_data[key] = value
    for key, value in result.items():
        json_data[key] = value

    return json.dumps(json_data)

def get_vertex_list(graph: ig.Graph, vertices: list) -> list:
    """ Take a list (or list of lists) of vertex indices and return a
        list (or list of lists) of vertex names """
    result = []
    for vals in vertices:
        if isinstance(vals, list):
            result.append(get_vertex_list(graph, vals))
        elif isinstance(vals, str):
            result.append(vals)
        elif isinstance(vals, int):
            result.append(graph.vs[vals]['name'])
    return result
