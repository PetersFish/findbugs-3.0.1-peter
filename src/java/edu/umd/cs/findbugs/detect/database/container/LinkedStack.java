package edu.umd.cs.findbugs.detect.database.container;

import java.util.LinkedList;

/**
 * @author Peter Yu
 * @date 2018/6/10 17:30
 */
public class LinkedStack<T> {
    private LinkedList<T> linkedList = new LinkedList<>();

    public boolean push(T t){
        return linkedList.add(t);
    }

    public T pop(){
        if(linkedList.size() == 0){
            return null;
        }
        return linkedList.removeLast();
    }
}
