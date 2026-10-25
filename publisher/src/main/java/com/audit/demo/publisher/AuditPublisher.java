package com.audit.demo.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.audit.demo.model.AuditEvent;

@Service // business logic layer ka bean. @Component jaisa hi hai, bas intent batata hai.
public class AuditPublisher {

    // JmsTemplate = JMS ka "courier boy". Connection, session, producer sab khud sambhalta hai.
    // Alternate: raw JMS API (bahut boilerplate) ya Spring Cloud Stream (zyada abstraction).
    private final JmsTemplate jmsTemplate;
    private final String queueName;

    // Constructor injection: @Autowired field injection se behtar kyunki testing mein easily mock kar sakte ho
    public AuditPublisher(JmsTemplate jmsTemplate, @Value("${audit.queue-name}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
    }

    public void publish(AuditEvent event) {
        // convertAndSend = object -> JSON (hamare converter se) -> queue mein daalo.
        // Default mein ye "point-to-point queue" hai: har message sirf EK consumer ko milta hai.
        // Agar sabko dena ho (jaise notification), to Topic use hota hai.
        jmsTemplate.convertAndSend(queueName, event);
        // Production note: ye call broker down hone par exception phekta hai. Real systems mein
        // ya to retry karte hain ya "outbox pattern" use karte hain. Wo hum baad mein dekhenge.
    }
}
