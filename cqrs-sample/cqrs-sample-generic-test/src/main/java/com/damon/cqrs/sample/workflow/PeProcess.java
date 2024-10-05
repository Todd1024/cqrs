package com.damon.cqrs.sample.workflow;

import lombok.Data;

@Data
public class PeProcess {
    private String id;
    private PeNode start;

    public PeProcess(String id, PeNode start) {
        this.id = id;
        this.start = start;
    }
}
 

 
