package com.audit.demo.config;


import java.util.Map;                                                   // Map.of(...) ke liye, ye import tumhare code mein missing tha

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter; // Jackson 3 wala naya converter (Spring 7)
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;           // TEXT/BYTES enum, import missing tha

import com.audit.demo.model.AuditEvent;

import tools.jackson.databind.json.JsonMapper;                          // Jackson 3 mein ObjectMapper ki jagah JsonMapper builder use hota hai
import tools.jackson.databind.cfg.DateTimeFeature;                      // Jackson 3 mein date features yahan shift ho gaye

@Configuration // Spring ko batata hai ki is class ke @Bean methods container mein register karne hain.
               // Koi DB table/column nahi banta, ye sirf in-memory bean registration hai.
public class JmsConfig {

    @Bean // JmsTemplate / listener container is bean ko auto-pick kar lete hain (Boot auto-config).
          // Alternative: har JmsTemplate pe manually setMessageConverter, par wo duplication hai.
    public MessageConverter jacksonJmsMessageConverter() {

        // Jackson 3 mein ObjectMapper IMMUTABLE hai, isliye builder se banate hain (thread-safe, production-friendly).
        // JavaTimeModule alag se register karne ki zarurat NAHI, Instant/LocalDateTime support built-in hai.
        JsonMapper mapper = JsonMapper.builder()
                // false = dates numeric timestamp nahi, ISO string jaisi jayengi (2026-10-24T17:00:00Z).
                // Readable hai broker console mein, aur consumer repo bhi easily parse karega.
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        // Jackson 3 ke saath yahi converter chalta hai. MappingJackson2MessageConverter Jackson 2 ka hai
        // aur Spring 7 mein removal ke liye marked hai, naye code mein use mat karo.
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(mapper);

        // TEXT = JSON string ke roop mein jata hai, ActiveMQ console mein padh sakte ho (debugging easy).
        // BYTES chhota/fast hota hai, par human-readable nahi. Audit ke liye TEXT behtar.
        converter.setTargetType(MessageType.TEXT);

        // JMS property ka naam jisme type-id jata hai, consumer isi se class pehchanta hai.
        converter.setTypeIdPropertyName("_type");

        // Poora class path bhejne se consumer repo (alag package) mein deserialize fail hota hai.
        // Isliye short alias "AuditEvent". Consumer mein bhi yahi alias apni class se map karna.
        // Production note: alias ek contract hai, ise rename kiya to purane messages queue mein atak jayenge.
        converter.setTypeIdMappings(Map.of("AuditEvent", AuditEvent.class));

        return converter;
    }

    // Sirf placeholder class taaki mapping compile ho.
    // IMPORTANT: publisher repo mein yahan apni asli AuditEvent class daalna behtar hai
    // (Map.of("AuditEvent", AuditEvent.class)), warna serialization pe real class ka info nahi milega.
    private static final class AuditEventAliasHolder {}
}

