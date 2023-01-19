package com.damon.cqrs.sample.red_packet.domain.service;

import com.damon.cqrs.Config;
import com.damon.cqrs.CommandHandler;
import com.damon.cqrs.sample.red_packet.api.IRedPacketCommandHandler;
import com.damon.cqrs.sample.red_packet.api.command.RedPacketCreateCommand;
import com.damon.cqrs.sample.red_packet.api.command.RedPacketGetCommand;
import com.damon.cqrs.sample.red_packet.api.command.RedPacketGrabCommand;
import com.damon.cqrs.sample.red_packet.api.dto.WeixinRedPacketDTO;
import com.damon.cqrs.sample.red_packet.domain.aggregate.WeixinRedPacket;

import java.util.concurrent.CompletableFuture;

/**
 * @author xianpinglu
 */
public class RedPacketCommandHandler extends CommandHandler<WeixinRedPacket> implements IRedPacketCommandHandler {

    public RedPacketCommandHandler(Config config) {
        super(config);
    }

    @Override
    public void createRedPackage(RedPacketCreateCommand command) {
        super.process(command, () -> new WeixinRedPacket(command)).join();
    }

    @Override
    public int grabRedPackage(final RedPacketGrabCommand command) {
        return super.process(command, redPacket -> redPacket.grabRedPackage(command)).join();
    }

    @Override
    public WeixinRedPacketDTO get(RedPacketGetCommand command) {
        CompletableFuture<WeixinRedPacketDTO> future = super.process(
                command,
                redPacket -> {
                    WeixinRedPacketDTO redPacketDTO = new WeixinRedPacketDTO();
                    redPacketDTO.setMap(redPacket.getMap());
                    redPacketDTO.setId(redPacket.getId());
                    redPacketDTO.setRedpacketStack(redPacket.getRedpacketStack());
                    redPacketDTO.setSponsorId(redPacket.getSponsorId());
                    return redPacketDTO;
                }
        );
        return future.join();
    }

    @Override
    public CompletableFuture<WeixinRedPacket> getAggregateSnapshot(long aggregateId, Class<WeixinRedPacket> classes) {
        return super.getAggregateSnapshot(aggregateId, classes);
    }

    @Override
    public CompletableFuture<Boolean> saveAggregateSnapshot(WeixinRedPacket aggregate) {
        return super.saveAggregateSnapshot(aggregate);
    }

    @Override
    public WeixinRedPacket createAggregateSnapshot(WeixinRedPacket aggregate) {
        return super.createAggregateSnapshot(aggregate);
    }

    @Override
    public long createSnapshotCycle() {
        return super.createSnapshotCycle();
    }
}
