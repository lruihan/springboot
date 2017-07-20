package com.example.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Test {

    private static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        Test test = new Test();
        test.func1();
    }
    private boolean func1() {
        ReentrantLock lock = new ReentrantLock();
        try {
            logger.info("try locking");
            lock.tryLock(1, TimeUnit.MINUTES);
            try {
                func2();
                return true;
            } catch (Exception e) {
                logger.error("exception from func2...");
            }
        } catch (InterruptedException e) {
            logger.error("failed...", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            logger.debug("Released clusterUpdateLock from syncCluster.");
        }
        return false;
    }
    private void func2() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for(int i = 0; i < 4; i++) {
            executorService.submit(() -> {
                try {
                    logger.info("thread...");
                    Thread.sleep(2*60*1000);
                } catch (InterruptedException e) {
                    logger.error("interrupted");
                    //e.printStackTrace();
                }
            });
        }
        shutdownExecutor(executorService);
    }

    private void shutdownExecutor(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
            logger.error("Termination is interrupted", e);
        }
        finally {
            if (!executor.isTerminated()) {
                logger.error("Force to shutdown");
            }
            executor.shutdownNow();
        }
    }
}
