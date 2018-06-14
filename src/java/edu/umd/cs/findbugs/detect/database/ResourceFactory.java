package edu.umd.cs.findbugs.detect.database;


import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Peter Yu
 * @date 2018/6/6 17:37
 */
public class ResourceFactory {

    private ResourceFactory(){}

    private static final Logger LOGGER = Logger.getLogger(ResourceFactory.class.getName());

    private static final Set<Resource> resourceSet;

    // 初始化所有资源，将生成的资源放入resourceSet
    static {
        resourceSet = new HashSet<>();
        loadResources();
//        Resource outputStream = new Resource("java.io.FileOutputStream");
//        outputStream.appendAddMethod(new ResourceOperation("java.io.FileOutputStream","<init>","(Ljava/lang/String;)V"));
//        outputStream.appendDelMethod(new ResourceOperation("java.io.FileOutputStream","close","()V"));
//        outputStream.appendDelMethod(new ResourceOperation("java.io.OutputStream","close","()V"));
//        addResource(outputStream);
    }

    private static Resource addResource(Resource resource) {
        boolean add = resourceSet.add(resource);
        if(!add){
            for (Resource re : resourceSet) {
                if(re.equals(resource)){
                    return re;
                }
            }
        }
        return resource;
    }

    public static void loadResources(){
        try {
            InputStream inputStream = ResourceFactory.class.getResourceAsStream("/custom/resource.xml");
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(inputStream);
            extractResource(document);
        } catch (DocumentException e) {
            LOGGER.warning("Incorrect resource config file.");
        } catch (Exception e){
            LOGGER.warning("File not found.");
        }
    }

    public static void appendCustomResource(String customResource){
        try {
            if(StringUtils.isBlank(customResource)){
                return;
            }
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new StringReader(customResource));
            extractResource(document);
        } catch (Exception e) {
            LOGGER.warning("The custom resources are not configed in the right way. Please check it! ");
        }
    }

    private static void extractResource(Document document) {
        Element rootElement = document.getRootElement();
        List<Element> resourceList = rootElement.elements("Resource");
        for (Element element : resourceList) {
            String className = element.attribute("className").getValue();
            Resource resource = new Resource(className);
            resource = addResource(resource);
            List<Element> addMethods = element.element("addMethods").elements("addMethod");
            for (Element addMethod : addMethods) {
                String className1 = addMethod.attribute("className").getValue();
                String methodName = addMethod.attribute("methodName").getValue();
                String methodSignature = addMethod.attribute("methodSignature").getValue();
                ResourceOperation addOperation = new ResourceOperation(className1, methodName, methodSignature);
                resource.appendAddMethod(addOperation);
            }
            List<Element> delMethods = element.element("delMethods").elements("delMethod");
            for (Element delMethod : delMethods) {
                String className1 = delMethod.attribute("className").getValue();
                String methodName = delMethod.attribute("methodName").getValue();
                String methodSignature = delMethod.attribute("methodSignature").getValue();
                ResourceOperation delOperation = new ResourceOperation(className1, methodName, methodSignature);
                resource.appendDelMethod(delOperation);
            }
        }
    }

    public static Set<Resource> listResources(){
        return resourceSet;
    }

    /**
     * 给指定资源类型添加Open方法
     * @param resource
     * @param operation
     * @return
     */
    public static boolean appendAddMethod(Resource resource, ResourceOperation operation){
        for (Resource tempResource : resourceSet) {
            if(tempResource.getClassName().equals(resource.getClassName())){
                return tempResource.appendAddMethod(operation);
            }
        }
        return false;
    }

    public static boolean appendDelMethod(Resource resource, ResourceOperation operation){
        for (Resource tempResource : resourceSet) {
            if(tempResource.getClassName().equals(resource.getClassName())){
                return tempResource.appendDelMethod(operation);
            }
        }
        return false;
    }

    public static void main(String[] args) {
        loadResources();
    }

}
