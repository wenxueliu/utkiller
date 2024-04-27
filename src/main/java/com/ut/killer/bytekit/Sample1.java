package com.ut.killer.bytekit;

public class Sample1 {
    public Sample1() {
    }
    public String echo(String str, boolean exception) {
        if (exception) {
            throw new RuntimeException("test exception, str: " + str);
        }
        return "hello " + str;
    }
}
