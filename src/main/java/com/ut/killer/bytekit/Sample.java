package com.ut.killer.bytekit;

public class Sample {
    private int exceptionCount = 0;

    public String hello(String str, boolean exception) {
        if (exception) {
            exceptionCount++;
            throw new RuntimeException("test exception, str: " + str);
        }
        Sample1 sample1 = new Sample1();
        return sample1.echo(str, false);
    }
}
