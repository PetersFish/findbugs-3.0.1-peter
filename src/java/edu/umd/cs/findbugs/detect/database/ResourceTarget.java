package edu.umd.cs.findbugs.detect.database;


/**
 * 记录关闭方法参数中可能是资源的类
 * 当且仅当realTarget为true时，关闭方法才能关闭ResourceTarget
 * @author Peter Yu 2018/7/25 14:15
 */
public class ResourceTarget {

    private String className;

    private boolean realTarget = false;

    public ResourceTarget(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isRealTarget() {
        return realTarget;
    }

    public void setRealTarget(boolean realTarget) {
        this.realTarget = realTarget;
    }
}
