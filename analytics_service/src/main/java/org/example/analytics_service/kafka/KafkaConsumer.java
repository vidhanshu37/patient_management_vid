package org.example.analytics_service.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient-topic", groupId = "analytics-group")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            // perform business logic here

            log.info("Received patient event: {} \n id : {}, \n name : {}", patientEvent.getEventType(), patientEvent.getPatientId(), patientEvent.getName());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing from Kafka: {}", e.getMessage());
        }
    }
}
