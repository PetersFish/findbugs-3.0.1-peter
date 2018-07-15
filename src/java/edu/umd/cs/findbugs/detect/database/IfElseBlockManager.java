package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.database.container.LinkedStack;

import java.util.*;

/**
 * IfElseBlock的管理功能类
 *
 * @author Peter Yu 2018/7/11 16:56
 */
public class IfElseBlockManager {

    private Set<IfElseBlock> blockSet = new HashSet<>();// 记录所有的IfElseBlock

    private LinkedStack<BitSet> tempBranchStack = new LinkedStack<>();// 存储临时未归类的区间

    private LinkedStack<Integer> currentIfBlockEndStack = new LinkedStack<>();;// 存储当前ifBlock的结尾

    public boolean addBlock(IfElseBlock block) {
        return blockSet.add(block);
    }

    /**
     * 判断opcode是否在条件语句里面，返回集合
     * map的key是IfElseBlock，value是具体的blockIndex
     *
     * @param seen
     * @return
     */
    public Map<IfElseBlock, Map.Entry<Integer, BitSet>> inIfElseBlock(int seen) {
        // 遍历所有block，进行判断
        Map<IfElseBlock, Map.Entry<Integer, BitSet>> blockMap = new HashMap<>();
        for (IfElseBlock block : blockSet) {
            Map.Entry<Integer, BitSet> entry = block.inBlock(seen);
            if (entry != null) {
                blockMap.put(block, entry);
            }
        }
        return blockMap;
    }

    /**
     * 判断范围是否落在某个条件分支里面，并返回条件块和具体所在分支
     * @param branch
     * @return
     */
    public Map<IfElseBlock,Map.Entry<Integer,BitSet>> inIfElseBlock(BitSet branch) {
        Map<IfElseBlock,Map.Entry<Integer,BitSet>> map = new HashMap<>();
        for (IfElseBlock block : blockSet) {
            Map<Integer, BitSet> branchMap = block.getBranchMap();
            for (Map.Entry<Integer, BitSet> entry : branchMap.entrySet()) {
                BitSet range = entry.getValue();
                boolean include = include(range, branch);
                if (include) {
                    map.put(block, entry);
                }
            }
        }
        return map;
    }

    /**
     * 判断区域是否落在某个IfElseBlock里面，并返回符合要求的IfElseBlock
     * @param branchFallThrough
     * @param branchTarget
     * @return
     */
    public List<IfElseBlock> inIfElseBlock(int branchFallThrough, int branchTarget) {
        BitSet target = new BitSet();
        target.set(branchFallThrough, branchTarget);
        // 遍历所有block，进行判断
        List<IfElseBlock> blockList = new LinkedList<>();
        for (IfElseBlock block : blockSet) {
            BitSet wholeRange = block.getWholeRange();
            boolean include = include(wholeRange, target);
            if (include) {
                blockList.add(block);
            }
        }
        return blockList;
    }

    /**
     * 返回具体在哪个条件语句块当中
     *
     * @param seen
     * @return
     */
    public Map.Entry<IfElseBlock, Map.Entry<Integer, BitSet>> inBaseIfElseBlock(int seen) {
        Map<IfElseBlock, Map.Entry<Integer, BitSet>> blockEntryMap = inIfElseBlock(seen);

        Integer minRange = null;
        Map.Entry<IfElseBlock, Map.Entry<Integer, BitSet>> tempEntry = null;
        // 遍历最小范围的情况
        for (Map.Entry<IfElseBlock, Map.Entry<Integer, BitSet>> entry : blockEntryMap.entrySet()) {
            IfElseBlock block = entry.getKey();
            Map.Entry<Integer, BitSet> indexRangeEntry = entry.getValue();
            if (minRange == null) {
                minRange = indexRangeEntry.getValue().length();
                tempEntry = entry;
            } else {
                int tempRange = indexRangeEntry.getValue().length();
                if (minRange > tempRange) {
                    minRange = tempRange;
                    tempEntry = entry;
                }
            }
        }

        return tempEntry;
    }

    /*public IfElseBlock getFamily(int branchFallThrough, int branchTarget) {
        List<IfElseBlock> blockList = inIfElseBlock(branchFallThrough, branchTarget);
        Integer minRange = null;
        IfElseBlock tempBlock = null;
        for (IfElseBlock block : blockList) {
            int tempRange = block.getWholeRange().length();
            if(minRange == null){
                minRange = tempRange;
                tempBlock = block;
            }else {
                if(minRange > tempRange){
                    minRange = tempRange;
                    tempBlock = block;
                }
            }
        }
        // 需要再加一个防误报
        if (tempBlock != null) {
            boolean inBranch = tempBlock.inBranch(branchFallThrough, branchTarget);
            // 还要加一个条件：goto不和tempBlock的边界一样
            // branchTarget前一条opcode就是goto
            boolean gotoEqual = isGotoEqual(branchTarget, tempBlock);

            if (inBranch&&!gotoEqual) {
                tempBlock = null;
            }
        }
        return tempBlock;
    }*/

