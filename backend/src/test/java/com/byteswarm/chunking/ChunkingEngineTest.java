package com.byteswarm.chunking;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ChunkingEngineTest {

    @Test
    void shouldChunkEvenlyDividedDataset() {
        List<String> dataset = List.of("1+1", "2+2", "3+3", "4+4");
        List<Chunk> chunks = ChunkingEngine.chunkDataset(dataset, 2, "test-job");

assertEquals(2, chunks.size());
assertEquals(2, chunks.get(0).getPayload().size());
assertEquals(2, chunks.get(1).getPayload().size());
assertEquals("test-job-chunk-0", chunks.get(0).getChunkId());
    }

    @Test
    void shouldHandleUnevenChunkSize() {
        List<String> dataset = List.of("a", "b", "c", "d", "e");
        List<Chunk> chunks = ChunkingEngine.chunkDataset(dataset, 2, "job");

assertEquals(3, chunks.size());
assertEquals(2, chunks.get(0).getPayload().size());
assertEquals(2, chunks.get(1).getPayload().size());
assertEquals(1, chunks.get(2).getPayload().size());
    }

    @Test
    void shouldGenerateExactNumberOfMockEquations() {
        List<String> equations = ChunkingEngine.generateMockDataset(10_000);
assertEquals(10_000, equations.size());
        for (String eq : equations) {
assertTrue(eq.matches("\\d+[+\\-*]\\d+"), "Bad equation: " + eq);
        }
    }

    @Test
    void shouldRejectEmptyDataset() {
assertThrows(IllegalArgumentException.class,
                () ->ChunkingEngine.chunkDataset(List.of(), 10, "j"));
    }

    @Test
    void shouldRejectZeroChunkSize() {
assertThrows(IllegalArgumentException.class,
                () ->ChunkingEngine.chunkDataset(List.of("1"), 0, "j"));
    }

    @Test
    void allChunksShouldReferenceSameJobId() {
        List<String> dataset = List.of("1", "2", "3", "4", "5", "6");
        List<Chunk> chunks = ChunkingEngine.chunkDataset(dataset, 2, "shared-job");
chunks.forEach(c ->assertEquals("shared-job", c.getJobId()));
    }

    @Test
    void chunksShouldStartAsPending() {
        List<String> dataset = List.of("1", "2");
        List<Chunk> chunks = ChunkingEngine.chunkDataset(dataset, 1, "j");
chunks.forEach(c ->assertEquals(ChunkStatus.PENDING, c.getStatus()));
    }
}
