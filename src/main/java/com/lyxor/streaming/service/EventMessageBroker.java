package com.lyxor.streaming.service;

import com.lyxor.streaming.model.DeliveryStatus;
import com.lyxor.streaming.model.EventMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EventMessageBroker {

    private final BlockingQueue<EventMessage> messageQueue;
    private final Map<String, EventMessage> inFlightMessages = new ConcurrentHashMap<>();
    private final Set<String> acknowledgedHistory = ConcurrentHashMap.newKeySet();

    public EventMessageBroker(int queueCapacity) {
        this.messageQueue = new ArrayBlockingQueue<>(queueCapacity);
    }

    public boolean publishWithBackpressure(EventMessage message, long timeoutMs) {
        try {
            return messageQueue.offer(message, timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public EventMessage pollMessage(long timeoutMs) throws InterruptedException {
        EventMessage msg = messageQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (msg != null) {
            msg.setStatus(DeliveryStatus.IN_FLIGHT);
            inFlightMessages.put(msg.getMessageId(), msg);
        }
        return msg;
    }

    public boolean acknowledgeMessage(String messageId) {
        EventMessage msg = inFlightMessages.get(messageId);
        if (msg != null) {
            msg.setStatus(DeliveryStatus.ACKNOWLEDGED);
            acknowledgedHistory.add(messageId);
            inFlightMessages.remove(messageId);
            return true;
        }
        return false;
    }

    public int getQueueSize() {
        return messageQueue.size();
    }

    public int getInFlightCount() {
        return inFlightMessages.size();
    }
}
