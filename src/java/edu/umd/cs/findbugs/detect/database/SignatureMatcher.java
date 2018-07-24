package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.AnalysisCacheToRepositoryAdapter;
import edu.umd.cs.findbugs.detect.BadResourceCheck;
import edu.umd.cs.findbugs.util.SignatureUtils;

import java.util.logging.Logger;

/**
 * @author Peter Yu
 * @date 2018/6/20 20:40
 */
public class SignatureMatcher {

    public static boolean matches(String sig1, String sig2){
        String returnType1 = SignatureUtils.getReturnType(sig1);
        String returnType2 = SignatureUtils.getReturnType(sig2);
        if(returnType1.equals(returnType2)){
            return true;
        }
        String className1 = SignatureUtils.getObjectReturnTypeClassName(sig1);
        String className2 = SignatureUtils.getObjectReturnTypeClassName(sig2);
        if(className1 != null&&className2 != null&&ClassMatcher.matches(className1,className2)){
            return true;
        }
        return false;
    }
}
