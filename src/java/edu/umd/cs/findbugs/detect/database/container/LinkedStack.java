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

    public T peek(){
        if(linkedList.size() == 0){
            return null;
        }
        return linkedList.getLast();
    }

    public T pop(){
        if(linkedList.size() == 0){
            return null;
        }
        return linkedList.removeLast();
    }

    public T peekBottom(){
        if(linkedList.size() == 0){
            return null;
        }
        return linkedList.getFirst();
    }

    public Integer size(){
        return linkedList.size();
    }


    @Override
    public String toString() {
        return linkedList.toString();
    }
}
