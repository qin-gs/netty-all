package com.example.netty.future;

import java.util.concurrent.*;

public class JdkFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(2);
        Future<Integer> future = service.submit(() -> {
            TimeUnit.SECONDS.sleep(2);
            return 1;
        });
        // 同步阻塞等待任务完成
        Integer i = future.get();
        System.out.println(i);
    }
}
