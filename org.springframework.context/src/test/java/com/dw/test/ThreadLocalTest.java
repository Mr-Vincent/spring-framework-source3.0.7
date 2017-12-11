package com.dw.test;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalTest {

    private ThreadLocal<List> localList = new ThreadLocal<List>();

    public List invoke(){
        List list = localList.get();
        if (list == null) {
            localList.set(new ArrayList());
        }
        return localList.get();
    }

    public static void main(String[] args) {

        ThreadLocalTest threadLocalTest = new ThreadLocalTest();
        for (int i = 0; i < 4; i++) {
            List list = threadLocalTest.invoke();
            System.out.println(list);
        }

    }
}
