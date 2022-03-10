package com.damon.cqrs.sample.red_packet.domain.aggregate;

import com.damon.cqrs.domain.Aggregate;
import com.damon.cqrs.sample.red_packet.api.event.RedPacketCreatedEvent;
import com.damon.cqrs.sample.red_packet.api.event.RedPacketGrabSucceedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * 微信红包聚合根
 *
 * @author xianpinglu
 */
public class WeixinRedPacket extends Aggregate {
    /**
     * key 用户id   value  抢到的金额
     */
    private Map<Long, Double> map;

    private Stack<Double> redpacketStack;

    private Long sponsorId;

    private Double money;

    private int size;

    public WeixinRedPacket() {

    }

    public WeixinRedPacket(Long id, Double money, int size, Long sponsorId) {
        super(id);
        Stack<Double> stack = getRandomMoney(money, size);
        RedPacketCreatedEvent event = new RedPacketCreatedEvent(stack);
        event.setAggregateId(id);
        event.setSponsorId(sponsorId);
        event.setMoney(money);
        event.setSize(size);
        super.applyNewEvent(event);
    }

    private Stack<Double> getRandomMoney(Double totalMoney, int size) {
        Stack<Double> stack = new Stack<>();
        // remainSize 剩余的红包数量 , remainMoney 剩余的钱
        Double remainMoney = totalMoney;
        int remainSize = size;
        for (int i = 0; i < size; i++) {
            if (remainSize == 1) {
                remainSize--;
                stack.add((double) Math.round(remainMoney * 100) / 100);
            } else {
                Random r = new Random();
                double min = 0.01;
                double max = remainMoney / remainSize * 2;
                double money = r.nextDouble() * max;
                money = money <= min ? 0.01 : money;
                money = Math.floor(money * 100) / 100;
                remainSize--;
                remainMoney -= money;
                stack.add(money);
            }
        }
        return stack;
    }

    /**
     * 抢红包
     *
     * @param userId
     * @return
     */
    public int grabRedPackage(Long userId) {
        //红包已抢完
        if (redpacketStack.size() == 0) {
            return 0;
        }
        //用户已抢过红包
        if (map.get(userId) != null) {
            return -1;
        }
        //抢红包成功
        super.applyNewEvent(new RedPacketGrabSucceedEvent(redpacketStack.pop(), userId));
        return 1;
    }

    private void apply(RedPacketGrabSucceedEvent event) {
        map.put(event.getUserId(), event.getMoney());
    }

    private void apply(RedPacketCreatedEvent event) {
        this.redpacketStack = event.getRedpacketStack();
        this.sponsorId = event.getSponsorId();
        this.map = new HashMap<>();
        this.size = event.getSize();
        this.money = event.getMoney();
    }

    @Override
    public long createSnapshootCycle() {
        return -1;
    }

    public Map<Long, Double> getMap() {
        return map;
    }

    public Stack<Double> getRedpacketStack() {
        return redpacketStack;
    }

    public Long getSponsorId() {
        return sponsorId;
    }
}