package edu.umd.cs.findbugs.util;

import org.apache.bcel.Constants;

/**
 * @author Peter Yu
 * @date 2018/6/11 15:01
 */
public class OpcodeUtils implements Constants{

    private OpcodeUtils(){}

    public static boolean isLoad(int opcode){
        if(opcode == ALOAD||opcode == ALOAD_0||opcode == ALOAD_1||opcode == ALOAD_2||opcode == ALOAD_3){
            return true;
        }
        return false;
    }
}
