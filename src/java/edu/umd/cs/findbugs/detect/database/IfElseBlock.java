package edu.umd.cs.findbugs.detect.database;

import java.util.*;

/**
 * 条件语句块的描述类
 * @author Peter Yu 2018/7/11 16:04
 */
public class IfElseBlock {

    private static Integer count = 1;// 0 留给main block

    private final Integer blockId;

    private Integer blockIndex = 1;// 0 留给else block

    private Integer elseBlockStart;// else块开始位置

    private Integer elseBlockEnd;// else块结束位置，起到标记IfElseBlock唯一性的作用

    private Integer blockLevel;// block的层级，1为第一层，2为第二层（包含在其他block里面）

    private final BitSet wholeRange = new BitSet();// 记录从if开始else结束的整个pc范围

    private final BitSet elseBlock = new BitSet();

    private final TreeMap<Integer,BitSet> branchMap = new TreeMap<>();// key用来存储blockIndex

    public IfElseBlock() {
        branchMap.put(0, elseBlock);
        blockId = count;
        count ++;
    }

    /**
     * 添加ifBlock
     * @param bitSet
     */
    public void addIfBlock(BitSet bitSet){
        branchMap.put(blockIndex, bitSet);
        ++ blockIndex;

        // 每加一次ifBlock，都需要对elseBlock的信息进行更新

        elseBlockStart = bitSet.length();
        updateElseBlock();

        // 更新wholeRange
        wholeRange.or(bitSet);
    }

    /**
     * 更新elseBlock的信息
     */
    private void updateElseBlock() {
        if(elseBlockStart != null&&elseBlockEnd != null){
            elseBlock.clear();
            elseBlock.set(elseBlockStart, elseBlockEnd);
        }
    }

    /**
     * 当知道else的结尾在哪里时
     * @param elseBlockEnd
     */
    public void setElseBlockEnd(Integer elseBlockEnd) {
        if (this.elseBlockEnd != null) {
            return;
        }
        this.elseBlockEnd = elseBlockEnd;
        updateElseBlock();

        // 更新wholeRange
        int start = wholeRange.previousSetBit(elseBlockEnd);
        wholeRange.set(start, elseBlockEnd);
    }


    /**
     * 判断代码是否在条件语句块里面
     * @param seen
     * @return
     */
    public Map.Entry<Integer,BitSet> inBlock(int seen){
        for (Map.Entry<Integer, BitSet> entry : branchMap.entrySet()) {
            BitSet range = entry.getValue();
            boolean inRange = range.get(seen);
            if (inRange) {
                return entry;
            }
        }
        return null;
    }

    public boolean inBranch(int branchFallThrough, int branchTarget){
        BitSet target = new BitSet();
        target.set(branchFallThrough, branchTarget);
        for (BitSet bitSet : branchMap.values()) {
            boolean include = include(bitSet, target);
            if (include) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断range是否包含target
     * @param range
     * @param target
     * @return
     */
    private boolean include(BitSet range, BitSet target) {
        BitSet tempSet = new BitSet();
        tempSet.or(range);
        tempSet.and(target);
        return tempSet.equals(target);
    }

    public BitSet getWholeRange() {
        return wholeRange;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public TreeMap<Integer, BitSet> getBranchMap() {
        return branchMap;
    }

    public Integer getElseBlockEnd() {
        return elseBlockEnd;
    }

    public Integer getBlockLevel() {
        return blockLevel;
    }

    public void setBlockLevel(Integer blockLevel) {
        this.blockLevel = blockLevel;
    }

    public BitSet getElseBlock() {
        return elseBlock;
    }

    @Override
    public int hashCode() {
        return blockId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IfElseBlock)){
            return false;
        }
        IfElseBlock block = (IfElseBlock) obj;
        return (block.getBlockId() == this.getBlockId());
    }

    public static void main(String[] args) {
        BitSet bitSet = new BitSet();
        bitSet.set(1,5);

    }

    /**
     * 判断block是否包含
     * @param branch
     * @return
     */
    public boolean hasBranch(BitSet branch) {
        for (BitSet bitSet : branchMap.values()) {
            return include(bitSet, branch)&&bitSet.length()==branch.length();
        }
        return false;
    }
}
