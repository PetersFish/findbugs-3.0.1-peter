package edu.umd.cs.findbugs.detect.database;


import edu.umd.cs.findbugs.detect.constant.ScanMode;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
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

    private static Integer MAX_LOOK_INTO_LEVEL = 2;

    private static final Set<Resource> resourceSet;

    private static ScanMode scanMode = ScanMode.VARIABLE;

    // 初始化所有资源，将生成的资源放入resourceSet
    static {
        resourceSet = new HashSet<>();
        loadResources();
    }

    public static Set<Resource> listResources(){
        return resourceSet;
    }

    /**
     * 判断字符串是否包含资源，包含则资源相关
     * @param sig
     * @return
     */
    public static boolean signatureInvovlesResource(String sig){
        sig = sig.replaceAll("/",".").replaceAll("java.io.File", "java.io.");
        for (Resource resource : resourceSet) {
            if(sig.indexOf(resource.getClassName()) >= 0){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断操作是否资源相关
     * @param operation
     * @return
     */
    public static boolean operationInvolvesResource(ResourceOperation operation){
        if(signatureInvovlesResource(operation.getClazzName())){
            return true;
        }
        if(signatureInvovlesResource(operation.getSignature())){
            return true;
        }
        return false;
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

    /**
     * 给指定资源类型添加Close方法
     * @param resource
     * @param operation
     * @return
     */
    public static boolean appendDelMethod(Resource resource, ResourceOperation operation){
        for (Resource tempResource : resourceSet) {
            if(tempResource.getClassName().equals(resource.getClassName())){
                return tempResource.appendDelMethod(operation);
            }
        }
        return false;
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

    private static void loadResources(){
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

    private static void extractResource(Document document) {
        Element rootElement = document.getRootElement();
        scanMode = ScanMode.parse(rootElement.attribute("scanMode").getStringValue());
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

    public static Integer getMaxLookIntoLevel() {
//        if (isFieldMode()) {
//            return 1;
//        }
        return MAX_LOOK_INTO_LEVEL;
    }

    public static void setMaxLookIntoLevel(Integer maxLookIntoLevel) {
        MAX_LOOK_INTO_LEVEL = maxLookIntoLevel;
    }

    public static void main(String[] args) {
        loadResources();
    }

    public static boolean isFieldMode() {
        return scanMode.equals(ScanMode.FIELD);
    }
}
