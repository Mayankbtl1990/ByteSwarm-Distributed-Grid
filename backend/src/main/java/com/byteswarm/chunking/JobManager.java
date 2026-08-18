package com.byteswarm.chunking;

import com.byteswarm.model.JobStatus;
import com.byteswarm.server.ChunkDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JobManager {
    private static final Logger log = LoggerFactory.getLogger(JobManager.class);
    private static final JobManager INSTANCE = new JobManager();

    private final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<Chunk>> jobChunks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> jobResults = new ConcurrentHashMap<>();

    private JobManager() {}

    public static JobManager getInstance() { return INSTANCE; } // ✅ space added

    public String submitJob(List<String> dataset, int chunkSize) {
        String jobId = "job-" + UUID.randomUUID().toString().substring(0, 8);

        List<Chunk> chunks = ChunkingEngine.chunkDataset(dataset, chunkSize, jobId);
        jobChunks.put(jobId, chunks);
        jobResults.put(jobId, new ConcurrentHashMap<>());

        JobStatus status = new JobStatus(jobId, chunks.size());
        jobStatuses.put(jobId, status);

        log.info("Submitted job {} with {} chunks", jobId, chunks.size());
        ChunkDispatcher.dispatch(chunks);

        return jobId;
    }

    public void recordResult(String jobId, String chunkId, Object result) {
        Map<String, Object> results = jobResults.get(jobId);
        if (results == null) {
            log.warn("Unknown job: {}", jobId);
            return;
        }
        results.put(chunkId, result);

        JobStatus status = jobStatuses.get(jobId);
        if (status != null) {
            status.setCompletedChunks(results.size());
            if (results.size() >= status.getTotalChunks()) {
                status.setState("COMPLETED");
                status.setCompletedAt(System.currentTimeMillis());
                log.info("Job {} COMPLETED — {} chunks", jobId, results.size());
            }
        }
    }

    public JobStatus getStatus(String jobId) { return jobStatuses.get(jobId); } // ✅
    public List<Chunk> getChunks(String jobId) { return jobChunks.get(jobId); } // ✅
    public Map<String, Object> getResults(String jobId) { return jobResults.get(jobId); } // ✅
    public Collection<JobStatus> getAllJobs() { return jobStatuses.values(); } // ✅
}