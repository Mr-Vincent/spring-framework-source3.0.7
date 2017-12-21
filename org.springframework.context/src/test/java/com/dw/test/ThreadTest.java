package com.dw.test;

public class ThreadTest extends Thread {
    @Override
    public void run() {
        try {
            sleep(50000);  // 延迟50秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        Thread thread = new ThreadTest();
        thread.start();
        System.out.println("在50秒之内按任意键中断线程!");
        System.in.read();
        // 如果线程处于阻塞状态，那么使用interrupt会抛异常
        thread.interrupt();
        // join就是等待这个线程结束 才继续执行
        thread.join();
        System.out.println("线程已经退出!");
    }
}
