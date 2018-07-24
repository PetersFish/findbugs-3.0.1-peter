package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.detect.database.container.BitSetBuffer;

import java.util.*;
import java.util.logging.Logger;

/**
 * 用于捕获资源实例，当valve打开时（true），变量才存放成功
 *
 * @author Peter Yu
 * @date 2018/6/8 9:25
 */
public class ResourceInstanceCapturer {

    private static final Logger logger = Logger.getLogger(ResourceInstanceCapturer.class.getClass().getName());

    private LinkedList<ResourceInstance> instanceList = new LinkedList<>();

    private HashMap<Integer,ResourceInstance> lastClosedMap = new HashMap<>();

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
            if(instanceList.size() == 0){
                return false;
            }
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
     * 关闭资源
     *
     * @return
     */
    public boolean removeInstance(Integer registerOperand, BitSetBuffer closeRange) {
        boolean flag = false;
        // 获取pc的作用范围

//        System.out.println("instancelist size:"+instanceList.size());
//        System.out.println(instanceList);
        for (Iterator<ResourceInstance> iterator = instanceList.descendingIterator(); iterator.hasNext(); ) {

            ResourceInstance instance = iterator.next();
            Integer pc = instance.getPc();

//            System.out.println("*Check resource closeablity*");

            // 不在关闭作用范围内的，跳过
            if (!closeRange.isEmpty()&&!closeRange.get(pc)) {
//                System.out.println("----- Resource not in closeRange, skip close! -----");
                continue;
            }

            // 删除操作数为registerOperand的资源实例

            if(registerOperand.equals(instance.getStackIndex())){

                ResourceInstance lastClosed = lastClosedMap.get(registerOperand);

                if (lastClosed == null) {
//                    System.out.println("----- Fisrt resource, resource being closed -----");
                    iterator.remove();
                    lastClosed = instance;
                    lastClosedMap.put(registerOperand,lastClosed);
                    flag = true;
                    continue;
                }

                // 如果当前资源的pc比关掉的资源pc大，则可以被删除
                if (instance.getPc() > lastClosed.getPc()) {
//                    System.out.println("----- resource pc > lastClosed pc, being closed -----");
                    iterator.remove();
                    lastClosed = instance;
                    lastClosedMap.put(registerOperand,lastClosed);
                    flag = true;
                    continue;
                }

                // 判断互斥：互相包含，没有goto（或goto不一致）
                IfElseBranch tempBranch = instance.getBranch();
                if (tempBranch == null) {
//                    System.out.println("----- resource in main block and has lastClosed, skip close! -----");
                    // 资源关不掉
                    continue;
                }
                Integer tempGotoTarget = tempBranch.getGotoTarget();

                IfElseBranch closedBranch = lastClosed.getBranch();
                if (closedBranch == null) {
//                    System.out.println("----- lastClosed resource exist in main block, skip close! -----");
                    // 资源关不掉
                    continue;
                }
                Integer closedGotoTarget = closedBranch.getGotoTarget();

                // 目标资源在closed资源的范围里面，且goto不相等或为null，则互斥
                BitSetBuffer closedRange = lastClosed.getClosedRange();
                BitSetBuffer toCloseRange = instance.getClosedRange();

                // 如果在完全同一个区域，则互斥
                if (toCloseRange.equals(closedRange)) {
//                    System.out.println("----- resource and lastClosed have the same closeRange, skip close! -----");
                    continue;
                }

                // 不是同一个区域，但是有交集
                if (closedRange.get(pc)) {
                    if (tempGotoTarget == null || closedGotoTarget == null ||
                        !tempGotoTarget.equals(closedGotoTarget)) {
//                        System.out.println("----- resource is in lastClosed  closeRange and goto not qualified, skip close! -----");
                        // 资源关不掉
                        continue;
                    }
                }

                iterator.remove();
                lastClosed = instance;
                lastClosedMap.put(registerOperand,lastClosed);
                flag = true;
//                System.out.println("----- passed all filters, resource being closed -----");
            }
        }
        return flag;
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
