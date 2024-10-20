package com.damon.cqrs.sample.trade_matching;

import cn.hutool.core.util.IdUtil;
import com.damon.cqrs.config.CqrsConfig;
import com.damon.cqrs.sample.TestConfig;
import com.damon.cqrs.sample.trade_matching.api.IStockCommandService;
import com.damon.cqrs.sample.trade_matching.api.cmd.StockBuyCmd;
import com.damon.cqrs.sample.trade_matching.domain.StockCommandService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test3 {
    public static void main(String[] args) throws InterruptedException {

        CqrsConfig cqrsConfig = TestConfig.init();
        IStockCommandService stockCommandService = new StockCommandService(cqrsConfig);
        ExecutorService service1 = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(200 * 5000);
        long start = System.currentTimeMillis();
        for (int k = 0; k < 800; k++) {
            service1.submit(() -> {
                for (int i = 0; i < 5000; i++) {
                    StockBuyCmd buyOrderCmd = new StockBuyCmd(IdUtil.getSnowflakeNextId(), 10000L, 0);
                    buyOrderCmd.setOrderId(IdUtil.getSnowflakeNextId());
                    buyOrderCmd.setNumber(1000);
                    buyOrderCmd.setPrice(100L);
                    stockCommandService.buy(buyOrderCmd);
                    latch.countDown();
                }
            });
        }

        latch.await();
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
    }
}
