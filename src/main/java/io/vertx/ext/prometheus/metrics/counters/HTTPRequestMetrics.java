package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.prometheus.MetricLabel;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HTTPRequestMetrics {
  private final @NotNull Gauge requests;
  private final @NotNull Counter responses;
  private final @NotNull Stopwatch proocessTime;
  private final @NotNull String localAddress;
  private final @NotNull  Set<MetricLabel> enabledMetricLabelValues;

  public HTTPRequestMetrics(@NotNull String name, @NotNull String localAddress, @NotNull GaugeFactory gauges, final Set<MetricLabel> enabledMetricLabelValues, @NotNull CounterFactory counters, @NotNull HistogramFactory histograms) {
    this.localAddress = localAddress;
    this.enabledMetricLabelValues = enabledMetricLabelValues;
    requests = gauges.httpRequests(name, enabledMetricLabelValues);
    responses = counters.httpResponses(name);
    proocessTime = new Stopwatch(name + "_requests", localAddress, histograms);
  }

  public @NotNull Metric begin(@NotNull HttpMethod method, @NotNull String path, @NotNull String host) {
    requests(method.name(), path, host, "active").inc();
    requests(method.name(), path, host, "total").inc();
    return new Metric(method, path, host, proocessTime.start());
  }

  public void reset(@NotNull Metric metric) {
    metric.resetTimer(proocessTime.start());
    requests(metric, "reset").inc();
    requests(metric, "processed").inc();
    requests(metric, "active").dec();
  }

  public void responseEnd(@NotNull Metric metric, int responseStatusCode) {
    metric.resetTimer(proocessTime.start());
    requests(metric, "active").dec();
    requests(metric, "processed").inc();
    responses(responseStatusCode).inc();
  }

  public void requestEnd(@NotNull Metric metric) {
    metric.resetTimer(proocessTime.start());
  }

  public void upgrade(@NotNull Metric metric) {
    requests(metric, "upgraded").inc();
  }

  private @NotNull Counter.Child responses(int responseStatusCode) {
    return responses.labels(localAddress, Integer.toString(responseStatusCode));
  }

  private @NotNull Gauge.Child requests(@NotNull HTTPRequestMetrics.@NotNull Metric metric, @NotNull String state) {
    return requests(metric.method.name(), metric.path, metric.host, state);
  }

  private @NotNull Gauge.Child requests(@NotNull String method, @NotNull String path, @NotNull String host, @NotNull String state) {
    final List<String> labelNames = new ArrayList<>();
    labelNames.add(localAddress);
    labelNames.add(method);
    if (enabledMetricLabelValues.contains(MetricLabel.useHost)) {
      labelNames.add(host);
    }
    if (enabledMetricLabelValues.contains(MetricLabel.usePath)) {
      labelNames.add(path);
    }

    labelNames.add(state);

    return requests.labels(labelNames.toArray(new String[labelNames.size()]));
  }

  public static final class Metric {
    private final @NotNull HttpMethod method;
    private final @NotNull String path;
    private final @NotNull String host;
    private @NotNull Histogram.Timer timer;

    public Metric(@NotNull HttpMethod method, @NotNull String path, @NotNull String host, @NotNull Histogram.Timer timer) {
      this.method = method;
      this.path = path;
      this.host = host;
      this.timer = timer;
    }

    public void resetTimer(@NotNull Histogram.Timer newTimer) {
      timer.observeDuration();
      timer = newTimer;
    }
  }
}
