package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.BugInstance;

/**
 * 资源实例：
 * 包含的信息有：资源种类（Resource），变量在操作数栈里面的坐标，错误报告
 *
 * @author Peter Yu
 * @date 2018/6/7 14:44
 */
public class ResourceInstance {

    /**
     * 资源种类
     */
    private final Resource resource;

    /**
     * 变量在操作数栈里面的坐标
     */
    private Integer stackIndex = null;

    /**
     * 错误报告
     */
    private final BugInstance bugInstance;

    public ResourceInstance(Resource resource, Integer stackIndex, BugInstance bugInstance) {
        this.resource = resource;
        this.stackIndex = stackIndex;
        this.bugInstance = bugInstance;
    }

    public Resource getResource() {
        return resource;
    }

    public Integer getStackIndex() {
        return stackIndex;
    }

    public void setStackIndex(Integer stackIndex) {
        this.stackIndex = stackIndex;
    }

    public BugInstance getBugInstance() {
        return bugInstance;
    }
}
