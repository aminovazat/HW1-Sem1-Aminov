package com.mipt.azataminov.patterns;

import java.util.Optional;

public class ValidationDecorator extends BaseDecorator {
    public ValidationDecorator(DataService wrapped) {
        super(wrapped);
    }

    @Override
    public Optional<String> findDataByKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ не может быть null или пустым");
        }
        return wrapped.findDataByKey(key);
    }

    @Override
    public void saveData(String key, String data) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ не может быть null или пустым");
        }
        if (data == null) {
            throw new IllegalArgumentException("Данные не могут быть null");
        }
        wrapped.saveData(key, data);
    }

    @Override
    public boolean deleteData(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ не может быть null или пустым");
        }
        return wrapped.deleteData(key);
    }
}