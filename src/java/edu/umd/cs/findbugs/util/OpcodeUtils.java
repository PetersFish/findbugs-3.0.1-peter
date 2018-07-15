package edu.umd.cs.findbugs.util;

import org.apache.bcel.Constants;

import java.util.BitSet;

/**
 * @author Peter Yu
 * @date 2018/6/11 15:01
 */
public class OpcodeUtils implements Constants{

    private OpcodeUtils(){}

    private static final BitSet ifInstructionSet = new BitSet();

    static {
        ifInstructionSet.set(Constants.IF_ACMPEQ);
        ifInstructionSet.set(Constants.IF_ACMPNE);
        ifInstructionSet.set(Constants.IF_ICMPEQ);
        ifInstructionSet.set(Constants.IF_ICMPNE);
        ifInstructionSet.set(Constants.IF_ICMPLT);
        ifInstructionSet.set(Constants.IF_ICMPLE);
        ifInstructionSet.set(Constants.IF_ICMPGT);
        ifInstructionSet.set(Constants.IF_ICMPGE);
        ifInstructionSet.set(Constants.IFEQ);
        ifInstructionSet.set(Constants.IFNE);
        ifInstructionSet.set(Constants.IFLT);
        ifInstructionSet.set(Constants.IFLE);
        ifInstructionSet.set(Constants.IFGT);
        ifInstructionSet.set(Constants.IFGE);
        ifInstructionSet.set(Constants.IFNULL);
        ifInstructionSet.set(Constants.IFNONNULL);
    }


    public static boolean isIfInstruction(int opcode){
        return ifInstructionSet.get(opcode);
    }

    public static boolean isLoad(int opcode){
        if(opcode == ALOAD||opcode == ALOAD_0||opcode == ALOAD_1||opcode == ALOAD_2||opcode == ALOAD_3){
            return true;
        }
        return false;
    }

    public static boolean isInvoke(int opcode) {
        if(opcode == INVOKEVIRTUAL||opcode == INVOKESPECIAL||opcode == INVOKESTATIC||opcode == INVOKEINTERFACE){
            return true;
        }
        return false;
    }
}
