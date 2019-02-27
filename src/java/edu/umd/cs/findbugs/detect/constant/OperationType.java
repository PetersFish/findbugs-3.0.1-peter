package edu.umd.cs.findbugs.detect.constant;

/**
 * 资源开启关闭的黑白名单种类
 * @author Peter Yu 2019/2/25 19:52
 */
public enum  OperationType implements OperationConstants{
    OPEN_WHITE(OPEN, WHITE),
    OPEN_BLACK(OPEN, BLACK),
    CLOSE_WHITE(CLOSE, WHITE),
    CLOSE_BLACK(CLOSE, BLACK),
    ILLEGAL("","");

    private String operationType;

    private String listType;

    OperationType(String operationType, String listType) {
        this.operationType = operationType;
        this.listType = listType;
    }

    public static OperationType parse(String operationType, String listType){
        OperationType[] values = OperationType.values();
        for (OperationType type : values) {
            if (type.getOperationType().equals(operationType)
                && type.getListType().equals(listType)) {
                return type;
            }
        }
        return OperationType.ILLEGAL;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getListType() {
        return listType;
    }
}
