""" The Jobs module """
from multiprocessing import Manager
import os
import uuid
import logging

import falcon

from .service import Service

class Jobs:
    """ A class to keep track of all running jobs """
    active_jobs = {}
    manager = None

    def __init__(self, manager:Manager):
        self.active_jobs = {}
        self.manager = manager
        #logging.basicConfig(filename="/tmp/restLogger.log", level=logging.DEBUG)

    def create_job(self, url: str, service: Service) -> uuid.UUID:
        """ Create the job record for an Service instance.  This is typically
            called by the algorithm upon initiation to register it's instance """
        # Create the uuid
        job_uuid = uuid.uuid4()
        # Add it to the list
        self.active_jobs[job_uuid] = service
        #logging.info('Created job %s for service %s [%d]'%(str(job_uuid), service, os.getpid()))
        return job_uuid

    def remove_job(self, job_uuid: uuid.UUID):
        """ Remove the job from our list"""
        #logging.info('Removing job %s'%str(job_uuid))
        del self.active_jobs[job_uuid]

    def check_job(self, job_uuid: uuid.UUID) -> str:
        """ Check the status of a running job """
        if job_uuid in self.active_jobs:
            return self.active_jobs[job_uuid].get_status()
        return None

    def on_get(self, req: falcon.Request, resp: falcon.Response, job_id: str):
        """ Handles GET requests /status and /fetch """
        path = req.path
        #print('path: '+path)
        #print('job_id: '+job_id)
        #logging.info('path: %s, job_id: %s [%d]'%(path,job_id,os.getpid()))
        if path.startswith("/status/"):
            uid = get_job_id(job_id)
            if uid in self.active_jobs:
                resp.code = falcon.HTTP_200
                resp.text = str(self.active_jobs[uid].get_status(uid))
                return
            add_error(resp, "No such job")
            return

        if path.startswith("/fetch/"):
            uid = get_job_id(job_id)
            if uid in self.active_jobs:
                self.active_jobs[uid].fetch_results(uid, req, resp)
                return
            add_error(resp, "No such job")
            return

        if path.startswith("/terminate/"):
            uid = get_job_id(job_id)
            if uid in self.active_jobs:
                self.active_jobs[uid].terminate(uid, req, resp)
                return
            add_error(resp, "No such job")
            return

        if path.startswith("/jobs"):
            return

        #print('no matching path')

    def get_manager(self):
        return self.manager

def get_job_id(job_id: str) -> uuid.UUID:
    """ Get the job id from a URL """
    return uuid.UUID(job_id)

def add_error(resp: falcon.Response, error: str):
    """ Construct an error return """
    #logging.error('error: %s'%(error))
    resp.status = falcon.HTTP_500
    resp.text = '{"error": '+'"'+error+'"}'
