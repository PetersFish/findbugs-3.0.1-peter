package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.util.SignatureUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 描述资源操作的Class name，name and type，以及操作类型，以及操作检查的迫切程度
 *
 * @author Peter Yu
 * @date 2018/6/6 17:43
 */
public class ResourceOperation {

    private final String clazzName;

    private final String methodName;

    private final String signature;

    public ResourceOperation(String clazzName, String methodName, String methodDescription) {
        this.clazzName = clazzName.replaceAll("/", ".");
        this.methodName = methodName;
        this.signature = methodDescription;
    }

    public boolean match(ResourceOperation operation) {
        String cName = operation.getClazzName();
        String mName = operation.getMethodName();
        String sig = operation.getSignature();

        return ClassMatcher.matches(clazzName, cName) && methodName.equals(mName) &&
               SignatureMatcher.matches(signature, sig);
    }

    public Resource getInvolvedResourceForOpenInvoke() {
        String className = SignatureUtils.getObjectReturnTypeClassName(signature);
        if (StringUtils.isNotBlank(className)) {
            return new Resource(className);
        }
        return null;
    }

    public Resource getInvolvedResourceForCloseInvoke() {
        String className = SignatureUtils.getObjectParamClassName(signature);
        if (StringUtils.isNotBlank(className)) {
            return new Resource(className);
        }
        return null;
    }

    public String getClazzName() {
        return clazzName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public int hashCode() {
        String[] arr = { clazzName, methodName, signature };
        int hash = 0;
        for (int i = 0; i < arr.length; i++) {
            hash = hash * 31 + arr[i].hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ResourceOperation)) {
            return false;
        }
        ResourceOperation operation = (ResourceOperation) obj;
        return this.clazzName.equals(operation.clazzName) && this.methodName.equals(operation.methodName)
               && this.signature.equals(operation.signature);
    }

    @Override
    public String toString() {
        return "ResourceOperation{" +
               "clazzName='" + clazzName + '\'' +
               ", methodName='" + methodName + '\'' +
               ", methodDescription='" + signature + '\'' +
               '}';
    }

}
