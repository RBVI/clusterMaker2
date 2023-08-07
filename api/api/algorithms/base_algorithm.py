"""
Algorithm base class
"""

from uuid import UUID
import json

import falcon
import logging

from multiprocessing import Manager,Process
from datetime import datetime

from api.jobs import Jobs
import api.utils as utils

class BaseAlgorithm():
    jobs = None
    myjobs = {}
    logFile = None

    def __init__(self, jobs: Jobs):
        self.jobs = jobs
        self.manager = jobs.get_manager()

    def on_post(self, req: falcon.Request, resp: falcon.Response):
        """ Get data and arguments """
        result = self.manager.dict()
        status = self.manager.dict()

        #self.logFile.write(repr(req)+"\n")
        self.log(repr(req))

        # Get our parameters
        args = self.get_args(req)
        self.log("Arguments: "+repr(args))
        #self.logFile.write(repr(args))

        # We need to do the load here because we can't pass a stream to our
        # child process
        if hasattr(req.get_param('data'),'file'):
            # Python requests adds an extra dict
            args['json_data'] = json.load(req.get_param('data').file)
        else:
            # Assume it's just straight text
            args['json_data'] = json.loads(req.get_param('data'))

        #f = open("/var/tmp/wsgi.log", "w")
        #f.write(str(args['json_data']))

        uuid = self.jobs.create_job(req.path, self)
        proc = Process(target=self.community_detection, args=(args, status, result))
        self.myjobs[uuid] = (proc, status, result)
        proc.start()
        resp.code = falcon.HTTP_200
        resp.text = json.dumps({'job_id': str(uuid)})

    def get_status2(self, uid: UUID) -> dict:
        if uid in self.myjobs:
            (proc, status, result) = self.myjobs[uid]
            json_status = {}
            for key, value in status.items():
                json_status[key] = value

            return json_status
        return None

    def get_status(self, uid: UUID) -> str:
        if uid in self.myjobs:
            (proc, status, result) = self.myjobs[uid]
            #self.logFile.write("Getting status for "+repr(uid)+"\n")
            #self.logFile.write("Status = "+repr(status['status'])+"\n")
            self.log("Status for "+repr(uid)+" = "+repr(status['status']))
            return status['status']
        return None

    def fetch_results(self, uid: UUID, req: falcon.Request, resp: falcon.Response):
        if uid in self.myjobs:
            (proc, status, result) = self.myjobs[uid]
            # Add our response
            resp.text = utils.get_json_result(status, result)
            if status['status'] == 'done':
              resp.code = falcon.HTTP_200
            elif status['status'] == 'error':
              resp.code = falcon.HTTP_500
              self.log("Returning 500. Response = "+repr(resp.text))
        else:
            resp.code = falcon.HTTP_400
            resp.text = str({'error':"no such job: "+str(uid)})

    def terminate(self, uid: UUID, req: falcon.Request, resp: falcon.Response):
        # Terminate the running process
        if uid in self.myjobs:
            (proc, status, result) = self.myjobs[uid]
            proc.terminate()
            self.jobs.remove_job(uid)
            del self.myjobs[uid]
            resp.text = "Job: "+str(uid)+" terminated"
            resp.code = falcon.HTTP_200
        else:
            resp.code = falcon.HTTP_400
            resp.text = str({'error':"no such job: "+str(uid)})

    def log(self, message: str):
        now = datetime.today()
        logging.info(str(now)+": "+message)


