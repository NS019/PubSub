package com.example.audit.consumer.consumer;


import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.example.audit.consumer.model.AuditEvent;

@Component // Spring is class ka bean banata hai, tabhi @JmsListener scan hota hai
public class AuditConsumer {

    // @JmsListener = "is queue pe message aaye to ye method chalao". Spring background threads mein queue poll karta hai.
    // Queue ka naam property se aata hai, hardcode nahi.
    // Default concurrency 1 hai (ek hi thread). Load badhe to spring.jms.listener.concurrency=3-10 se parallel
    // consumers chala sakte ho. Queue mein har message sirf EK consumer ko milta hai, to parallelism safe hai
    // (par message ka order guarantee nahi rehta).
    @JmsListener(destination = "${audit.queue-name}")
    public void onAuditEvent(AuditEvent event) {
        // System.out seedha console par print karta hai. Seekhne ke liye perfect.
        // Production mein iski jagah SLF4J logger (log.info) use hota hai, kyunki logger mein
        // level (INFO/WARN/ERROR), timestamp, thread name aur file/centralized logging (ELK/CloudWatch) milti hai,
        // aur System.out ko in sab ke bina filter ya route nahi kar sakte.
        System.out.println("===== AUDIT EVENT RECEIVED =====");
        System.out.println("eventId     : " + event.eventId());
        System.out.println("action      : " + event.action());
        System.out.println("entityType  : " + event.entityType());
        System.out.println("entityId    : " + event.entityId());
        System.out.println("performedBy : " + event.performedBy());
        System.out.println("occurredAt  : " + event.occurredAt());
        System.out.println("================================");

        // Yahan koi exception nahi aayi, to method normally return hota hai.
        // Spring tab JMS session commit karta hai, aur ActiveMQ message ko queue se hamesha ke liye hata deta hai (acknowledge).
        //
        // Agar yahan exception phenko (test ke liye try kar sakti ho: throw new RuntimeException("boom");),
        // to message rollback hoke queue mein wapas jaata hai aur dobara deliver hota hai.
        // Default redelivery policy ke baad (6 attempts) message "ActiveMQ.DLQ" (dead letter queue) mein chala jaata hai,
        // taaki ek kharab ("poison") message poori queue ko na rok de.
    }
}
