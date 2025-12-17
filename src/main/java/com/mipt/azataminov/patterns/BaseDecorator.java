package com.mipt.azataminov.patterns;

import java.util.Optional;

public abstract class BaseDecorator implements DataService {
    protected final DataService wrapped;

    public BaseDecorator(DataService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Optional<String> findDataByKey(String key) {
        return wrapped.findDataByKey(key);
    }

    @Override
    public void saveData(String key, String data) {
        wrapped.saveData(key, data);
    }

    @Override
    public boolean deleteData(String key) {
        return wrapped.deleteData(key);
    }
}