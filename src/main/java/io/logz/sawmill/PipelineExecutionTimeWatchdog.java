package io.logz.sawmill;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PipelineExecutionTimeWatchdog {
    private static final Logger logger = LoggerFactory.getLogger(PipelineExecutionTimeWatchdog.class);
    public static final int QUEUE_SIZE = 10;
    private final CircularFifoQueue<Bucket> circularFifoQueue;
    private final long timeFrame;

    public PipelineExecutionTimeWatchdog(long thresholdTimeMs) {
        this.timeFrame = thresholdTimeMs / QUEUE_SIZE;
        this.circularFifoQueue = new CircularFifoQueue<>(initQueue());
        initWatchdog();
    }

    private void initWatchdog() {
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(this::tick, 0, timeFrame, MILLISECONDS);
    }

    private List<Bucket> initQueue() {
        List<Bucket> buckets = new ArrayList<>();
        for (int i = 0; i < QUEUE_SIZE; i++) buckets.add(new Bucket());
        return buckets;
    }

    public Bucket getCurrentBucket() {
        return circularFifoQueue.get(QUEUE_SIZE - 1);
    }

    private void tick() {
        Bucket bucket = circularFifoQueue.poll();
        alertOvertimeProcessingDocs(bucket);

        bucket.clearBucket();

        circularFifoQueue.add(bucket);
    }

    private void alertOvertimeProcessingDocs(Bucket bucket) {
        bucket.getOnProcessDocs().forEach(doc -> {
            logger.warn("processing {} takes too long, more than threshold={}", doc, timeFrame*QUEUE_SIZE);
        });
    }

    public class Bucket {
        private final ConcurrentMap<String, Doc> idToDocs;

        private Bucket() {
            idToDocs = new ConcurrentHashMap<>();
        }

        public void addDoc(String id, Doc doc) {
            idToDocs.put(id, doc);
        }

        public void removeDoc(String id) {
            idToDocs.remove(id);
        }

        private void clearBucket() {
            idToDocs.clear();
        }

        private List<Doc> getOnProcessDocs() {
            return idToDocs.values().stream().collect(Collectors.toList());
        }
    }
}
