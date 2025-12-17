package com.mipt.azataminov.patterns;

import java.util.Optional;

public class LoggingDecorator extends BaseDecorator {
    public LoggingDecorator(DataService wrapped) {
        super(wrapped);
    }

    @Override
    public Optional<String> findDataByKey(String key) {
        System.out.println("Логирование: поиск данных по ключу: " + key);
        Optional<String> result = wrapped.findDataByKey(key);
        System.out.println("Логирование: результат поиска - " + (result.isPresent() ? "найдено" : "не найдено"));
        return result;
    }

    @Override
    public void saveData(String key, String data) {
        System.out.println("Логирование: сохранение данных по ключу: " + key);
        wrapped.saveData(key, data);
        System.out.println("Логирование: данные сохранены");
    }

    @Override
    public boolean deleteData(String key) {
        System.out.println("Логирование: удаление данных по ключу: " + key);
        boolean result = wrapped.deleteData(key);
        System.out.println("Логирование: удаление " + (result ? "успешно" : "не удалось"));
        return result;
    }
}