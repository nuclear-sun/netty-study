package org.sun.herostory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {

            private final AtomicInteger ac = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Pool-" + ac.getAndIncrement());
                return thread;
            }
        });

        executorService.execute(new MyTask("1"));
        executorService.execute(new MyTask("2"));
        executorService.execute(new MyTask("3"));
    }

    static class MyTask implements Runnable {

        private String taskName;

        public MyTask(String taskName) {
            this.taskName = taskName;
        }

        public void run() {

            System.out.println("task name: " + this.taskName);

            String name = Thread.currentThread().getName();
            System.out.println("执行线程名称为 " + name);

            throw new NullPointerException();
        }

    }
}
