package com.byteswarm.chunking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChunkingEngine {
    private static final Logger log = LoggerFactory.getLogger(ChunkingEngine.class);

    private ChunkingEngine() {}


    public static List<Chunk>chunkDataset(List<String> dataset, int chunkSize, String jobId) {
        if (dataset == null || dataset.isEmpty()) {
            throw new IllegalArgumentException("Dataset cannot be empty");
        }
        if (chunkSize<= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }

        List<Chunk> chunks = new ArrayList<>();
        int total = dataset.size();

        for (int i = 0; i< total; i += chunkSize) {
            int end = Math.min(i + chunkSize, total);
            List<String> slice = new ArrayList<>(dataset.subList(i, end));
            String chunkId = jobId + "-chunk-" + (i / chunkSize);
            chunks.add(new Chunk(chunkId, jobId, slice));
        }

        log.info(" Chunked {} items into {} chunks (size {})", total, chunks.size(), chunkSize);
        return chunks;
    }
    public static List<String>generateMockDataset(int size) {
        Random rand = new Random(42);
        String[] ops = {"+", "-", "*"};
        List<String> equations = new ArrayList<>(size);

        for (int i = 0; i< size; i++) {
            int a = rand.nextInt(1000);
            int b = rand.nextInt(1000);
            String op = ops[rand.nextInt(ops.length)];
            equations.add(a + op + b);
        }

        log.info(" Generated {} mock equations", size);
        return equations;
    }
}
