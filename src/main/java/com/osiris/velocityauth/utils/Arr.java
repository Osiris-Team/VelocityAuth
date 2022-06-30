package com.osiris.velocityauth.utils;

/**
 * Generic array wrapper class,
 * to provided additional useful functionality
 * for arrays.
 */
public class Arr<T>{
    Object[] content;
    public int length;

    public Arr(int size) {
        this.content = new Object[size];
        this.length = size;
    }

    public Arr(T[] content) {
        this.content = content;
        this.length = content.length;
    }

    public Arr<T> set(int i, T obj){
        content[i] = obj;
        return this;
    }

    /**
     * Returns null instead of throwing {@link IndexOutOfBoundsException}.
     */
    public T get(int i){
        try{
            return (T) content[i];
        } catch (Exception ignored) {}
        return null;
    }

    public String toPrintString(){
        return toPrintString(0, length-1);
    }
    public String toPrintString(int startIndex, int endIndex){
        return toPrintString(" ", startIndex, endIndex);
    }
    public String toPrintString(String separator, int startIndex, int endIndex){
        StringBuilder s = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            s.append(get(i));
            s.append(separator);
        }
        return s.toString();
    }
}
