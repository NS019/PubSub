package com.example.audit.consumer.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;


import org.springframework.jms.support.converter.JacksonJsonMessageConverter; // Jackson 3 wala naya converter (Spring 7)
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.example.audit.consumer.model.AuditEvent;

import tools.jackson.databind.json.JsonMapper; 
import tools.jackson.databind.cfg.DateTimeFeature;

@Configuration // is class ke @Bean methods Spring container mein register hote hain
public class JmsConfig {

    // Boot ka auto-configured listener container is bean ko apne aap utha leta hai,
    // to @JmsListener ko JSON -> AuditEvent conversion mil jaata hai.
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
    	JsonMapper mapper = JsonMapper.builder()
                // false = dates numeric timestamp nahi, ISO string jaisi jayengi (2026-10-24T17:00:00Z).
                // Readable hai broker console mein, aur consumer repo bhi easily parse karega.
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();// publisher ke ISO format ke saath consistent

        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
  
        converter.setTargetType(MessageType.TEXT);  // publisher TEXT bhejta hai, yahan bhi TEXT
        converter.setTypeIdPropertyName("_type");   // publisher jis property mein type likhta hai, wahi naam

        // SABSE ZAROORI LINE: publisher "_type=AuditEvent" (chhota alias) bhejta hai.
        // Yahan batate hain ki alias "AuditEvent" = HAMARI consumer-side class.
        // Ye nahi lagayi to converter publisher ka poora class path dhoondhega aur ClassNotFoundException dega.
        converter.setTypeIdMappings(Map.of("AuditEvent", AuditEvent.class));
        return converter;
    }
}
