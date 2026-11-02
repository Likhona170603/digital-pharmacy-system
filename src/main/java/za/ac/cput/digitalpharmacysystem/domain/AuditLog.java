package za.ac.cput.digitalpharmacysystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/*
 * Sesona Ntshatsha
 * 240773365

 */
@Entity
public class AuditLog {
    @Id
    private String id;
    private String action;
    private String entityTarget;
    private LocalDateTime timeStamp;
    private Long actorId;
    private String actorRole;
    private String entityId;

    protected AuditLog() {}

    private AuditLog(Builder builder) {
        this.id=builder.id;
        this.action=builder.action;
        this.entityTarget=builder.entityTarget;
        this.timeStamp=builder.timeStamp;
        this.actorId=builder.actorId;
        this.actorRole=builder.actorRole;
        this.entityId=builder.entityId;
    }
    public String getId() {
        return id;
    }
    public String getAction() {
        return action;
    }
    public String getEntityTarget() {
        return entityTarget;
    }
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }
    public Long getActorId() {
        return actorId;
    }
    public String getActorRole() {
        return actorRole;
    }
    public String getEntityId() {
        return entityId;
    }
    @Override
    public String toString() {
        return "AuditLog{" +
                "id='" + id + '\'' +
                ", action='" + action + '\'' +
                ", entityTarget='" + entityTarget + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }


    public static class Builder {
        private String id;
        private String action;
        private String entityTarget;
        private LocalDateTime timeStamp;
        private Long actorId;
        private String actorRole;
        private String entityId;
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        public Builder setAction(String action) {
            this.action = action;
            return this;

        }
        public Builder setEntityTarget(String entityTarget) {
            this.entityTarget = entityTarget;
            return this;
        }
        public Builder setTimeStamp(LocalDateTime timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }
        public Builder setActorId(Long actorId) {
            this.actorId = actorId;
            return this;
        }
        public Builder setActorRole(String actorRole) {
            this.actorRole = actorRole;
            return this;
        }
        public Builder setEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }
        public AuditLog build() {
            return new AuditLog(this);



        }
    }
}