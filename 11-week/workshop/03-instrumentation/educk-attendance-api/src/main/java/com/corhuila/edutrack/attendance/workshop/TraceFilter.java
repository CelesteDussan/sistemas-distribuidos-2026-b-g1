package com.corhuila.edutrack.attendance.workshop;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class TraceFilter extends OncePerRequestFilter {
    private final JsonLog log;
    public TraceFilter(JsonLog log) { this.log = log; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var ctx = TraceContext.incoming(request.getHeader("traceparent"), request.getHeader("X-Correlation-Id"));
        request.setAttribute(TraceContext.ATTRIBUTE, ctx);
        response.setHeader("traceparent", ctx.traceparent());
        response.setHeader("X-Correlation-Id", ctx.correlationId());
        long start = System.nanoTime(); boolean failed = false;
        try { chain.doFilter(request, response); }
        catch (ServletException | IOException | RuntimeException e) { failed = true; throw e; }
        finally {
            String level = failed || response.getStatus() >= 500 ? "ERROR" : response.getStatus() >= 400 ? "WARN" : "INFO";
            log.write(ctx, level, "attendance.http.completed", "Attendance HTTP operation completed", (System.nanoTime() - start) / 1_000_000.0);
            request.removeAttribute(TraceContext.ATTRIBUTE);
        }
    }
}
