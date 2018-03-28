package io.vertx.ext.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.vertx.core.Closeable;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.*;
import io.vertx.ext.prometheus.metrics.*;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import io.vertx.ext.prometheus.server.MetricsServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.vertx.ext.prometheus.MetricsType.*;

public final class VertxPrometheusMetrics extends DummyVertxMetrics {
  private final @NotNull Vertx vertx;
  private final @NotNull VertxPrometheusOptions options;
  private final @NotNull VerticleMetrics verticleMetrics;
  private final @NotNull TimerMetrics timerMetrics;
  private final @NotNull GaugeFactory gauges;
  private final @NotNull CounterFactory counters;
  private final @NotNull HistogramFactory histograms;

  private @Nullable Closeable server;

  public VertxPrometheusMetrics(@NotNull Vertx vertx, @NotNull VertxPrometheusOptions options) {
    this.vertx = vertx;
    this.options = options;
    this.verticleMetrics = options.isEnabled(Verticles) ? new VerticlePrometheusMetrics(options.getRegistry()) : new VerticleDummyMetrics();
    this.timerMetrics = options.isEnabled(Timers) ? new TimerPrometheusMetrics(options.getRegistry()) : new TimerDummyMetrics();
    this.gauges = new GaugeFactory(options.getRegistry());
    this.counters = new CounterFactory(options.getRegistry());
    this.histograms = new HistogramFactory(options.getRegistry());
  }

  @Override
  public void eventBusInitialized(@NotNull EventBus bus) {
    if (options.isEmbeddedServerEnabled()) {
      server = MetricsServer
          .create(vertx)
          .apply(options.getRegistry(), options.getFormat())
          .apply(options.getAddress());
    }
  }

  @Override
  public void verticleDeployed(@NotNull Verticle verticle) {
    verticleMetrics.deployed(verticle);
  }

  @Override
  public void verticleUndeployed(@NotNull Verticle verticle) {
    verticleMetrics.undeployed(verticle);
  }

  @Override
  public void timerCreated(long id) {
    timerMetrics.created(id);
  }

  @Override
  public void timerEnded(long id, boolean cancelled) {
    timerMetrics.ended(id, cancelled);
  }

  @Override
  public @NotNull EventBusMetrics<?> createMetrics(@NotNull EventBus eventBus) {
    return options.isEnabled(EventBus)
        ? new EventBusPrometheusMetrics(options.getRegistry())
        : super.createMetrics(eventBus);
  }

  @Override
  public @NotNull HttpServerMetrics<?, ?, ?> createMetrics(@NotNull HttpServer httpServer, @NotNull SocketAddress localAddress, @NotNull HttpServerOptions httpServerOptions) {
    return options.isEnabled(HTTPServer)
        ? new HTTPServerPrometheusMetrics(options.getRegistry(), options.getEnabledServerMetricLabelValues(), localAddress, gauges, counters, histograms)
        : super.createMetrics(httpServer, localAddress, httpServerOptions);
  }

  @Override
  public @NotNull HttpClientMetrics<?, ?, ?, ?, ?> createMetrics(@NotNull HttpClient client, @NotNull HttpClientOptions httpClientOptions) {
    return options.isEnabled(HTTPClient)
        ? new HTTPClientPrometheusMetrics(options.getRegistry(), options.getEnabledClientMetricLabelValues(),
            getLocalAddress(httpClientOptions.getLocalAddress()), gauges, counters, histograms)
        : super.createMetrics(client, httpClientOptions);
  }

  @Override
  public @NotNull TCPMetrics<?> createMetrics(@NotNull SocketAddress localAddress, @NotNull NetServerOptions netServerOptions) {
    return options.isEnabled(NetServer)
        ? new NetServerPrometheusMetrics(options.getRegistry(), localAddress, gauges, counters)
        : super.createMetrics(localAddress, netServerOptions);
  }

  @Override
  public @NotNull TCPMetrics<?> createMetrics(@NotNull NetClientOptions netClientOptions) {
    return options.isEnabled(NetClient)
        ? new NetClientPrometheusMetrics(options.getRegistry(), getLocalAddress(netClientOptions.getLocalAddress()), gauges, counters)
        : super.createMetrics(netClientOptions);
  }

  @Override
  public @NotNull DatagramSocketMetrics createMetrics(@NotNull DatagramSocket socket, @NotNull DatagramSocketOptions datagramSocketOptions) {
    return options.isEnabled(DatagramSocket)
        ? new DatagramSocketPrometheusMetrics(options.getRegistry(), counters)
        : super.createMetrics(socket, datagramSocketOptions);
  }

  @Override
  public @NotNull <P> PoolMetrics<?> createMetrics(@NotNull P pool, @NotNull String poolType, @NotNull String poolName, int maxPoolSize) {
    return options.isEnabled(Pools)
        ? new PoolPrometheusMetrics(options.getRegistry(), poolType, poolName, maxPoolSize)
        : super.createMetrics(pool, poolType, poolName, maxPoolSize);
  }

  @Override
  public boolean isMetricsEnabled() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
    if (server != null) {
      server.close(event -> { /* do nothing */ });
    }

    gauges.close();
    counters.close();
    histograms.close();
  }

  private static @NotNull String getLocalAddress(@Nullable String address) {
    return address == null ? "unknown" : address;
  }

  private interface VerticleMetrics {
    void deployed(@NotNull Verticle verticle);

    void undeployed(@NotNull Verticle verticle);
  }

  private interface TimerMetrics {
    void created(long id);

    void ended(long id, boolean cancelled);
  }

  private static final class VerticlePrometheusMetrics extends PrometheusMetrics implements VerticleMetrics {
    private static final @NotNull Gauge collector =
        Gauge.build("vertx_verticle_number", "Deployed verticles number").labelNames("class").create();

    public VerticlePrometheusMetrics(@NotNull CollectorRegistry registry) {
      super(registry);
      register(collector);
    }

    @Override
    public void deployed(@NotNull Verticle verticle) {
      collector.labels(verticle.getClass().getName()).inc();
    }

    @Override
    public void undeployed(@NotNull Verticle verticle) {
      collector.labels(verticle.getClass().getName()).dec();
    }
  }

  private static final class TimerPrometheusMetrics extends PrometheusMetrics implements TimerMetrics {
    private static final @NotNull Gauge collector =
        Gauge.build("vertx_timers_number", "Timers number").labelNames("state").create();

    public TimerPrometheusMetrics(@NotNull CollectorRegistry registry) {
      super(registry);
      register(collector);
    }

    @Override
    public void created(long id) {
      collector.labels("created").inc();
      collector.labels("active").inc();
    }

    @Override
    public void ended(long id, boolean cancelled) {
      if (cancelled) {
        collector.labels("cancelled").inc();
      }
      collector.labels("destroyed").inc();
      collector.labels("active").dec();
    }
  }

  private final class VerticleDummyMetrics implements @NotNull VerticleMetrics {

    @Override
    public void deployed(@NotNull Verticle verticle) {
      VertxPrometheusMetrics.super.verticleDeployed(verticle);
    }

    @Override
    public void undeployed(@NotNull Verticle verticle) {
      VertxPrometheusMetrics.super.verticleUndeployed(verticle);
    }
  }

  private final class TimerDummyMetrics implements @NotNull TimerMetrics {

    @Override
    public void created(long id) {
      VertxPrometheusMetrics.super.timerCreated(id);
    }

    @Override
    public void ended(long id, boolean cancelled) {
      VertxPrometheusMetrics.super.timerEnded(id, cancelled);
    }
  }
}
