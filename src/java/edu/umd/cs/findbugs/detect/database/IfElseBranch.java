package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.database.container.BitSetBuffer;

import java.util.Objects;

/**
 * @author Peter Yu 2018/7/17 17:47
 */
public class IfElseBranch implements Comparable<IfElseBranch>{

    private Integer ifOpcode;

    private BitSetBuffer range;

    private BitSetBuffer exRange = new BitSetBuffer();

    private BitSetBuffer wholeRange = new BitSetBuffer();

    private Integer branchStart;

    private Integer branchEnd;

    private Integer gotoTarget;

    private Integer realEnd;

    public IfElseBranch() {
    }

    public IfElseBranch(Integer branchStart, Integer branchEnd, Integer ifOpcode) {
        this.branchStart = branchStart;
        this.branchEnd = branchEnd;
        this.ifOpcode = ifOpcode;
        range = new BitSetBuffer();
        range.set(branchStart, branchEnd);
        wholeRange.or(range);
    }

    public BitSetBuffer getRange() {
        return range;
    }

    public void setRange(BitSetBuffer range) {
        this.range = range;
    }

    public Integer getGotoTarget() {
        return gotoTarget;
    }

    public void setGotoTarget(Integer gotoTarget) {
        this.gotoTarget = gotoTarget;
        // 更新realEnd
        if(realEnd == null||realEnd < gotoTarget){
            realEnd = (gotoTarget > branchEnd)?gotoTarget:branchEnd;
            wholeRange.clear();
            wholeRange.set(branchStart, realEnd);
            exRange.set(branchEnd, realEnd);
        }
    }

    public Integer getBranchStart() {
        return branchStart;
    }

    public void setBranchStart(Integer branchStart) {
        this.branchStart = branchStart;
    }

    public Integer getBranchEnd() {
        return branchEnd;
    }

    public void setBranchEnd(Integer branchEnd) {
        this.branchEnd = branchEnd;
    }

    public BitSetBuffer getWholeRange() {
        return wholeRange;
    }

    public Integer getRealEnd() {
        return realEnd;
    }

    public BitSetBuffer getExRange() {
        return exRange;
    }

    public Integer getIfOpcode() {
        return ifOpcode;
    }

    public void setIfOpcode(Integer ifOpcode) {
        this.ifOpcode = ifOpcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfElseBranch branch = (IfElseBranch) o;
        return Objects.equals(range, branch.range);
    }

    @Override
    public int hashCode() {

        return Objects.hash(range);
    }

    @Override
    public int compareTo(IfElseBranch o) {
        return this.wholeRange.cardinality() - o.wholeRange.cardinality();
    }

    public BitSetBuffer inRange(Integer pc) {

        if(range.get(pc)){
            return range;
        }

        if(exRange.get(pc)){
            return exRange;
        }
        return null;
    }

    @Override
    public String toString() {
        return "IfElseBranch{" +
               "range=" + range +
               ", exRange=" + exRange +
               ", gotoTarget=" + gotoTarget +
               '}';
    }
}
