package com.mipt.azataminov.patterns;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class MetricableDecorator extends BaseDecorator {
    private final MetricService metricService = new MetricService();

    public MetricableDecorator(DataService wrapped) {
        super(wrapped);
    }

    @Override
    public Optional<String> findDataByKey(String key) {
        Instant start = Instant.now();
        Optional<String> result = wrapped.findDataByKey(key);
        Duration duration = Duration.between(start, Instant.now());
        metricService.sendMetric("findDataByKey", duration);
        return result;
    }

    @Override
    public void saveData(String key, String data) {
        Instant start = Instant.now();
        wrapped.saveData(key, data);
        Duration duration = Duration.between(start, Instant.now());
        metricService.sendMetric("saveData", duration);
    }

    @Override
    public boolean deleteData(String key) {
        Instant start = Instant.now();
        boolean result = wrapped.deleteData(key);
        Duration duration = Duration.between(start, Instant.now());
        metricService.sendMetric("deleteData", duration);
        return result;
    }

    public static class MetricService {
        public void sendMetric(String methodName, Duration duration) {
            System.out.println("Метрика: метод " + methodName + " выполнялся: " + duration.toMillis() + "ms");
        }
    }
}