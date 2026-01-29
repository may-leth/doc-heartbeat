package com.may.soul.doc_heartbeat.benchmark;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class TikaPerformanceTest {

    private static final int ITERATIONS = 1000;
    private static final String SAMPLE_TEXT = "Sample document content for testing";

    @Test
    void comparePerformance() throws Exception {
        StopWatch watch1 = new StopWatch("Shared Instance");
        Tika sharedTika = new Tika();

        watch1.start();
        for (int i = 0; i < ITERATIONS; i++) {
            InputStream is = new ByteArrayInputStream(SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8));
            sharedTika.parseToString(is);
        }
        watch1.stop();

        StopWatch watch2 = new StopWatch("New Instance Each Time");

        watch2.start();
        for (int i = 0; i < ITERATIONS; i++) {
            Tika newTika = new Tika();
            InputStream is = new ByteArrayInputStream(SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8));
            newTika.parseToString(is);
        }
        watch2.stop();

        System.out.println("=== Resultados del Benchmark ===");
        System.out.println("Instancia compartida: " + watch1.getTotalTimeMillis() + "ms");
        System.out.println("Nueva instancia: " + watch2.getTotalTimeMillis() + "ms");
        System.out.println("Diferencia: " + (watch2.getTotalTimeMillis() - watch1.getTotalTimeMillis()) + "ms");
    }
}