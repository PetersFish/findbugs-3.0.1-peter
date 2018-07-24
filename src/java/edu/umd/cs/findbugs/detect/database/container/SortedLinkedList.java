package edu.umd.cs.findbugs.detect.database.container;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 存放实例按照升序或逆序排列
 * @author Peter Yu 2018/7/17 14:23
 */
public class SortedLinkedList<T> {
    private Comparator<T> comparator;

    private LinkedList<T> list = new LinkedList<>();

    private boolean accend = true;

    public SortedLinkedList() {}

    public SortedLinkedList(Comparator<T> comparator){
        this.comparator = comparator;
    }

    public SortedLinkedList(boolean accend) {
        this.accend = accend;
    }

    public boolean add(T target){

        if(accend){
            if(list.isEmpty()){
                return list.add(target);
            }else {
                if(comparator != null){
                    for (int i = list.size()-1; i >= 0; i--) {
                        if(comparator.compare(target,list.get(i)) < 0){
                            if(i == 0){
                                list.add(i,target);
                                return true;
                            }
                            continue;
                        }else {
                            list.add(i+1,target);
                            return true;
                        }

                    }
                }else if(target instanceof Comparable) {
                    Comparable comparable = (Comparable) target;
                    for (int i = list.size()-1; i >= 0; i--) {

                        if(comparable.compareTo(list.get(i)) < 0){
                            if(i == 0){
                                list.add(i,target);
                                return true;
                            }
                            continue;
                        }else {
                            list.add(i+1,target);
                            return true;
                        }

                    }
                }
            }
        }else {
            if(list.isEmpty()){
                return list.add(target);
            }else {
                if(comparator != null){
                    for (int i = 0; i < list.size(); i++) {
                        if(comparator.compare(target,list.get(i)) >= 0){
                            list.add(i,target);
                            return true;
                        }else if(i == list.size()-1){
                            list.add(i+1,target);
                            return true;
                        }
                    }
                }else if(target instanceof Comparable){
                    Comparable comparable = (Comparable) target;
                    for (int i = 0; i < list.size(); i++) {
                        if(comparable.compareTo(list.get(i)) >= 0){
                            list.add(i,target);
                            return true;
                        }else if(i == list.size()-1){
                            list.add(i+1,target);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public T getLast(){
        if(list.size() == 0){
            return null;
        }
        return list.getLast();
    }

    public T getFirst(){
        if(list.size() == 0){
            return null;
        }
        return list.getFirst();
    }

    public T pollLast(){
        if(list.size() == 0){
            return null;
        }
        return list.pollLast();
    }

    public Iterator<T> iterator(){
        return list.iterator();
    }

    public int hasElement(T t) {
        if(list.size() == 0){
            return -1;
        }

        for (int i = list.size()-1; i >= 0; i--) {
            if(t instanceof Comparable){
                Comparable comparable = (Comparable) t;
                if(comparable.compareTo(list.get(i)) == 0){
                    return i;
                }
            }
        }
        return -1;
    }

    public T remove(int index){
        return list.remove(index);
    }

    public boolean isAccend() {
        return accend;
    }

    public void setAccend(boolean accend) {
        this.accend = accend;
    }

    public int size(){
        return list.size();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
