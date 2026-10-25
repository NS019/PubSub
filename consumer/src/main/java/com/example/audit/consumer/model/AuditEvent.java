package com.example.audit.consumer.model;

import java.time.Instant;

// "record" = immutable data holder (Java 16+). Getters, equals, hashCode, toString apne aap ban jaate hain.
// Immutable isliye kyunki event "jo ho chuka" uski kahani hai, usko baad mein badalna nahi chahiye.
// Ye DB table nahi banata, sirf message ka shape hai.
// Alternate: normal class + Lombok @Value. Record kam boilerplate deta hai, isliye ye chuna.
public record AuditEvent(
        String eventId,       // UUID, consumer isse duplicate delivery pakad sakta hai
        String action,        // abhi String; baad mein enum bana denge
        String entityType,
        String entityId,
        String performedBy,
        Instant occurredAt    // Instant = timezone-free UTC moment. LocalDateTime avoid kiya kyunki
                              // alag servers par timezone ka confusion hota hai.
) {}