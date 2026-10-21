package com.corhuila.edutrack.attendance.workshop;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
public class AttendanceInstrumentationTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private static final String TRACE = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String PARENT = "00-" + TRACE + "-00f067aa0ba902b7-01";
    private static final String BODY = "{\"studentId\":\"dc601d9f-34dd-4e4c-8e66-a96f941235af\",\"sessionId\":\"df80a559-1b49-47c4-9877-78d85ec6e3ea\",\"sessionDate\":\"2026-10-20\",\"status\":\"ABSENT\",\"sequenceNumber\":15}";
    @Test void incomingTraceReachesAmqpWithChildSpanAndMetrics() throws Exception {
        var rabbit = mock(RabbitTemplate.class); var sent = new AtomicReference<Message>();
        doAnswer(call -> {
            var message = new Message(call.getArgument(2, byte[].class), new MessageProperties());
            sent.set(call.getArgument(3, MessagePostProcessor.class).postProcessMessage(message)); return null;
        }).when(rabbit).convertAndSend(eq("edutrack.events"), eq("educk.attendance.student.absent"), any(Object.class), any(MessagePostProcessor.class));
        var metrics = new AttendanceMetrics(); var log = new JsonLog(mapper);
        var mvc = MockMvcBuilders.standaloneSetup(new AttendanceProbe(new AbsencePublisher(rabbit, log), mapper), metrics)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper), new org.springframework.http.converter.StringHttpMessageConverter())
            .addFilters(new TraceFilter(log, metrics)).build();
        var result = mvc.perform(post("/api/v1/attendance").contentType("application/json").content(BODY).header("traceparent", PARENT)).andReturn();
        assertEquals(202, result.getResponse().getStatus());
        var outgoing = sent.get().getMessageProperties().getHeader("traceparent").toString();
        assertEquals(TRACE, outgoing.substring(3, 35));
        assertNotEquals(PARENT.substring(36, 52), result.getResponse().getHeader("traceparent").substring(36, 52));
        assertNotEquals(result.getResponse().getHeader("traceparent").substring(36, 52), outgoing.substring(36, 52));
        assertEquals(result.getResponse().getHeader("X-Correlation-Id"), sent.get().getMessageProperties().getHeader("correlation_id"));
        assertEquals("StudentAbsent", mapper.readTree(sent.get().getBody()).get("eventType").asText());
        assertTrue(metrics.render().contains("outcome=\"success\"} 1"));
        assertTrue(metrics.render().contains("le=\"+Inf\"} 1"));
        var scrape = mvc.perform(get("/metrics")).andReturn().getResponse();
        assertEquals(200, scrape.getStatus()); assertTrue(scrape.getContentAsString().contains("attendance_request_duration_seconds_bucket"));
        assertTrue(metrics.render().contains("outcome=\"success\"} 1"));
    }
    @Test void missingMalformedAndZeroContextsGetNonzeroIdentifiers() {
        for (String h : new String[]{"", "garbage", "00-" + "0".repeat(32) + "-00f067aa0ba902b7-01", "00-" + TRACE + "-" + "0".repeat(16) + "-01"}) {
            var ctx = TraceContext.incoming(h, "invalid");
            assertTrue(ctx.traceparent().matches("00-[0-9a-f]{32}-[0-9a-f]{16}-01"));
            assertNotEquals("0".repeat(32), ctx.traceId()); assertNotEquals("0".repeat(16), ctx.spanId());
            assertDoesNotThrow(() -> java.util.UUID.fromString(ctx.correlationId()));
        }
    }
    @Test void histogramIsCumulativeAndTracksOutcomes() {
        var m = new AttendanceMetrics(); m.enter(); m.finish(202, .08); m.enter(); m.finish(503, 2);
        String data = m.render();
        assertTrue(data.contains("le=\"0.05\"} 0")); assertTrue(data.contains("le=\"0.1\"} 1"));
        assertTrue(data.contains("le=\"3.0\"} 2")); assertTrue(data.contains("le=\"+Inf\"} 2"));
        assertTrue(data.contains("outcome=\"error\"} 1")); assertTrue(data.contains("attendance_requests_in_flight 0"));
    }
    @Test void brokerFailureIs503AndBusinessRejectionDoesNotPublish() throws Exception {
        var rabbit = mock(RabbitTemplate.class);
        doThrow(new AmqpConnectException(new ConnectException("unavailable"))).when(rabbit)
            .convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class));
        var m = new AttendanceMetrics(); var log = new JsonLog(mapper);
        var mvc = MockMvcBuilders.standaloneSetup(new AttendanceProbe(new AbsencePublisher(rabbit, log), mapper))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper), new org.springframework.http.converter.StringHttpMessageConverter()).addFilters(new TraceFilter(log, m)).build();
        assertEquals(503, mvc.perform(post("/api/v1/attendance").contentType("application/json").content(BODY)).andReturn().getResponse().getStatus());
        clearInvocations(rabbit);
        assertEquals(400, mvc.perform(post("/api/v1/attendance").contentType("application/json").content(BODY.replace("ABSENT", "INVALID"))).andReturn().getResponse().getStatus());
        verifyNoInteractions(rabbit); assertTrue(m.render().contains("outcome=\"rejected\"} 1"));
    }
}
