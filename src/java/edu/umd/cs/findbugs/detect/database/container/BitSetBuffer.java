package edu.umd.cs.findbugs.detect.database.container;

import java.util.BitSet;

/**
 * @author Peter Yu 2018/7/18 13:18
 */
public class BitSetBuffer extends BitSet implements Comparable<BitSetBuffer>{

    public Integer getStart(){
        return this.nextSetBit(0);
    }

    public Integer getEnd(){
        return this.length();
    }

    public boolean overlap(BitSetBuffer target) {
        if(this.intersects(target)){
            if(!this.includeEachOther(target)){
                return true;
            }
        }
        return false;
    }

    public boolean include(BitSetBuffer target) {
        BitSet tempSet = new BitSet();
        tempSet.or(this);
        tempSet.and(target);
        return tempSet.equals(target);
    }

    public boolean includeEachOther(BitSetBuffer target){
        return this.include(target)||target.include(this);
    }

    @Override
    public int compareTo(BitSetBuffer o) {
        int lengthMinus = this.length() - o.length();
        int cardMinus = this.cardinality() - o.cardinality();
        return (lengthMinus != 0)?lengthMinus:cardMinus;
    }
}
