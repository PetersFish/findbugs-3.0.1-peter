package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.AnalysisCacheToRepositoryAdapter;
import edu.umd.cs.findbugs.ba.ObjectTypeFactory;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.util.SyntheticRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表资源的类
 * @author Peter Yu
 * @date 2018/6/6 18:50
 */
public class Resource {

    /**
     * 资源的类名
     */
    private final @DottedClassName String className;

    /**
     * 资源的类型，相当于java里面的Class
     */
    private final ObjectType type;

    /**
     * 开启资源的操作
     */
    private final Set<ResourceOperation> addMethodSet;

    /**
     * 关闭资源的操作
     */
    private final Set<ResourceOperation> delMethodSet;

    public Resource(String className) {
        this.className = className.replaceAll("/",".");
        this.type = ObjectTypeFactory.getInstance(className);
        this.addMethodSet = new HashSet<>();
        this.delMethodSet = new HashSet<>();
    }

    /**
     * 添加开启资源的操作
     * @param operation 资源操作
     * @return 成功与否的标记
     */
    public boolean appendAddMethod(ResourceOperation operation){
        return addMethodSet.add(operation);
    }

    /**
     * 添加资源关闭的操作
     * @param operation 资源操作
     * @return 成功与否的标记
     */
    public boolean appendDelMethod(ResourceOperation operation){
        return delMethodSet.add(operation);
    }

    public Set<ResourceOperation> getAddMethodSet(){
        return addMethodSet;
    }

    public Set<ResourceOperation> getDelMethodSet() {
        return delMethodSet;
    }

    public String getClassName() {
        return className;
    }

    public ObjectType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null||! (obj instanceof Resource)){
            return false;
        }
        Resource re = (Resource) obj;
        return this.className.equals(re.getClassName());
    }

    @Override
    public String toString() {
        return "Resource{" +
               "className='" + className + '\'' +
               '}';
    }
}
