"""
Algorithm base class
"""

from uuid import UUID
import json

import falcon
from multiprocessing import Manager,Process

from api.jobs import Jobs
import api.utils as utils

class BaseAlgorithm():
    jobs = None
    myjobs = {}

    def __init__(self, jobs: Jobs):
        self.jobs = jobs
        self.manager = jobs.get_manager()

    def on_post(self, req: falcon.Request, resp: falcon.Response):
        """ Get data and arguments """
        result = self.manager.dict()
        status = self.manager.dict()

        # Get our parameters
        args = self.get_args(req)

        # We need to do the load here because we can't pass a stream to our
        # child process
        if hasattr(req.get_param('data'),'file'):
            # Python requests adds an extra dict
            args['json_data'] = json.load(req.get_param('data').file)
        else:
            # Assume it's just straight text
            args['json_data'] = json.loads(req.get_param('data'))

        f = open("/var/tmp/wsgi.log", "w")
        f.write(str(args['json_data']))

        uuid = self.jobs.create_job(req.path, self)
        proc = Process(target=self.community_detection, args=(args, status, result))
        self.myjobs[uuid] = (proc, status, result)
        proc.start()
        resp.code = falcon.HTTP_200
        resp.text = json.dumps({'job_id': str(uuid)})

    def get_status(self, uid: UUID) -> str:
        if uid in self.myjobs:
            (proc, status, result) = self.myjobs[uid]
            return status['status']
        return None

    def fetch_results(self, uid: UUID, req: falcon.Request, resp: falcon.Response):
        if uid in self.myjobs:
            (proc, status, result) = self.myjobs[uid]
            # Add our response
            resp.text = utils.get_json_result(status, result)
            resp.code = falcon.HTTP_200
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

