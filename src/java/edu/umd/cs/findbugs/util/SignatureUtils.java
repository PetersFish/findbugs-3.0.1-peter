package edu.umd.cs.findbugs.util;

import edu.umd.cs.findbugs.ba.SignatureParser;

/**
 * @author Peter Yu
 * @date 2018/6/8 17:44
 */
public class SignatureUtils {

    private SignatureUtils(){}

    private static final String OBJET_TAG = "L";

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
        SignatureParser parser = new SignatureParser(signature);
        String parameter = parser.getParameter(0);
        return parameter.startsWith("L");
    }

    public static boolean isSingleParam(String signature){
        SignatureParser parser = new SignatureParser(signature);
        int numParameters = parser.getNumParameters();
        if(numParameters == 1){
            return true;
        }
        return false;
    }

    public static String getObjectParamClassName(String signature){
        if(isSingleParam(signature)&&isObjectParam(signature)){
            SignatureParser parser = new SignatureParser(signature);
            String parameter = parser.getParameter(0);
            return parameter.substring(1,parameter.length()-1).replace("/",".");
        }
        return null;
    }

    public static String trimArgument(String arg){
        if (arg == null) {
            return null;
        }
        if(!arg.startsWith(OBJET_TAG)){
            return null;
        }
        return arg.substring(1,arg.length()-1).replace("/",".");
    }

    public static String getReturnType(String signature){
        SignatureParser parser = new SignatureParser(signature);
        return parser.getReturnTypeSignature();
    }

    public static String getObjectReturnTypeClassName(String signature) {
        SignatureParser parser = new SignatureParser(signature);
        String returnTypeSignature = parser.getReturnTypeSignature();
        if(returnTypeSignature.startsWith("L")){
            return returnTypeSignature.substring(1, returnTypeSignature.length() - 1).replace("/",".");
        }
        return null;
    }
}
