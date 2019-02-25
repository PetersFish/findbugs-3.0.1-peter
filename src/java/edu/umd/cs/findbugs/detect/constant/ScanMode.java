package edu.umd.cs.findbugs.detect.constant;

/**
 * @author Peter Yu 2019/2/25 15:44
 */
public enum  ScanMode {

    /**
     * VARIABLE[变量模式],
     * FIELD[属性模式]
     */
    VARIABLE("variable"),FIELD("field"),ILLEGAL("");

    private String mode;

    ScanMode(String mode) {
        this.mode = mode;
    }

    public static ScanMode parse(String mode){
        ScanMode[] values = ScanMode.values();
        for (ScanMode scanMode : values) {
            if (scanMode.getMode().equals(mode)) {
                return scanMode;
            }
        }
        return ScanMode.ILLEGAL;
    }

    public String getMode() {
        return mode;
    }

}
