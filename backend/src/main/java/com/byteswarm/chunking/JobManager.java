package com.byteswarm.chunking;

import com.byteswarm.model.JobStatus;
import com.byteswarm.server.ChunkDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class JobManager {
    private static final Logger log = LoggerFactory.getLogger(JobManager.class);
    private static final JobManager INSTANCE = new JobManager();

    private final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<Chunk>> jobChunks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> jobResults = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> chunkComputeTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> chunkComputeCounts = new ConcurrentHashMap<>();

    private final AtomicLong reassignedChunks = new AtomicLong(0);
    private final AtomicLong droppedWorkers = new AtomicLong(0);

    private JobManager() {}

    public static JobManager getInstance() {
        return INSTANCE;
    }

    public String submitJob(List<String> dataset, int chunkSize) {
        String jobId = "job-" + UUID.randomUUID().toString().substring(0, 8);
        List<Chunk> chunks = ChunkingEngine.chunkDataset(dataset, chunkSize, jobId);

        jobChunks.put(jobId, chunks);
        jobResults.put(jobId, new ConcurrentHashMap<>());
        chunkComputeTimes.put(jobId, new AtomicLong(0));
        chunkComputeCounts.put(jobId, new AtomicLong(0));

        JobStatus status = new JobStatus(jobId, chunks.size());
        jobStatuses.put(jobId, status);

        log.info(" Submitted job {} with {} chunks", jobId, chunks.size());
        ChunkDispatcher.dispatch(chunks);
        return jobId;
    }

    public void recordResult(String jobId, String chunkId, Object result) {
        recordResult(jobId, chunkId, result, 0);
    }

    public void recordResult(String jobId, String chunkId, Object result, long computeTimeMs) {
        Map<String, Object> results = jobResults.get(jobId);
        if (results == null) {
            log.warn("Unknown job: {}", jobId);
            return;
        }

        Chunk chunk = findChunk(jobId, chunkId);
        if (chunk == null) {
            log.warn("Unknown chunk {} for job {}", chunkId, jobId);
            return;
        }

        if (chunk.getStatus() == ChunkStatus.COMPLETED) {
            log.warn("Duplicate/late result ignored for chunk {} of job {}", chunkId, jobId);
            return;
        }

        results.put(chunkId, result);
        chunk.setStatus(ChunkStatus.COMPLETED);

        if (computeTimeMs > 0) {
            AtomicLong totalTime = chunkComputeTimes.get(jobId);
            AtomicLong count = chunkComputeCounts.get(jobId);
            if (totalTime != null) {
                totalTime.addAndGet(computeTimeMs);
            }
            if (count != null) {
                count.incrementAndGet();
            }
        }

        JobStatus status = jobStatuses.get(jobId);
        if (status != null) {
            status.setCompletedChunks((int) jobChunks.get(jobId).stream()
                    .filter(c -> c.getStatus() == ChunkStatus.COMPLETED)
                    .count());

            logJobProgress(jobId);

            if (status.getCompletedChunks() >= status.getTotalChunks()) {
                status.setState("COMPLETED");
                status.setCompletedAt(System.currentTimeMillis());

                long totalMs = chunkComputeTimes.get(jobId).get();
                long count = chunkComputeCounts.get(jobId).get();
                double avg = count > 0 ? (double) totalMs / count : 0;

                log.info(" Job {} COMPLETED — {} chunks | avg compute: {}ms",
                        jobId, status.getCompletedChunks(), String.format("%.2f", avg));
            }
        }
    }

    public void logJobProgress(String jobId) {
        JobStatus status = jobStatuses.get(jobId);
        if (status != null) {
            log.info(" Job {} progress: {}/{} completed, state={}",
                    jobId,
                    status.getCompletedChunks(),
                    status.getTotalChunks(),
                    status.getState());
        }
    }

    public List<Chunk> getInFlightChunks() {
        return jobChunks.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getStatus() == ChunkStatus.DISPATCHED)
                .collect(Collectors.toList());
    }

    public List<Chunk> getInFlightChunksForWorker(String workerId) {
        return jobChunks.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getStatus() == ChunkStatus.DISPATCHED)
                .filter(c -> workerId.equals(c.getAssignedWorkerId()))
                .collect(Collectors.toList());
    }

    public Chunk findChunk(String jobId, String chunkId) {
        List<Chunk> chunks = jobChunks.get(jobId);
        if (chunks == null) {
            return null;
        }
        return chunks.stream()
                .filter(c -> chunkId.equals(c.getChunkId()))
                .findFirst()
                .orElse(null);
    }

    public void markChunkReassigned(String jobId, String chunkId) {
        Chunk chunk = findChunk(jobId, chunkId);
        if (chunk != null) {
            chunk.setStatus(ChunkStatus.PENDING);
            chunk.setAssignedWorkerId(null);
            chunk.setDispatchedAt(0L);
            reassignedChunks.incrementAndGet();
        }
    }

    public void incrementDroppedWorkers() {
        droppedWorkers.incrementAndGet();
    }

    public long getReassignedChunksCount() {
        return reassignedChunks.get();
    }

    public long getDroppedWorkersCount() {
        return droppedWorkers.get();
    }

    public int getTotalChunksCount() {
        return jobChunks.values().stream().mapToInt(List::size).sum();
    }

    public int getCompletedChunksCount() {
        return (int) jobChunks.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getStatus() == ChunkStatus.COMPLETED)
                .count();
    }

    public double getAverageComputeTime(String jobId) {
        AtomicLong total = chunkComputeTimes.get(jobId);
        AtomicLong count = chunkComputeCounts.get(jobId);
        if (total == null || count == null || count.get() == 0) {
            return 0;
        }
        return (double) total.get() / count.get();
    }

    public JobStatus getStatus(String jobId) {
        return jobStatuses.get(jobId);
    }

    public List<Chunk> getChunks(String jobId) {
        return jobChunks.get(jobId);
    }

    public Map<String, Object> getResults(String jobId) {
        return jobResults.get(jobId);
    }

    public Collection<JobStatus> getAllJobs() {
        return jobStatuses.values();
    }
}