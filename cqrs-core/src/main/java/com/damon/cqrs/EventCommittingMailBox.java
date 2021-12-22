package com.damon.cqrs;

import static java.time.ZonedDateTime.now;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.damon.cqrs.domain.Aggregate;
import com.damon.cqrs.exception.DuplicateEventStreamException;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * 
 * 
 * @author xianping_lu
 *
 * 
 */
@Slf4j
public class EventCommittingMailBox {
    private ExecutorService service;
    private ZonedDateTime lastActiveTime;
    private final int mailboxNumber;
    private AtomicBoolean onRunning = new AtomicBoolean(false);
    private ConcurrentLinkedQueue<EventCommittingContext> eventQueue = new ConcurrentLinkedQueue<>();
    private final int batchCommitSize;
    private final Consumer<List<EventCommittingContext>> handler;
    private ConcurrentHashMap<Long, ConcurrentHashMap<String, EventCommittingContext>> aggregateDictDict = new ConcurrentHashMap<>();

    public EventCommittingMailBox(ExecutorService service, Consumer<List<EventCommittingContext>> handler, int mailboxNumber, int batchCommitSize) {
        this.mailboxNumber = mailboxNumber;
        this.batchCommitSize = batchCommitSize;
        this.handler = handler;
        this.lastActiveTime = now();
        this.service = service;
    }

    public void enqueue(EventCommittingContext context) {
        ConcurrentHashMap<String, EventCommittingContext> aggregateDict = aggregateDictDict.computeIfAbsent(context.getAggregate().getId(),
                (key) -> new ConcurrentHashMap<String, EventCommittingContext>());
        String eventId = context.getAggregate().getId() + ":" + context.getVersion();
        if (aggregateDict.putIfAbsent(eventId, context) == null) {
            context.setMailBox(this);
            eventQueue.add(context);
            lastActiveTime = now();
            tryRun();
        } else {
            String message = String.format("aggregate id : %s , aggregate type : %s  event stream already exist in the EventCommittingMailBox, eventId: %s", context.getAggregate().getId(),
                    context.getAggregate().getClass().getTypeName(), eventId);
            throw new DuplicateEventStreamException(message);
        }

    }

    private void tryRun() {
        if (onRunning.compareAndSet(false, true)) {
            service.submit(() -> {
                process();
            });
        }
    }

    private void setAsNotRunning() {
        onRunning.compareAndSet(true, false);
    }

    private boolean noUnHandledMessage() {
        return eventQueue.isEmpty();
    }

    private void completeRun() {
        lastActiveTime = now();
        if (log.isDebugEnabled()) {
            log.debug("{} complete run, mailboxNumber: {}", this.getClass(), mailboxNumber);
        }
        setAsNotRunning();
        if (!noUnHandledMessage()) {
            tryRun();
            return;
        }
    }

    public ZonedDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * 移除聚合所有待提交的事件 (聚合更新冲突时使用)
     * 
     * @param aggregateId
     * @return
     */
    public ConcurrentHashMap<String, EventCommittingContext> removeAggregateAllEventCommittingContexts(long aggregateId) {
        return aggregateDictDict.remove(aggregateId);
    }

    private void process() {
        lastActiveTime = now();
        List<EventCommittingContext> events = new ArrayList<>();
        while (events.size() < batchCommitSize) {
            EventCommittingContext event = eventQueue.poll();
            if (event != null) {
                Aggregate aggregate = event.getAggregate();
                ConcurrentHashMap<String, EventCommittingContext> eventMap = aggregateDictDict.getOrDefault(aggregate.getId(), null);
                String eventId = aggregate.getId() + ":" + event.getVersion();
                if (eventMap != null && eventMap.remove(eventId) != null) {
                    events.add(event);
                }
            } else {
                break;
            }
        }
        if (events.size() == 0) {
            completeRun();
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("{} batch process events , mailboxNumber : {}, batch size : {}", this.getClass(), mailboxNumber, events.size());
        }
        try {
            handler.accept(events);
        } finally {
            completeRun();
        }
    }

}
