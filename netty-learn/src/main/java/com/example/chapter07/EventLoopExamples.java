package com.example.chapter07;

import java.util.Collections;
import java.util.List;

public class EventLoopExamples {

    public static void executeTask() {
        boolean terminated = true;

        while (!terminated) {
            // 阻塞直到有事件已经就绪并可以运行
            List<Runnable> readyEvents = blockUntilEventsReady();
            // 依次处理所有事件
            for (Runnable readyEvent : readyEvents) {
                readyEvent.run();
            }
        }
    }

    /**
     * 模拟延时获取任务
     */
    private static final List<Runnable> blockUntilEventsReady() {

        return Collections.<Runnable>singletonList(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
