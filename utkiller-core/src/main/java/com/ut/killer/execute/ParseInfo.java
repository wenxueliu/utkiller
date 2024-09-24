package com.ut.killer.execute;

public class ParseInfo {
    String value;
    int newIndex;

    public ParseInfo(String value, int newIndex) {
        this.value = value;
        this.newIndex = newIndex;
    }

    public String getValue() {
        return value;
    }

    public int getNewIndex() {
        return newIndex;
    }
}
