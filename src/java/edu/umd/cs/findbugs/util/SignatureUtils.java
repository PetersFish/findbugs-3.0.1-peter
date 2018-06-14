package edu.umd.cs.findbugs.util;

import edu.umd.cs.findbugs.ba.SignatureParser;

/**
 * @author Peter Yu
 * @date 2018/6/8 17:44
 */
public class SignatureUtils {

    private SignatureUtils(){}

    public static boolean isVoidReturnType(String signature){
        SignatureParser parser = new SignatureParser(signature);
        String returnTypeSignature = parser.getReturnTypeSignature();
        if("V".equals(returnTypeSignature)){
            return true;
        }
        return false;
    }

    public static boolean isObjectReturnType(String signature){
        SignatureParser parser = new SignatureParser(signature);
        String returnTypeSignature = parser.getReturnTypeSignature();
        return returnTypeSignature.startsWith("L");
    }

    public static boolean isObjectParam(String signature){
        return signature.startsWith("L");
    }

    public static String getObjectParamClassName(String signature){
        if(isObjectParam(signature)){
            return signature.substring(1,signature.length()-1);
        }
        return null;
    }

    public static String getObjectReturnTypeClassName(String signature) {
        SignatureParser parser = new SignatureParser(signature);
        String returnTypeSignature = parser.getReturnTypeSignature();
        if(returnTypeSignature.startsWith("L")){
            return returnTypeSignature.substring(1, returnTypeSignature.length() - 1);
        }
        return null;
    }
}
