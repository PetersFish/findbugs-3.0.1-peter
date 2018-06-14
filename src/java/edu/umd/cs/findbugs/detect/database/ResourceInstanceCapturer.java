package edu.umd.cs.findbugs.detect.database;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * 用于捕获资源实例，当valve打开时（true），变量才存放成功
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
     * @param instance 资源变量
     * @return
     */
    public boolean addInstance(ResourceInstance instance){
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
    public boolean addStackIndex(int registerOperand){
        if(valve){
            ResourceInstance lastInstance = instanceList.getLast();
            lastInstance.setStackIndex(registerOperand);
            ResourceInstance set = instanceList.set(instanceList.size() - 1, lastInstance);
            valve = false;
            return true;
        }
        return false;
    }

    public boolean isInstanceExist(int registerOperand){
        for (ResourceInstance resourceInstance : instanceList) {
            if(resourceInstance.getStackIndex() == registerOperand){
                return true;
            }
        }
        return false;
    }

    /**
     * 根据statckInedx删除变量实例
     * @param registerOperand
     * @return
     */
    public boolean removeInstance(int registerOperand){
        // 遍历所有的resourceInstance，如果其stackIndex==registerOperand，则将其从集合中删除，
        // 如果没有遍历到，则返回false
        for (Iterator<ResourceInstance> iterator = instanceList.iterator();iterator.hasNext();) {
            ResourceInstance resourceInstance = iterator.next();
            Integer stackIndex = resourceInstance.getStackIndex();
            if(registerOperand == stackIndex){
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有资源实例
     * @return
     */
    public LinkedList<ResourceInstance> listResourceInstance(){
        return instanceList;
    }

    /**
     * 清空扫描记录
     */
    public void clear(){
        instanceList.clear();
    }
}
