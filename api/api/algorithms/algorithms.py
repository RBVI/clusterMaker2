from api.jobs import Jobs
from .fastgreedy import FastGreedy
from .infomap import Infomap
from .leiden import Leiden
from .label_propagation import LabelPropagation
from .leading_eigenvector import LeadingEigenvector
from .multilevel import Multilevel
from .umap import UMAP
from .tsne import TSNE
from .isomap import IsoMapEmbedding
from .mds import MDS
from .spectral import SpectralEmbedding
from .lineardisc import LinearDiscriminant
import falcon
import json

class Algorithms(object):
    jobs = None
    algorithms = None

    def __init__(self, jobs: Jobs):
        self.jobs = jobs
        self.algorithms = {}

        # Initialize the list of algorithms
        self.algorithms["leiden"] = Leiden(jobs)
        self.algorithms["fastgreedy"] = FastGreedy(jobs)
        self.algorithms["infomap"] = Infomap(jobs)
        self.algorithms["labelpropagation"] = LabelPropagation(jobs)
        self.algorithms["leadingeigenvector"] = LeadingEigenvector(jobs)
        self.algorithms["multilevel"] = Multilevel(jobs)

        # Dimensionality reduction (manifold) techniques
        self.algorithms["umap"] = UMAP(jobs)
        self.algorithms["tsne"] = TSNE(jobs)
        self.algorithms["mds"] = MDS(jobs)
        self.algorithms["isomap"] = IsoMapEmbeddingjobs)
        self.algorithms["spectral"] = SpectralEmbedding(jobs)
        self.algorithms["lineardisc"] = LinearDiscriminant(jobs)

    def on_get(self, req: falcon.Request, resp: falcon.Response):
        resp.code = falcon.HTTP_200
        resp.text = '{"algorithms":'+json.dumps(list(self.algorithms.keys()))+'}'


    def get_algorithms(self) -> list:
        return self.algorithms.keys()

    def get_algorithm(self, algorithm:str):
        if algorithm in self.algorithms:
            return self.algorithms[algorithm]
        return None
