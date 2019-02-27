package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.constant.OperationConstants;
import edu.umd.cs.findbugs.util.SignatureUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用于记录已经判定的operation
 * @author Peter Yu
 * @date 2018/6/14 10:18
 */
public class ResourceOperationDetector implements OperationConstants {

    {
        openWhiteMap = new HashMap<>();
        closeWhiteMap = new HashMap<>();
        openBlackMap = new HashMap<>();
        closeBlackMap = new HashMap<>();
        init();
    }

    private Map<String,Set<ResourceOperation>> openWhiteMap;

    private Map<String,Set<ResourceOperation>> closeWhiteMap;

    private Map<String,Set<ResourceOperation>> openBlackMap;

    private Map<String,Set<ResourceOperation>> closeBlackMap;

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
                appendOperation(resource.getClassName(),operation,OPEN,WHITE);
            }
            for (ResourceOperation operation : delMethodSet) {
                appendOperation(resource.getClassName(),operation,CLOSE,WHITE);
            }
        }
    }



    /**
     * 追加operation的白名单或黑名单
     * @param operation
     * @param operationType
     * @param whiteOrBlack
     */
    public void appendOperation(String resource, ResourceOperation operation, String operationType, String whiteOrBlack){

        Map<String,Set<ResourceOperation>> tempMap = null;

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

    public boolean inOpenWhiteList(ResourceOperation targetOperation){
        String openClassName = SignatureUtils.getObjectReturnTypeClassName(targetOperation.getSignature());
        if (openClassName == null) {
            openClassName = targetOperation.getClazzName();
        }
        return inList(targetOperation, openClassName, openWhiteMap);
    }

    private boolean inList(ResourceOperation targetOperation, String openClassName, Map<String,Set<ResourceOperation>> map) {
        ResourceMacher macher = new ResourceMacher(openClassName);
        Resource resource = macher.resourceMatched();

        if (resource != null&&!OBJECT.equals(resource.getClassName())) {
            Set<ResourceOperation> operations = map.get(resource.getClassName());
            if (operations.contains(targetOperation)){
                return true;
            }
            for (ResourceOperation operation : operations) {
                if(targetOperation.match(operation)){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean inOpenBlackList(ResourceOperation targetOperation){
        Set<ResourceOperation> operations = openBlackMap.get(OPEN);
        if (operations == null) {
            openBlackMap.put(OPEN,new HashSet<ResourceOperation>());
            return false;
        }
        return operations.contains(targetOperation);
    }

    public boolean inCloseWhiteList(ResourceOperation targetOperation){
        String closeClassName = SignatureUtils.getObjectParamClassName(targetOperation.getSignature());
        if (closeClassName == null) {
            closeClassName = targetOperation.getClazzName();
        }
        return inList(targetOperation, closeClassName, closeWhiteMap);
    }


    public boolean inCloseBlackList(ResourceOperation targetOperation){
        Set<ResourceOperation> operations = closeBlackMap.get(CLOSE);
        if (operations == null) {
            closeBlackMap.put(CLOSE,new HashSet<ResourceOperation>());
            return false;
        }
        return operations.contains(targetOperation);
    }
}
