package com.audit.demo.controller;


import java.time.Instant;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.demo.model.AuditEvent;
import com.audit.demo.publisher.AuditPublisher;

@RestController // @Controller + @ResponseBody: return value seedha JSON response banta hai
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditPublisher publisher;

    public AuditController(AuditPublisher publisher) { this.publisher = publisher; }

    // Body mein sirf itna bhejo: { "action":"CREATE","entityType":"DASHBOARD","entityId":"d-1","performedBy":"neeti" }
    @PostMapping("/test")
    public String send(@RequestBody TestRequest req) {
        // eventId aur occurredAt server khud banata hai. Client ke bharose nahi chhodte,
        // kyunki audit trail mein time aur ID trustworthy honi chahiye.
        AuditEvent event = new AuditEvent(UUID.randomUUID().toString(),
                req.action(), req.entityType(), req.entityId(), req.performedBy(), Instant.now());
        publisher.publish(event);
        return "sent " + event.eventId();
    }

    // Request ke liye alag record: API ka shape aur event ka shape ko alag rakhte hain,
    // taaki ek badle to dusra na tute.
    public record TestRequest(String action, String entityType, String entityId, String performedBy) {}
}
