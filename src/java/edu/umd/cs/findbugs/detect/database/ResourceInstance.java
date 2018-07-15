package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.BugInstance;

import java.util.*;

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
     * 实例所在的行数
     */
    private Integer pc;

    /**
     * 存放所在条件语句块
     */
    private Set<IfElseBlockLocation> inBlockSet = new HashSet<>();

    private boolean removeForbidden = false;

    /**
     * 错误报告
     */
    private final BugInstance bugInstance;

    public ResourceInstance(Resource resource, Integer stackIndex, Integer pc, BugInstance bugInstance) {
        this.resource = resource;
        this.stackIndex = stackIndex;
        this.bugInstance = bugInstance;
        this.pc = pc;
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

    public Integer getPc() {
        return pc;
    }

    public void setPc(Integer pc) {
        this.pc = pc;
    }

    public boolean addInBlock(IfElseBlockLocation location){
        return inBlockSet.add(location);
    }

    public Set<IfElseBlockLocation> getInBlockSet(){
        return inBlockSet;
    }

    public boolean isRemoveForbidden() {
        return removeForbidden;
    }

    public void setRemoveForbidden(boolean removeForbidden) {
        this.removeForbidden = removeForbidden;
    }
}
