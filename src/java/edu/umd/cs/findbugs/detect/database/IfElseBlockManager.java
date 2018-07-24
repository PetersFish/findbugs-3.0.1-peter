package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.database.container.BitSetBuffer;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Peter Yu 2018/7/18 11:41
 */
public class IfElseBlockManager {

    private LinkedList<IfElseBlock> blockList = new LinkedList<>();

    public LinkedList<IfElseBlock> getBlockList() {
        return blockList;
    }

    public IfElseBlock getParent(IfElseBranch branch) {
        // 遍历所有IfElseBlock2，找到匹配的
        for (IfElseBlock block : blockList) {
            boolean flag = block.compatible(branch);
            if (flag) {
                return block;
            }
        }
        return null;
    }

    public boolean addBlock(IfElseBlock block) {
        return blockList.add(block);
    }

    /**
     * 通过分支结尾位置获取相应分支
     * @param pc
     * @return
     */
    public LinkedList<IfElseBranch> getBranchByBranchEnd(int pc) {

        LinkedList<IfElseBranch> branches = new LinkedList<>();
        for (IfElseBlock block : blockList) {
            TreeSet<IfElseBranch> branchList = block.getBranchSet();
            for (IfElseBranch branch : branchList) {
                if(branch.getBranchEnd().equals(pc)){
                    branches.add(branch);
                }
            }
        }
        return branches;
    }

    /**
     * 将branch信息加入到资源实例当中
     * @param resourceInstance
     */
    public void injectBranchInfo(ResourceInstance resourceInstance) {

        Integer pc = resourceInstance.getPc();
        if (pc == null) {
            return;
        }

        // 获取pc所在的最小branch，将其放入实例
        IfElseBranch minBranch = null;
        for (IfElseBlock block : blockList) {

            TreeSet<IfElseBranch> branchSet = block.getBranchSet();
            IfElseBranch last = branchSet.last();
            // 保证pc在范围内，不然就不需要遍历，找到了就break，不用再遍历
            if(last.getWholeRange().get(pc)){

                BitSetBuffer minRange = null;
                for (IfElseBranch ifElseBranch : branchSet) {

                    BitSetBuffer tempRange = ifElseBranch.inRange(pc);
                    if (tempRange != null) {
                        if (minRange == null) {
                            minRange = tempRange;
                            minBranch = ifElseBranch;
                        }else {
                            // 如果minRange的起始位置比tempRange小，说明minRange的范围要大，需更新
                            if(minRange.getStart() < tempRange.getStart()){
                                minRange = tempRange;
                                minBranch = ifElseBranch;
                            }
                        }
                    }
                }
                break;
            }
        }

        resourceInstance.setBranch(minBranch);
    }

    /**
     * 获得pc所在的所有条件分支
     * @param closePc
     * @return
     */
    public TreeMap<BitSetBuffer, Integer> getExistRanges(Integer closePc) {
        IfElseBlock parent = getParent(closePc);
        if (parent == null) {
            return null;
        }

        return parent.getHosts(closePc);
    }

    /**
     * 获取pc所在的IfElseBlock
     * @param closePc
     * @return
     */
    private IfElseBlock getParent(Integer closePc) {
        // 遍历集合，找到所属的block
        for (IfElseBlock block : blockList) {
            TreeSet<IfElseBranch> branchSet = block.getBranchSet();
            for (IfElseBranch branch : branchSet) {
                BitSetBuffer wholeRange = branch.getWholeRange();
                if (wholeRange.get(closePc)) {
                    return block;
                }
            }
        }
        return null;
    }
}
