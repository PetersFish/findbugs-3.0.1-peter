package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.AnalysisCacheToRepositoryAdapter;
import edu.umd.cs.findbugs.bcel.BCELUtil;
import edu.umd.cs.findbugs.detect.BadResourceCheck;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;

import java.util.Set;
import java.util.logging.Logger;

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

    private static final AnalysisCacheToRepositoryAdapter adapter = new AnalysisCacheToRepositoryAdapter();

    private static final Logger LOGGER = Logger.getLogger(BadResourceCheck.class.getName());

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
        Resource resource = new Resource(className);
        JavaClass aClass = adapter.findClass(className);
        if (aClass == null) {
            return false;
        }

        if(resourceSet.contains(resource)){
            return true;
        }
        for (Resource r : resourceSet) {
            try {
                JavaClass tempClass = adapter.findClass(r.getClassName());
//                LOGGER.warning("aClass:"+aClass+":"+className);
//                LOGGER.warning("tempClass:"+tempClass+":"+r.getClassName());
                if (tempClass == null) {
                    continue;
                }
                if(tempClass.instanceOf(aClass)||aClass.instanceOf(tempClass)){
                    return true;
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warning(e.toString());
            }
        }
        return false;
    }

    public Resource resourceMatched(){
        Set<Resource> resourceSet = ResourceFactory.listResources();
        Resource resource = new Resource(className);
        JavaClass aClass = adapter.findClass(className);
        if(resourceSet.contains(resource)){
            return resource;
        }
        try {
            for (Resource r : resourceSet) {
                JavaClass tempClass = adapter.findClass(r.getClassName());
                if (tempClass == null) {
                    return null;
                }
                if(tempClass.instanceOf(aClass)||aClass.instanceOf(tempClass)){
                    return r;
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warning(e.toString());
        } catch (Exception e){
            LOGGER.warning(e.toString());
        }
        return null;
    }
}
