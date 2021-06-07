import uuid

class Job:
    job_uuid = None
    algorithm = None

    def __init__(self, job_uuid, algorithm):
        self.job_uuid = job_uuid
        self.algorithm = algorithm

    def check_job(self):
        # Check on the process status
        pass
