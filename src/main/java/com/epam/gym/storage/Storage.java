package com.epam.gym.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class Storage {

    @Getter
    private final Map<String, Map<Long, Object>> dataStore = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> idGenerators = new ConcurrentHashMap<>();

    public Storage() {
        initializeNamespace("trainees");
        initializeNamespace("trainers");
        initializeNamespace("trainings");
        initializeNamespace("trainingTypes");
        log.info("Storage initialized with namespaces: {}", dataStore.keySet());
    }

    private void initializeNamespace(String namespace) {
        dataStore.put(namespace, new ConcurrentHashMap<>());
        idGenerators.put(namespace, new AtomicLong(1));
    }

    public Long generateId(String namespace) {
        return idGenerators.get(namespace).getAndIncrement();
    }

    public void put(String namespace, Long id, Object value) {
        dataStore.get(namespace).put(id, value);
        log.debug("Stored in [{}]: id={}, value={}", namespace, id, value);
    }

    public Object get(String namespace, Long id) {
        return dataStore.get(namespace).get(id);
    }

    public Object remove(String namespace, Long id) {
        Object removed = dataStore.get(namespace).remove(id);
        log.debug("Removed from [{}]: id={}", namespace, id);
        return removed;
    }

    public Map<Long, Object> getAll(String namespace) {
        return new ConcurrentHashMap<>(dataStore.get(namespace));
    }

    public boolean contains(String namespace, Long id) {
        return dataStore.get(namespace).containsKey(id);
    }

    public long count(String namespace) {
        return dataStore.get(namespace).size();
    }
}
