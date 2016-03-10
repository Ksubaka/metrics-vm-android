package com.ksubaka.metrics_vm_android;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of gauges for Android VM memory usage.
 */
public class MemoryUsageGaugeSet implements MetricSet {

    private final ActivityManager mActivityManager;

    public MemoryUsageGaugeSet(Context context) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        putMemoryInfo(gauges, "total");

        putMemoryByProcesses(gauges);

        return Collections.unmodifiableMap(gauges);
    }

    private void putMemoryInfo(Map<String, Metric> gauges, final String name) {
        final ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(info);

        // Prefer the used memory instead.
        /*
        gauges.put(name + ".availMem", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return info.availMem;
            }
        });/**/

        gauges.put(name + ".totalMem", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return info.totalMem;
            }
        });

        gauges.put(name + ".usedMem", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return info.totalMem - info.availMem;
            }
        });

        // Not very useful
        /*
        gauges.put(name + ".lowMemory", new Gauge<Boolean>() {
            @Override
            public Boolean getValue() {
                return info.lowMemory;
            }
        });

        gauges.put(name + ".threshold", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return info.threshold;
            }
        });
        /**/
    }

    private void putMemoryByProcesses(Map<String, Metric> gauges) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = mActivityManager.getRunningAppProcesses();
        int[] pids = new int[runningAppProcesses.size()];
        String[] names = new String[runningAppProcesses.size()];
        int index = 0;
        for (ActivityManager.RunningAppProcessInfo runningApp : runningAppProcesses) {
            pids[index] = runningApp.pid;
            String processName = runningApp.processName;
            if (processName.contains(".")) {
                processName = processName.substring(processName.lastIndexOf(".") + 1);
            }
            names[index] = processName;
            index++;
        }
        index = 0;
        for (Debug.MemoryInfo processMemory : mActivityManager.getProcessMemoryInfo(pids)) {
            putProcessMemoryInfo(gauges, names[index++], processMemory);
        }
    }

    private void putProcessMemoryInfo(Map<String, Metric> gauges, String name, final Debug.MemoryInfo processMemory) {
        gauges.put(name + ".pss", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.getTotalPss() * 1024L;
            }
        });
        /*
        gauges.put(name + ".privateClean", new Gauge<Long>() {
            @Override
            public Long getValue() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return processMemory.getTotalPrivateClean() * 1024L;
                }
                return 0L;
            }
        });
        gauges.put(name + ".privateDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.getTotalPrivateDirty() * 1024L;
            }
        });
        gauges.put(name + ".sharedClean", new Gauge<Long>() {
            @Override
            public Long getValue() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return processMemory.getTotalSharedClean() * 1024L;
                }
                return 0L;
            }
        });
        gauges.put(name + ".sharedDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.getTotalSharedDirty() * 1024L;
            }
        });

        /*
        gauges.put(name + ".dalvikPrivateDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.dalvikPrivateDirty * 1024L;
            }
        });
        gauges.put(name + ".dalvikPss", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.dalvikPss * 1024L;
            }
        });
        gauges.put(name + ".dalvikSharedDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.dalvikSharedDirty * 1024L;
            }
        });

        gauges.put(name + ".nativePrivateDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.nativePrivateDirty * 1024L;
            }
        });
        gauges.put(name + ".nativePss", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.nativePss * 1024L;
            }
        });
        gauges.put(name + ".nativeSharedDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.nativeSharedDirty * 1024L;
            }
        });

        gauges.put(name + ".otherPrivateDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.otherPrivateDirty * 1024L;
            }
        });
        gauges.put(name + ".otherPss", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.otherPss;
            }
        });
        gauges.put(name + ".otherSharedDirty", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return processMemory.otherSharedDirty * 1024L;
            }
        });
        /**/

    }


}
