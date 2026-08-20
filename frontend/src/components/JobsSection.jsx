import React from 'react';
import ProgressBar from './ProgressBar';

export default function JobsSection({ activeJob, chunkStats }) {
  return (
    <div className="jobs-section">
      <div className="jobs-header">
        <span> Active Job</span>
        {activeJob && <span className="job-id mono">{activeJob.jobId?.slice(0, 20)}</span>}
      </div>
      <div className="jobs-body">
        {!activeJob ? (
          <div className="no-job">
            <div className="no-job-icon"></div>
            <div>Waiting for jobs from the swarm...</div>
          </div>
        ) : (
          <div className="job-active">
            <div className="job-row">
              <span>Job Type:</span>
              <span className="mono">{activeJob.jobType || 'compute'}</span>
            </div>
            <ProgressBar
              current={chunkStats?.completed || 0}
              total={chunkStats?.total || 1}
              label="Chunks processed"
            />
          </div>
        )}
      </div>
    </div>
  );
}