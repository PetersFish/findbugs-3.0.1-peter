package edu.umd.cs.findbugs.detect.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用于记录已经判定的operation
 * @author Peter Yu
 * @date 2018/6/14 10:18
 */
public class ResourceOperationDetector {

    {
        openWhiteMap = new HashMap<>();
        closeWhiteMap = new HashMap<>();
        openBlackMap = new HashMap<>();
        closeBlackMap = new HashMap<>();
        init();
    }

    private static final String OPEN = "OPEN";
    private static final String CLOSE = "CLOSE";
    private static final String WHITE = "WHITE";
    private static final String BLACK = "BLACK";

    private Map<Resource,Set<ResourceOperation>> openWhiteMap;

    private Map<Resource,Set<ResourceOperation>> closeWhiteMap;

    private Map<Resource,Set<ResourceOperation>> openBlackMap;

    private Map<Resource,Set<ResourceOperation>> closeBlackMap;

    /**
     * 将ResourceFactory里面的规则都导入到白名单中
     */
    private void init(){
        Set<Resource> resourceSet = ResourceFactory.listResources();
        for (Resource resource : resourceSet) {
            Set<ResourceOperation> addMethodSet = resource.getAddMethodSet();
            Set<ResourceOperation> delMethodSet = resource.getDelMethodSet();
            // 将addMethodSet和delMethodSet中的operation都加入到相应的白名单
            for (ResourceOperation operation : addMethodSet) {
                appendOperation(operation,OPEN,WHITE);
            }
            for (ResourceOperation operation : delMethodSet) {
                appendOperation(operation,CLOSE,WHITE);
            }
        }
    }

    /**
     * 追加operation的白名单或黑名单
     * @param operation
     * @param operationType
     * @param whiteOrBlack
     */
    public void appendOperation(ResourceOperation operation, String operationType, String whiteOrBlack){

        String clazzName = operation.getClazzName();
        Resource resource = new Resource(clazzName);
        Map<Resource,Set<ResourceOperation>> tempMap = null;

        if(OPEN.equals(operationType)&&WHITE.equals(whiteOrBlack)){
            tempMap = openWhiteMap;
        }else if (OPEN.equals(operationType)&&BLACK.equals(whiteOrBlack)){
            tempMap = openBlackMap;
        }else if (CLOSE.equals(operationType)&&WHITE.equals(whiteOrBlack)){
            tempMap = closeWhiteMap;
        }else if (CLOSE.equals(operationType)&&BLACK.equals(whiteOrBlack)){
            tempMap = closeBlackMap;
        }

        if(tempMap.containsKey(resource)){
            Set<ResourceOperation> operationSet = tempMap.get(resource);
            operationSet.add(operation);
        }else {
            HashSet<ResourceOperation> operationHashSet = new HashSet<>();
            operationHashSet.add(operation);
            tempMap.put(resource,operationHashSet);
        }
    }

    /**
     * 判断是否在清单里面
     *
     * @param targetOperation
     * @param operationType
     * @param whiteOrBlack
     * @return
     */
    public boolean inNameList(ResourceOperation targetOperation, String operationType, String whiteOrBlack) {
        String clazzName = targetOperation.getClazzName();
        Resource resource = new Resource(clazzName);
        Map<Resource,Set<ResourceOperation>> tempMap = null;

        if(OPEN.equals(operationType)&&WHITE.equals(whiteOrBlack)){
            tempMap = openWhiteMap;
        }else if (OPEN.equals(operationType)&&BLACK.equals(whiteOrBlack)){
            tempMap = openBlackMap;
        }else if (CLOSE.equals(operationType)&&WHITE.equals(whiteOrBlack)){
            tempMap = closeWhiteMap;
        }else if (CLOSE.equals(operationType)&&BLACK.equals(whiteOrBlack)){
            tempMap = closeBlackMap;
        }

        if(tempMap.containsKey(resource)){
            Set<ResourceOperation> operationSet = tempMap.get(resource);
            return operationSet.contains(targetOperation);
        }
        return false;
    }
}
