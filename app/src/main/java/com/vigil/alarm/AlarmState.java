package com.vigil.alarm;

import java.time.Instant;
import java.util.UUID;

public class AlarmState<T> {

    private final UUID alarmId;
    private final String name;
    private final Instant activatedAt;

    private T currentValue;
    private Status status;

    private boolean acknowledged;

    private Instant lastUpdated;
    private Instant acknowledgedAt;

    public AlarmState (AlarmResult<T> result){

        this.alarmId = UUID.randomUUID();

        this.name = result.name();
        this.activatedAt = result.timestampNow();

        this.status = result.status();
        this.lastUpdated = result.timestampNow();

        this.currentValue = result.value();
        this.acknowledged = false;
    }

    public AlarmMessage<T> toMessage() {
        return new AlarmMessage<>(
            this.alarmId,
            this.name,
            this.currentValue,
            this.status,
            this.acknowledged,
            this.activatedAt,
            this.acknowledgedAt,
            this.lastUpdated
        );
    }

    public void update(AlarmResult<T> result){

        if (this.status != result.status()) {
            this.acknowledged = false;
            this.acknowledgedAt = null;
        }

        this.lastUpdated = result.timestampNow();
        this.currentValue = result.value();
        this.status = result.status();
    }

    public void acknowledge() {
        this.acknowledged = true;
        this.acknowledgedAt = Instant.now();
    }

    public UUID getAlarmId(){
        return this.alarmId;
    }

    public String getName(){
        return this.name;
    }

    public Instant getActivatedAt(){
        return this.activatedAt;
    }

    public T getCurrentValue(){
        return this.currentValue;
    }

    public Status getCurrentStatus(){
        return this.status;
    }

    public boolean getAcknowledged() {
        return this.acknowledged;
    }

    public Instant getLastUpdated() {
        return this.lastUpdated;
    }

    public Instant getAcknowledgedAt() {
        return this.acknowledgedAt;
    }

    public void setCurrentValue(T value){
        this.currentValue = value;
    }

    public void setCurrentStatus(Status status){
        this.status = status;
    }

    public void setAcknowledged(boolean ack) {
        this.acknowledged = ack;
    }

    public void setLastUpdated(Instant updated) {
        this.lastUpdated = updated;
    }

    public void setAcknowledgedAt(Instant ackAt){
        this.acknowledgedAt = ackAt;
    }
}
