package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.constant.OperationConstants;
import edu.umd.cs.findbugs.detect.constant.OperationType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Yu 2019/2/25 19:44
 */
public class FieldResourceOperationDetector implements OperationConstants {

    {
        openWhite = new HashMap<>();
        closeWhite = new HashMap<>();
        openBlack = new HashMap<>();
        closeBlack = new HashMap<>();
    }

    private Map<Integer, ResourceOperation> openWhite;

    private Map<Integer, ResourceOperation> closeWhite;

    private Map<Integer, ResourceOperation> openBlack;

    private Map<Integer, ResourceOperation> closeBlack;

    public void appendOperation(ResourceOperation operation, String operationType,
                                String whiteOrBlack) {
        Map<Integer, ResourceOperation> tempMap = null;
        OperationType type = OperationType.parse(operationType, whiteOrBlack);
        switch (type){
            case OPEN_WHITE:
                tempMap = openWhite;
                break;
            case OPEN_BLACK:
                tempMap = openBlack;
                break;
            case CLOSE_WHITE:
                tempMap = closeWhite;
                break;
            case CLOSE_BLACK:
                tempMap = closeBlack;
                break;
        }
        tempMap.put(operation.hashCode(),operation);
    }

    public boolean inOpenWhiteList(ResourceOperation operation){
        return openWhite.containsKey(operation.hashCode());
    }
    public boolean inOpenBlackList(ResourceOperation operation){
        return openBlack.containsKey(operation.hashCode());
    }
    public boolean inCloseWhiteList(ResourceOperation operation){
        if (closeWhite.containsKey(operation.hashCode())) {
            operation.addFields(closeWhite.get(operation.hashCode()).getFields());
            return true;
        }
        return false;
    }
    public boolean inCloseBlackList(ResourceOperation operation){
        return closeBlack.containsKey(operation.hashCode());
    }
}
