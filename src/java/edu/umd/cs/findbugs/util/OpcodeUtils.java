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

    private static final BitSet returnInstructionSet = new BitSet();

    private static final BitSet gotoInstructionSet = new BitSet();

    private static final BitSet astoreSet = new BitSet();

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

        returnInstructionSet.set(Constants.IRETURN);
        returnInstructionSet.set(Constants.LRETURN);
        returnInstructionSet.set(Constants.FRETURN);
        returnInstructionSet.set(Constants.DRETURN);
        returnInstructionSet.set(Constants.ARETURN);
        returnInstructionSet.set(Constants.RETURN);

        gotoInstructionSet.set(Constants.GOTO);
        gotoInstructionSet.set(Constants.GOTO_W);

        astoreSet.set(Constants.ASTORE);
        astoreSet.set(Constants.ASTORE_0);
        astoreSet.set(Constants.ASTORE_1);
        astoreSet.set(Constants.ASTORE_2);
        astoreSet.set(Constants.ASTORE_3);
    }


    public static boolean isIfInstruction(int opcode){
        return ifInstructionSet.get(opcode);
    }

    public static boolean isLoad(int opcode){
        return opcode == ALOAD
               || opcode == ALOAD_0
               || opcode == ALOAD_1
               || opcode == ALOAD_2
               || opcode == ALOAD_3;
    }

    public static boolean isInvoke(int opcode) {
        return opcode == INVOKEVIRTUAL
               || opcode == INVOKESPECIAL
               || opcode == INVOKESTATIC
               || opcode == INVOKEINTERFACE;
    }

    public static boolean isReturn(int opcode) {
        return returnInstructionSet.get(opcode);
    }

    public static boolean isGoto(int opcode) {
        return gotoInstructionSet.get(opcode);
    }

    public static boolean isStore(int opcode){
        return astoreSet.get(opcode);
    }
}
