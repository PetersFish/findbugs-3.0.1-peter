package edu.umd.cs.findbugs.detect.database.container;

/**
 * @author Peter Yu 2018/7/19 15:27
 */
public interface Entry<K, V> extends Comparable<Entry<K, V>> {

    K getKey();

    V getValue();

    void setVlaue(V value);

    boolean equals(Entry o);

    int hashCode();
}
