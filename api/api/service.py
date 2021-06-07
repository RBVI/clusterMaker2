"""
This is the interface for all cluster algorithms
"""

import abc
import falcon
from uuid import UUID

class Service(metaclass=abc.ABCMeta):
    """ Interface for all algorithms """
    @classmethod
    def __subclasshook__(cls, subclass):
        return (hasattr(subclass, 'on_get') and
                callable(subclass.on_get) and
                hasattr(subclass, 'get_status') and
                callable(subclass.get_status) and
                hasattr(subclass, 'fetch_results') and
                callable(subclass.fetch_results) and
                hasattr(subclass, 'terminate') and
                callable(subclass.terminate) and
                NotImplemented)

    @abc.abstractmethod
    def on_post(self, req: falcon.Request, resp: falcon.Response):
        """ Called from falcon """
        raise NotImplementedError

    @abc.abstractmethod
    def get_status(self, uid: UUID) -> str:
        """ Called to retrieve the status of the job """
        raise NotImplementedError

    @abc.abstractmethod
    def fetch_results(self, uid: UUID, req: falcon.Request, resp: falcon.Response):
        """ Called to fetch the results from the job.  Note that we are passed the
            Falcon Request and Response objects in order to construct the data """
        raise NotImplementedError

    @abc.abstractmethod
    def terminate(self, uid: UUID, req: falcon.Request, resp: falcon.Response):
        """ Called to kill the running algorithm """
        raise NotImplementedError
