package com.corhuila.edutrack.attendance.workshop;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.atomic.AtomicInteger;
@RestController
public class AttendanceMetrics {
    private static final double[] LIMITS = {0.05, 0.1, 0.25, 0.5, 1, 3};
    private static final String ROUTE = "route=\"/api/v1/attendance\"";
    private final long[] buckets = new long[LIMITS.length];
    private final long[] outcomes = new long[3];
    private final AtomicInteger inFlight = new AtomicInteger();
    private long count; private double sum;
    public void enter() { inFlight.incrementAndGet(); }
    public synchronized void finish(int status, double seconds) {
        seconds = Math.max(0, seconds); count++; sum += seconds;
        outcomes[status >= 500 ? 2 : status >= 400 ? 1 : 0]++;
        for (int i = 0; i < LIMITS.length; i++) if (seconds <= LIMITS[i]) buckets[i]++;
        inFlight.decrementAndGet();
    }
    @GetMapping(value = "/metrics", produces = "text/plain;version=0.0.4;charset=utf-8")
    public synchronized String render() {
        var b = new StringBuilder("# TYPE attendance_requests_total counter\n");
        String[] names = {"success", "rejected", "error"};
        for (int i = 0; i < names.length; i++) b.append("attendance_requests_total{").append(ROUTE)
            .append(",outcome=\"").append(names[i]).append("\"} ").append(outcomes[i]).append('\n');
        b.append("# TYPE attendance_request_duration_seconds histogram\n");
        for (int i = 0; i < LIMITS.length; i++) b.append("attendance_request_duration_seconds_bucket{")
            .append(ROUTE).append(",le=\"").append(LIMITS[i]).append("\"} ").append(buckets[i]).append('\n');
        b.append("attendance_request_duration_seconds_bucket{").append(ROUTE).append(",le=\"+Inf\"} ").append(count).append('\n');
        b.append("attendance_request_duration_seconds_count{").append(ROUTE).append("} ").append(count).append('\n');
        b.append("attendance_request_duration_seconds_sum{").append(ROUTE).append("} ").append(sum).append('\n');
        b.append("# TYPE attendance_requests_in_flight gauge\nattendance_requests_in_flight ").append(inFlight.get()).append('\n');
        return b.toString();
    }
}
