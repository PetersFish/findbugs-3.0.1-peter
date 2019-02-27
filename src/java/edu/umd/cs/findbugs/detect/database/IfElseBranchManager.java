package edu.umd.cs.findbugs.detect.database;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Peter Yu 2018/7/17 17:51
 */
public class IfElseBranchManager {

    private LinkedList<IfElseBranch> branchList;

    {
        branchList = new LinkedList<>();
        // 初始化，加入一个空Branch，作为main里面的范围
        IfElseBranch branch = new IfElseBranch();
        branchList.add(branch);
    }

    public boolean addBranch(IfElseBranch block){
        return branchList.add(block);
    }

    public LinkedList<IfElseBranch> getBranchList() {
        return branchList;
    }

    public LinkedList<IfElseBranch> getBranchByBranchEnd(int pc){
        LinkedList<IfElseBranch> list = new LinkedList<>();
        for (Iterator<IfElseBranch> iterator = branchList.iterator(); iterator.hasNext();) {
            IfElseBranch branch = iterator.next();
            Integer branchEnd = branch.getBranchEnd();
            if (branchEnd == null) {
                continue;
            }
            if(pc == branchEnd){
                list.add(branch);
            }
        }
        return list;
    }

    public boolean removeBranch(IfElseBranch branch){
        return branchList.remove(branch);
    }

}
