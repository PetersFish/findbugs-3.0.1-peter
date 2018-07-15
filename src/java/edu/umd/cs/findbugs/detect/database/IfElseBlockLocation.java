package edu.umd.cs.findbugs.detect.database;

import java.util.BitSet;

/**
 * @author Peter Yu 2018/7/15 11:04
 */
public class IfElseBlockLocation {

    private IfElseBlock block;

    private Integer branchIndex;

    private BitSet branchRange;

    private boolean inBlock = false;

    public IfElseBlockLocation() {
    }

    public IfElseBlockLocation(IfElseBlock block, Integer branchIndex, BitSet branchRange) {
        this.block = block;
        this.branchIndex = branchIndex;
        this.branchRange = branchRange;
        this.inBlock = true;
    }

    public IfElseBlock getBlock() {
        return block;
    }

    public void setBlock(IfElseBlock block) {
        this.block = block;
    }

    public Integer getBranchIndex() {
        return branchIndex;
    }

    public void setBranchIndex(Integer branchIndex) {
        this.branchIndex = branchIndex;
    }

    public BitSet getBranchRange() {
        return branchRange;
    }

    public void setBranchRange(BitSet branchRange) {
        this.branchRange = branchRange;
    }
}
