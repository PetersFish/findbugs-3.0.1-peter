package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.detect.database.container.BitSetBuffer;

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
     * 资源所在分支
     */
    private IfElseBranch branch;

    /**
     * 属性变量名：如果resource是属性，则记录变量名
     */
    private String fieldName;

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

    public IfElseBranch getBranch() {
        return branch;
    }

    public void setBranch(IfElseBranch branch) {
        this.branch = branch;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "ResourceInstance{" +
               "resource=" + resource +
               ", stackIndex=" + stackIndex +
               ", pc=" + pc +
               ", fieldName='" + fieldName + '\'' +
               '}';
    }

    public BitSetBuffer getClosedRange() {
        if (branch == null) {
            return null;
        }

        BitSetBuffer range = branch.getRange();
        if (range.get(pc)) {
            return range;
        }

        BitSetBuffer exRange = branch.getExRange();
        if (exRange.get(pc)) {
            return exRange;
        }
        return null;
    }
}
