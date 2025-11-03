package com.municipal.ui.utils;

import javafx.scene.image.Image;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {
    
    private static final ImageCache INSTANCE = new ImageCache();
    private final Map<Long, Image> cache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;
    
    private ImageCache() {}
    
    public static ImageCache getInstance() {
        return INSTANCE;
    }
    
    public void put(Long imageId, Image image) {
        if (imageId == null || image == null) return;
        
        if (cache.size() >= MAX_CACHE_SIZE) {
            Long firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
        }
        
        cache.put(imageId, image);
    }
    
    public Image get(Long imageId) {
        return cache.get(imageId);
    }
    
    public void remove(Long imageId) {
        cache.remove(imageId);
    }
    
    public void clear() {
        cache.clear();
    }
    
    public boolean contains(Long imageId) {
        return cache.containsKey(imageId);
    }
}
