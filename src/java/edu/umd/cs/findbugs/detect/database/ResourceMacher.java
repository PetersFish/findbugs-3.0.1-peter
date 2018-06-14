package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.bcel.BCELUtil;
import org.apache.bcel.generic.ObjectType;

import java.util.Set;

/**
 * 判断资源是否匹配，方法是否可能为Open或Close资源操作
 * 作用于likeResourceOpenInvoke和likeResourceOpenInvoke里面
 *
 * @author Peter Yu
 * @date 2018/6/8 17:13
 */
public class ResourceMacher{

    private String className;

    private ObjectType type;

    public ResourceMacher(String className){
        this.className = className;
        this.type = BCELUtil.getObjectTypeInstance(className);
    }

    public static void main(String[] args) {
        String className = "java/io/OutputStream";
        ResourceMacher macher = new ResourceMacher(className);
        System.out.println(macher);
    }

    public boolean matches(){
        Set<Resource> resourceSet = ResourceFactory.listResources();
        for (Resource resource : resourceSet) {
            try {
                if(resource.getType().subclassOf(type)){
                    return true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
