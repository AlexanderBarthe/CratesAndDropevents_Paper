package dev.upscairs.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiHashMap<K, V> {

    private final HashMap<K, List<V>> map;

    public MultiHashMap() {
        map = new HashMap<>();
    }

    public List<V> get(K key) {
        return map.getOrDefault(key, new ArrayList<>());
    }

    public void put(K key, V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void putAll(K key, ArrayList<V> values) {
        map.put(key, values);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void removeEntry(K key, V value) {
        List<V> list = map.get(key);
        if (list != null) {
            list.remove(value);

            // Remove key if list is empty
            if (list.isEmpty()) map.remove(key);
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public long valuesOf(K key) {
        List<V> list = map.get(key);
        return (list == null) ? 0 : list.size();
    }

    public List<V> getAll() {
        List<V> list = new ArrayList<>();
        map.values().forEach(list::addAll);
        return list;
    }

    public void clear() {
        map.clear();
    }

}
