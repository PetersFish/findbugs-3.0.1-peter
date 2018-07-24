package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.database.container.BitSetBuffer;

import java.util.BitSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Peter Yu 2018/7/18 11:27
 */
public class IfElseBlock {

    private TreeSet<IfElseBranch> branchSet = new TreeSet<>();

    public boolean addBranch(IfElseBranch branch) {

        return branchSet.add(branch);
    }

    // 判断外界新增的一个branch是否能够加入到里面来
    public boolean compatible(IfElseBranch branch) {
        // 有交集就可以加进来
        for (IfElseBranch ifElseBranch : branchSet) {
            BitSet tempWholeRange = ifElseBranch.getWholeRange();
            BitSet wholeRange = branch.getWholeRange();
            if (tempWholeRange.intersects(wholeRange)) {
                return true;
            }
        }
        return false;
    }

    public TreeSet<IfElseBranch> getBranchSet() {
        return branchSet;
    }

    /**
     * 获得pc所在的范围
     *
     * @param pc
     * @return
     */
    public TreeMap<BitSetBuffer, Integer> getHosts(Integer pc) {

        TreeMap<BitSetBuffer, Integer> treeMap = new TreeMap<>();
        for (IfElseBranch branch : branchSet) {

            Integer ifOpcode = branch.getIfOpcode();
            BitSetBuffer range = branch.getRange();
            BitSetBuffer exRange = branch.getExRange();
            if (range.get(pc)) {
                treeMap.put(range, ifOpcode);
            } else if (exRange.get(pc)) {
                treeMap.put(exRange, ifOpcode);
            }
        }
        return treeMap;
    }
}
