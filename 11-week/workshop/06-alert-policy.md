# Attendance availability burn-rate policy

## Contract and thresholds

These [rules](06-alerts.yml) protect the99% Attendance availability objective in [CP6](05-slo.md).
The route belongs to the Gateway -> Attendance -> RabbitMQ -> Notifications StudentAbsent journey.
Bad events are technical HTTP errors; valid events are success+error. Business rejections are excluded.
Run against one environment's Prometheus. Do not aggregate dev,qa and main in one evaluation.

| Alert | Long window | Short confirmation | Burn threshold | Technical error ratio | Routing |
|---|---|---|---|---|---|
| AttendanceFastBudgetBurn | 1h | 5m | >14.4 | >14.4% in BOTH windows | page, teamG1 |
| AttendanceSlowBudgetBurn | 6h | 30m | >6 | >6% in BOTH windows | ticket, teamG1 |

The short window is another rate comparison, not an additional `for: 5m` or `for: 30m` delay.
Rules use for:0m and a30s evaluation interval; both comparisons must hold at the same evaluation.
Strict > means equality alone does not fire. Zero denominator gives no active comparison; missing
telemetry/no traffic must not be reported as healthy. No request or personal identifier is an alert label.
The runbook URL points to the CP8 artifact that follows in the second work block.

## How much of this school's budget is at risk?

The planning budget is50 bad registration attempts in5,000 valid monthly attempts.
Under uniform traffic across a30-day window, spent budget fraction=burn*window_minutes/43,200:

- Fast:14.4*60/43,200=0.02, or2% of the budget, equivalent to1 of the50 allowed errors.
- Slow:6*360/43,200=0.05, or5% of the budget, equivalent to2.5 allowed errors (discrete events round up).

Those are stationary-traffic equivalents, not a guarantee about the actual error count at detection.
For this small school traffic is concentrated into20six-hour days. At its active average0.6944/min,
a full active hour contains about41.67 attempts;14.4% errors is6 failures, or12% of the monthly budget.
A six-hour school day has250 attempts;6% errors is15 failures, or30% of that budget.
The observable quantity is errors_in_window / (0.01 * valid_attempts_in_30d), not simply elapsed time.
A classroom burst or a single low-volume failure changes these percentages materially.
Keep the raw counts beside rates on the dashboard; review low-volume alert usefulness before production.
The policy does not silently add a volume gate that would change the specified rules.

## Alerts deliberately not created

| Cause-based page | Why it is not a user-impact page |
|---|---|
| CPU above80% | Short CPU peaks need not fail Attendance; inspect CPU as diagnosis after user-facing symptoms |
| RabbitMQ process/container restarted | A restart can be recovered by durable outbox/retry; monitor failed attempts, backlog and delay instead of paging on a restart alone |

A3s successful dependency response raises latency but not the error counter. These availability rules
will not detect a latency-only incident. CP9 must report that limitation rather than invent a timeout
or assert a false page. A latency-budget alert is a separate follow-up using the CP4 histogram.

## Verification and references

Validate YAML and both rule expressions; where available run `promtool check rules 06-alerts.yml`.
Rule syntax validation is not evidence that Prometheus scrapes the application or notifications are routed.
References: [Google SRE multiwindow burn rates](https://sre.google/workbook/alerting-on-slos/)
and [Prometheus alerting rules](https://prometheus.io/docs/prometheus/latest/configuration/alerting_rules/).
