package edu.cuc.ccc.backends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cuc.ccc.Device;

// 负责管理发现的设备（包括自己）
public class DeviceManager {

    // This Device
    private Device myDevice = new Device() {{
        setPairCode("123456");
        setDeviceName("test");
    }};

    // Name,Device
    private Map<String, Device> knownDevices = new HashMap<>();
    private Device pairedDevice;

    // TODO:SharePreference
    public Device getLastPairedDevice() {
        return null;
    }

    public Device getPairedDevice() {
        return pairedDevice;
    }

    // TODO:新发现了一个设备，根据UUID合并？
    public void putNewFoundDevice(Device device) {
        knownDevices.put(device.getDeviceName(), device);
    }

    public void updateFoundDevice(Device device) {

    }

    public List<Device> getDevices() {
        return new ArrayList<>(knownDevices.values());
    }

    public void requestPairDevice(Device device) {
        if (device != pairedDevice) {
            if (device.getPairCode().isEmpty()) {

            } else {

            }
        }
    }

    public Device getMyDevice() {
        return myDevice;
    }
}
