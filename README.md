# vertx-prometheus-metrics

[Prometheus](https://prometheus.io/) implementation of the [Vert.x Metrics SPI](http://vertx.io/docs/vertx-core/java/index.html#_metrics_spi).

Based on [vertx-prometheus-metrics](https://github.com/nolequen/vertx-prometheus-metrics)


## Usage

You can find latest release on Maven Central.

* Maven:
```xml
<dependency>
  <groupId>com.hubrick.vertx</groupId>
  <artifactId>vertx-prometheus-metrics</artifactId>
  <version>3.5.1</version>
</dependency>
```

* Gradle:
```groovy
compile group: 'su.nlq', name: 'vertx-prometheus-metrics', version: '3.5.1'
```

Now you can set and enable Vert.x metrics:
```java
final Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
    new VertxPrometheusOptions().setEnabled(true)
));
```

## Compatibility

| Metrics    | Vert.x     | Prometheus | 
| ----------:| ----------:| ----------:|
| **3.5.1**  | 3.5.1      | 0.2.0      |

## Options

There are some special options you can use:

* Enable or disable specific `MetricsType` or check their state (all metrics are enabled by default)
* Specify which Prometheus `CollectorRegistry` should be used (unless otherwise specified, the default one is used)

## Metrics

The following metrics are provided.

### Vert.x metrics

* `vertx_timers_number` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of timers by state
* `vertx_verticle_number` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the currently deployed verticles number by class

### Event bus metrics

* `vertx_eventbus_handlers` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the registered message handlers number
* `vertx_eventbus_respondents` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the registered message reply-handlers number
* `vertx_eventbus_messages` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of messages by range (local or remote), state and address
* `vertx_eventbus_failures` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the number of messages handling failures by address, message type and reason
* `vertx_eventbus_messages_time_seconds` - [histogram](https://prometheus.io/docs/concepts/metric_types/#histogram) representing the total processing time (in seconds) of the messages by address and type
* `vertx_eventbus_bytes` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the read\written bytes number by address

### HTTP server metrics

* `vertx_httpserver_requests` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of processing requests by address, HTTP method, path and state
* `vertx_httpserver_responses` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the responses number by address and status code
* `vertx_httpserver_requests_time_seconds` - [histogram](https://prometheus.io/docs/concepts/metric_types/#histogram) of the total processing time (in seconds) of the requests by address
* `vertx_httpserver_websockets` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of the connected websockets    
* `vertx_httpserver_connections` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the active connections number
* `vertx_httpserver_bytes` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the read\written bytes number by address
* `vertx_httpserver_errors` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the number of errors occurred by address

### HTTP client metrics

* `vertx_httpclient_endpoints` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of endpoints number by address and state
* `vertx_httpclient_endpoints_queue_time_seconds` - [histogram](https://prometheus.io/docs/concepts/metric_types/#histogram) of the total queue time (in seconds) of pending endpoints
* `vertx_httpclient_requests` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of processing requests by address, HTTP method, path and state
* `vertx_httpclient_requests_time_seconds` - [histogram](https://prometheus.io/docs/concepts/metric_types/#histogram) of the total processing time (in seconds) of the requests by address
* `vertx_httpclient_responses` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the responses number by address and status code
* `vertx_httpclient_websockets` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of the connected websockets
* `vertx_httpclient_connections` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the active connections number
* `vertx_httpclient_bytes` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the read\written bytes number by address
* `vertx_httpclient_errors` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the number of errors occurred by address

### Net server metrics

* `vertx_netserver_connections` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the active connections number
* `vertx_netserver_bytes` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the read\written bytes number by address
* `vertx_netserver_errors` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the number of errors occurred by address

### Net client metrics

* `vertx_netclient_connections` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the active connections number
* `vertx_netclient_bytes` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the read\written bytes number by address
* `vertx_netclient_errors` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the number of errors occurred by address


### Datagram socket metrics

* `vertx_datagram_socket_bytes` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the read\written bytes number by address
* `vertx_datagram_socket_errors` - [counter](https://prometheus.io/docs/concepts/metric_types/#counter) of the number of errors occurred by address

### Pool metrics

* `vertx_pool_tasks` - [gauge](https://prometheus.io/docs/concepts/metric_types/#gauge) of the number of processing tasks by pool and state
* `vertx_pool_time_seconds` - [histogram](https://prometheus.io/docs/concepts/metric_types/#histogram) representing the total processing time (in seconds) of the tasks in a certain state
