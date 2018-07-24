package edu.umd.cs.findbugs.detect.database.container;

/**
 * @author Peter Yu 2018/7/19 15:30
 */
public class BranchEntry implements Entry<BitSetBuffer,Integer> {

    private BitSetBuffer key;

    private Integer value;

    public BranchEntry(BitSetBuffer key, Integer value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public BitSetBuffer getKey() {
        return key;
    }

    @Override
    public Integer getValue() {
        return value;
    }


    public void setVlaue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Entry o) {
        return this.key.equals(o.getKey())&&this.value.equals(o.getValue());
    }

    @Override
    public int compareTo(Entry<BitSetBuffer, Integer> o) {
        return this.key.compareTo(o.getKey());
    }
}