    public boolean isGotoEqual(int branchTarget, IfElseBlock tempBlock){
        return false;
    };

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

    /**
     * 寻找当前块是否已经有所属组织
     * @param blockEnd block结束点
     * @param currentBlockLevel block层级
     * @return
     */
    public IfElseBlock getFamily(int blockEnd, int currentBlockLevel) {
        for (IfElseBlock block : blockSet) {
            if(block.getElseBlockEnd() == blockEnd&&block.getBlockLevel() == currentBlockLevel){
                return block;
            }
        }
        return null;
    }

    /**
     * 获得所有bracn在else分支里的IfElseBlock
     * @param branch
     * @return
     */
    public List<IfElseBlock> inElseBranch(BitSet branch){
        List<IfElseBlock> list = new LinkedList<>();
        for (IfElseBlock block : blockSet) {
            BitSet elseBranch = block.getElseBlock();
            boolean include = include(elseBranch, branch);
            if (include) {
                list.add(block);
            }
        }
        return list;
    }

    /**
     * 判断范围是否在一个基础else块里面
     * @param branch
     * @return
     */
    public boolean inBaseElseBranch(BitSet branch) {
        Map<IfElseBlock, Map.Entry<Integer, BitSet>> map = inIfElseBlock(branch);
        // 获取最小的所在分支，并判断改分支是否是else分支，如果是返回true
        Integer blockIndex = null;
        Integer minCardinality = null;
        for (Map.Entry<Integer, BitSet> entry : map.values()) {
            BitSet bitSet = entry.getValue();
            Integer index = entry.getKey();
            int cardinality = bitSet.cardinality();
            if (minCardinality == null) {
                blockIndex = index;
                minCardinality = cardinality;
            }else {
                if(minCardinality > cardinality){
                    blockIndex = index;
                    minCardinality = cardinality;
                }
            }
        }
        if(blockIndex != null&&blockIndex == 0){
            return true;
        }
        return false;
    }

    /**
     * 解决层级误判：如果实际上没有elseBranch，则最后一个branch里面所有的IfElseBlock层级-1
     * @param block
     */
    public void checkFalsePositive(IfElseBlock block) {
        BitSet elseBlock = block.getElseBlock();
        // elseBlock为空，需要检查误判
        if(elseBlock.cardinality() == 0){
            // 获得elseBlock上面一个ifBlock里面所有的IfElseBlock，将其层级-1
            TreeMap<Integer, BitSet> branchMap = block.getBranchMap();
            Integer lastIfBlockIndex = branchMap.lastKey();
            BitSet lastIfBlock = branchMap.get(lastIfBlockIndex);
            List<IfElseBlock> list = listIfElseBlockInRange(lastIfBlock);
            for (IfElseBlock ifElseBlock : list) {
                ifElseBlock.setBlockLevel(ifElseBlock.getBlockLevel()-1);
            }
        }
    }

    public List<IfElseBlock> listIfElseBlockInRange(BitSet range) {
        List<IfElseBlock> list = new LinkedList<>();
        for (IfElseBlock block : blockSet) {
            BitSet wholeRange = block.getWholeRange();
            boolean include = include(range, wholeRange);
            if (include) {
                list.add(block);
            }
        }
        return list;
    }

    public IfElseBlock inBaseIfElseBlock(BitSet branch) {
        Map<IfElseBlock, Map.Entry<Integer, BitSet>> map = inIfElseBlock(branch);
        Integer minCardinality = null;
        IfElseBlock tempBlock = null;
        BitSet tempRange = null;
        filterBlock(map, minCardinality, tempBlock, tempRange);
        return tempBlock;
    }

    private void filterBlock(Map<IfElseBlock, Map.Entry<Integer, BitSet>> map, Integer minCardinality,
                                       IfElseBlock tempBlock, BitSet tempRange) {
        for (Map.Entry<IfElseBlock, Map.Entry<Integer, BitSet>> entry : map.entrySet()) {
            IfElseBlock ifElseBlock = entry.getKey();
            Map.Entry<Integer, BitSet> bitSetEntry = entry.getValue();
            BitSet range = bitSetEntry.getValue();
            int cardinality = range.cardinality();
            if(minCardinality == null){
                minCardinality = cardinality;
                tempBlock = ifElseBlock;
                tempRange = range;
            }else {
                if(minCardinality > cardinality){
                    minCardinality = cardinality;
                    tempBlock = ifElseBlock;
                    tempRange = range;
                }
            }
        }
    }

    public BitSet getBaseElseBranch(BitSet range) {
        Map<IfElseBlock, Map.Entry<Integer, BitSet>> map = inIfElseBlock(range);
        Integer minCardinality = null;
        IfElseBlock tempBlock = null;
        BitSet tempRange = null;
        filterBlock(map, minCardinality, tempBlock, tempRange);
        return tempRange;
    }

    public LinkedStack<BitSet> getTempBranchStack() {
        return tempBranchStack;
    }

    public LinkedStack<Integer> getCurrentIfBlockEndStack() {
        return currentIfBlockEndStack;
    }
}
