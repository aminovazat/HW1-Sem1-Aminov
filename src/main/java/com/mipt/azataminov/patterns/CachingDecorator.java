package com.mipt.azataminov.patterns;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CachingDecorator extends BaseDecorator {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public CachingDecorator(DataService wrapped) {
        super(wrapped);
    }

    @Override
    public Optional<String> findDataByKey(String key) {
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }

        Optional<String> result = wrapped.findDataByKey(key);
        result.ifPresent(data -> cache.put(key, data));
        return result;
    }

    @Override
    public void saveData(String key, String data) {
        wrapped.saveData(key, data);
        cache.put(key, data);
    }

    @Override
    public boolean deleteData(String key) {
        cache.remove(key);
        return wrapped.deleteData(key);
    }
}