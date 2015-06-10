package com.ecs160.packmanager.utils;

import java.util.UUID;

/**
 * Transaction class that holds all data about a specific transcation.
 */
public class Transaction {

    public enum TransactionStatus {
        INITIATED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        DELAYED,
        LOST;
    }

    private String transactionId;
    private String packageName; // "package" is a reserved keyword in Java so we can't use it.
    private TransactionStatus status;
    private User sender;
    private User receiver;
    private User intermediary; // May be null if the sender and receiver directly handle the transaction.
    private String location;
    private String timeStamp; // Formatted as a Jave-style time in millseconds.
    private String intermediaryLocation;
    private long intermediaryTimestamp; // Formatted as a Jave-style time in millseconds.

    public Transaction(String packageName, User sender, User receiver) {
        transactionId = UUID.randomUUID().toString().substring(0, 3);
        this.packageName = packageName;
        this.sender = sender;
        this.receiver = receiver;

    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setIntermediary(User intermediary) {
        this.intermediary = intermediary;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIntermediaryLocation(String intermediaryLocation) {
        this.intermediaryLocation = intermediaryLocation;
    }

    public void setIntermediaryTimestamp(long intermediaryTimestamp) {
        this.intermediaryTimestamp = intermediaryTimestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPackageName() {
        return packageName;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public User getIntermediary() {
        return intermediary;
    }

    public String getLocation() {
        return location;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getIntermediaryLocation() {
        return intermediaryLocation;
    }

    public long getIntermediaryTimestamp() {
        return intermediaryTimestamp;
    }
}
