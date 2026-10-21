package com.corhuila.edutrack.attendance.workshop;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@Component
public class AbsencePublisher {
    private final RabbitTemplate rabbit; private final JsonLog log;
    public AbsencePublisher(RabbitTemplate rabbit, JsonLog log) { this.rabbit = rabbit; this.log = log; }
    public void publish(byte[] envelope, TraceContext parent) {
        var producer = parent.child(); long start = System.nanoTime();
        try {
            rabbit.convertAndSend("edutrack.events", "educk.attendance.student.absent", envelope, message -> {
                message.getMessageProperties().setContentType("application/json");
                message.getMessageProperties().setHeader("traceparent", producer.traceparent());
                message.getMessageProperties().setHeader("correlation_id", producer.correlationId());
                message.getMessageProperties().setCorrelationId(producer.correlationId());
                return message;
            });
            log.write(producer, "INFO", "attendance.publish.sent", "StudentAbsent submitted to RabbitMQ", (System.nanoTime() - start) / 1_000_000.0);
        } catch (AmqpException e) {
            log.write(producer, "ERROR", "attendance.publish.failed", "RabbitMQ publication failed", (System.nanoTime() - start) / 1_000_000.0);
            throw e;
        }
    }
}
