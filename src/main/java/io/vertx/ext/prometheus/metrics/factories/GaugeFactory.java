package io.vertx.ext.prometheus.metrics.factories;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.vertx.ext.prometheus.MetricLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for shared gauges.
 * Gauges are identified by name, a gauge can only be shared once in a {@link CollectorRegistry}.
 *
 * @author jansorg
 */
public class GaugeFactory {
  private final CollectorRegistry registry;
  private final Map<String, Gauge> gauges = new ConcurrentHashMap<>();

  public GaugeFactory(CollectorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Unregisters the gauges created by this factory from the registry.
   */
  public void close() {
    gauges.values().forEach(registry::unregister);
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @param enabledMetricLabelValues The enabled metric labels for this metric
   * @return A gauge for http requests, identified by the given name. Gauges with the same name are shared.
   */
  public Gauge httpRequests(String name, final Set<MetricLabel> enabledMetricLabelValues) {
    final List<String> labelNames = new ArrayList<>();
    labelNames.add("local_address");
    labelNames.add("method");
    if (enabledMetricLabelValues.contains(MetricLabel.useHost)) {
      labelNames.add("host");
    }
    if (enabledMetricLabelValues.contains(MetricLabel.usePath)) {
      labelNames.add("path");
    }

    labelNames.add("state");

    return gauges.computeIfAbsent("vertx_" + name + "_requests", key -> register(Gauge.build(key, "HTTP requests number")
        .labelNames(labelNames.toArray(new String[labelNames.size()]))
        .create()));
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A gauge for websockets, identified by the given name. Gauges with the same name are shared.
   */
  public Gauge websockets(String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_websockets", key -> register(Gauge.build(key, "Websockets number")
        .labelNames("local_address").create()));
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A gauge for connections, identified by the given name. Gauges with the same name are shared.
   */
  public Gauge connections(String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_connections", key -> register(Gauge.build(key, "Active connections number")
        .labelNames("local_address").create()));
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A gauge for endpoints, identified by the given name. Gauges with the same name are shared.
   */
  public Gauge endpoints(String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_endpoints", key -> register(Gauge.build(key, "Endpoints number")
        .labelNames("local_address", "state").create()));
  }

  private Gauge register(Gauge gauge) {
    registry.register(gauge);
    return gauge;
  }
}
