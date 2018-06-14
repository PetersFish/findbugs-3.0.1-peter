package edu.umd.cs.findbugs.detect.database;

import java.util.HashSet;
import java.util.Set;

/**
 * 用于记录已经判定的operation
 * @author Peter Yu
 * @date 2018/6/14 10:18
 */
public class ResourceOperationDetector {

    private Set<ResourceOperation> openWhiteList = new HashSet<>();

    private Set<ResourceOperation> openBlackList = new HashSet<>();

    private Set<ResourceOperation> closeWhiteList = new HashSet<>();

    private Set<ResourceOperation> closeBlackList = new HashSet<>();

    public boolean appendOpenWhiteList(ResourceOperation operation){
        return openWhiteList.add(operation);
    }

    public boolean appendOpenBlackList(ResourceOperation operation){
        return openBlackList.add(operation);
    }

    public boolean appendCloseWhiteList(ResourceOperation operation){
        return closeWhiteList.add(operation);
    }

    public boolean appendCloseBlackList(ResourceOperation operation){
        return closeBlackList.add(operation);
    }

    public boolean inOpenWhiteList(ResourceOperation operation){
        for (ResourceOperation resourceOperation : openWhiteList) {
            if(resourceOperation.equals(operation)){
                return true;
            }
        }
        return false;
    }

    public boolean inOpenBlackList(ResourceOperation operation){
        for (ResourceOperation resourceOperation : openBlackList) {
            if(resourceOperation.equals(operation)){
                return true;
            }
        }
        return false;
    }

    public boolean inCloseWhiteList(ResourceOperation operation){
        for (ResourceOperation resourceOperation : closeWhiteList) {
            if(resourceOperation.equals(operation)){
                return true;
            }
        }
        return false;
    }

    public boolean inCloseBlackList(ResourceOperation operation){
        for (ResourceOperation resourceOperation : closeBlackList) {
            if(resourceOperation.equals(operation)){
                return true;
            }
        }
        return false;
    }
}
