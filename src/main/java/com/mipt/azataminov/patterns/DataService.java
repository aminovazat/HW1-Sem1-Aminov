package com.mipt.azataminov.patterns;

import java.util.Optional;

public interface DataService {
    Optional<String> findDataByKey(String key);
    void saveData(String key, String data);
    boolean deleteData(String key);
}