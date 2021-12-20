package com.damon.cqrs.rocketmq;

import com.damon.cqrs.domain.Command;

public class ACKCommand extends Command {

    /**
     * 
     */
    private static final long serialVersionUID = -1054063014509404578L;

    public ACKCommand(long commandId, long aggregateId) {
        super(commandId, aggregateId);
    }
    

    private CommandACKStatus status;

    public CommandACKStatus getStatus() {
        return status;
    }

    public void setStatus(CommandACKStatus status) {
        this.status = status;
    }

}
