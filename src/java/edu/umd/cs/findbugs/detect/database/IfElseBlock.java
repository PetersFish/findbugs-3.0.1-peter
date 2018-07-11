package edu.umd.cs.findbugs.detect.database;

import java.util.BitSet;

/**
 * @author Peter Yu 2018/7/11 16:04
 */
public class IfElseBlock {

    private static Integer count = 1;

    private Integer blockId;

    private BitSet range;

    public IfElseBlock(BitSet range) {
        blockId = count;
        this.range = range;
        count ++;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public BitSet getRange() {
        return range;
    }

    public void setRange(BitSet range) {
        this.range = range;
    }

    public static void main(String[] args) {
        BitSet bitSet = new BitSet();
        bitSet.set(1,5);
        IfElseBlock block = new IfElseBlock(bitSet);
        System.out.println(block.getBlockId());
        System.out.println(bitSet);
        bitSet.clear();
        bitSet.set(3,4);
        IfElseBlock block2 = new IfElseBlock(bitSet);
        System.out.println(block2.getBlockId());
        System.out.println(bitSet);
    }
}
