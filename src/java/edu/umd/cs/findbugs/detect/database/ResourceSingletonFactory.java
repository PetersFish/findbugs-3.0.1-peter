package edu.umd.cs.findbugs.detect.database;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Yu
 * @date 2018/6/13 11:10
 */
public class ResourceSingletonFactory {

    private static final ResourceSingletonFactory factory = new ResourceSingletonFactory();

    private final Map<Integer,Resource> resourceMap = new HashMap<>();

    private String customResources;

    private ResourceSingletonFactory(){}

    public static ResourceSingletonFactory getInstance(){
        return factory;
    }

    public Map<Integer, Resource> getResourceMap() {
        return resourceMap;
    }

    public String getCustomResources() {
        return customResources;
    }

    public void setCustomResources(String customResources) {
        this.customResources = customResources;
    }

    public static void main(String[] args) {
        ResourceSingletonFactory factory1 = ResourceSingletonFactory.getInstance();
        ResourceSingletonFactory factory2 = ResourceSingletonFactory.getInstance();
        factory1.setCustomResources("haha");
        String customResources = factory2.getCustomResources();
        System.out.println(customResources);
    }
}
