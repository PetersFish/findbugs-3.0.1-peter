package edu.umd.cs.findbugs.detect.database;

import java.util.*;

/**
 * 用于捕获资源实例，当valve打开时（true），变量才存放成功
 *
 * @author Peter Yu
 * @date 2018/6/8 9:25
 */
public class ResourceInstanceCapturer {

    private LinkedList<ResourceInstance> instanceList = new LinkedList<>();

    private boolean valve = false;

    public void setValve(boolean valve) {
        this.valve = valve;
    }

    public boolean isValve() {
        return valve;
    }

    /**
     * 添加资源变量，仅在指令被确定为是开启资源的时候调用
     *
     * @param instance 资源变量
     * @return
     */
    public boolean addInstance(ResourceInstance instance) {
        valve = true;
        return instanceList.add(instance);
    }

    /**
     * 给最后添加进来的资源变量设置stackIndex，只有valve为true的时候才能设置成功，
     * 设置完毕以后重新关闭valve（false）
     *
     * @param registerOperand
     * @return
     */
    public boolean addStackIndex(Integer registerOperand) {
        if (valve) {
            ResourceInstance lastInstance = instanceList.getLast();
            lastInstance.setStackIndex(registerOperand);
            ResourceInstance set = instanceList.set(instanceList.size() - 1, lastInstance);
            valve = false;
            return true;
        }
        return false;
    }

    public boolean isInstanceExist(Integer registerOperand) {
        for (ResourceInstance resourceInstance : instanceList) {
            if (resourceInstance.getStackIndex() == registerOperand) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据statckInedx删除变量实例
     *
     * @param registerOperand
     * @param location
     * @param blockManager
     * @return
     */
    public boolean removeInstance(Integer registerOperand, IfElseBlockLocation location,
                                  IfElseBlockManager blockManager) {
        // 删除动作前，先将实例的blockSet更新一遍
        for (ResourceInstance instance : instanceList) {
            Integer pc = instance.getPc();
            BitSet range = new BitSet();
            range.set(pc);
            Map<IfElseBlock, Map.Entry<Integer, BitSet>> map = blockManager.inIfElseBlock(range);
            for (Map.Entry<IfElseBlock, Map.Entry<Integer, BitSet>> entry : map.entrySet()) {
                IfElseBlockLocation blockLocation = new IfElseBlockLocation(entry.getKey(),
                                                                            entry.getValue().getKey(),
                                                                            entry.getValue().getValue());
                instance.addInBlock(blockLocation);
            }
        }

        // 遍历所有的resourceInstance，如果其stackIndex==registerOperand，则将其从集合中删除，
        // 如果没有遍历到，则返回false
        boolean flag = false;
        for (Iterator<ResourceInstance> iterator = instanceList.descendingIterator(); iterator.hasNext(); ) {
            ResourceInstance instance = iterator.next();
            Integer pc = instance.getPc();
            // 只删除范围内的实例
            IfElseBlock block = location.getBlock();
            BitSet removeRange = null;
            if (block != null) {
                removeRange = block.getWholeRange();
            }
            if (removeRange == null || removeRange.get(pc)) {
                Integer stackIndex = instance.getStackIndex();
                if (registerOperand == stackIndex) {
                    // 如果能够删除，才能进行删除
                    if (!instance.isRemoveForbidden()) {
                        iterator.remove();
                        notifyFriends(instance, blockManager);
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    // 通知朋友我被删除了，你们不用被删除了
    private void notifyFriends(ResourceInstance resourceInstance, IfElseBlockManager blockManager) {
        Integer pc = resourceInstance.getPc();
        Map.Entry<IfElseBlock, Map.Entry<Integer, BitSet>> entry = blockManager.inBaseIfElseBlock(pc);

        // 如果entry为null，说明删除的实例在mainBlock里面，所有其他的实例都不可以删除
        if (entry == null) {
            for (ResourceInstance instance : instanceList) {
                instance.setRemoveForbidden(true);
            }
        }
        // 如果entry不为null，则锁定被删istance所在的的基础分支，将里面的所有实例设为不可删除
        else {
            BitSet baseBranch = entry.getValue().getValue();
            for (ResourceInstance instance : instanceList) {
                Integer instancePc = instance.getPc();
                if (baseBranch.get(instancePc)) {
                    instance.setRemoveForbidden(true);
                }
            }
        }
    }

    /**
     * 获取所有资源实例
     *
     * @return
     */
    public LinkedList<ResourceInstance> listResourceInstance() {
        return instanceList;
    }

    /**
     * 清空扫描记录
     */
    public void clear() {
        instanceList.clear();
    }
}
