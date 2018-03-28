package io.vertx.ext.prometheus;

/**
 * @author marcus
 * @since 1.0.0
 */
public enum MetricLabel {

    /**
     * Use the Host as a label value in the http client based metrics
     */
    useHost,


    /**
     * Use the Path as a label value in the http client based metrics
     */
    usePath;

}
