// IMonitorService.aidl
package com.bfr.main.sysresmonitor;

interface IMonitorService {
    float memoryInfo(int stat);
    float[] cpuRate();
    float gpuRate();
    float cpuFreq(int id);
    float cpuTemp();
}