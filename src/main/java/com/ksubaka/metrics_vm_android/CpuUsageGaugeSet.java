package com.ksubaka.metrics_vm_android;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of gauges for Android VM memory usage.
 */
public class CpuUsageGaugeSet implements MetricSet {

    private final ActivityManager mActivityManager;
    private long mThreadCpuTimeNanos;

    public CpuUsageGaugeSet(Context context) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        mThreadCpuTimeNanos = Debug.threadCpuTimeNanos();
        if (mThreadCpuTimeNanos > 0) {
            gauges.put("threadCpuTimeNanos", new Gauge<Long>() {
                @Override
                public Long getValue() {
                    final long threadCpuTimeNanos = Debug.threadCpuTimeNanos();
                    final long val = threadCpuTimeNanos - mThreadCpuTimeNanos;
                    mThreadCpuTimeNanos = threadCpuTimeNanos;
                    return val;
                }
            });
        }

        return Collections.unmodifiableMap(gauges);
    }

}
