import React from 'react';

export default function JobsSection({ activeJob, chunkStats }) {
  return (
    <div className="jobs-section">
      <div className="jobs-header">
        <span> Active Job</span>
        {activeJob && <span className="job-id mono">{activeJob.jobId?.slice(0, 20)}...</span>}
      </div>
      <div className="jobs-body">
        {!activeJob ? (
          <div className="no-job">
            <div className="no-job-icon"></div>
            <div>Waiting for jobs from the swarm...</div>
            <div className="no-job-hint">Job runner arriving Day 2</div>
          </div>
        ) : (
          <div className="job-active">
            <div className="job-row">
              <span>Job Type:</span>
              <span className="mono">{activeJob.jobType || 'compute'}</span>
            </div>
            <div className="job-row">
              <span>Chunks processed:</span>
              <span className="mono" style={{ color: '#0f0' }}>
                {chunkStats?.completed || 0}
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}