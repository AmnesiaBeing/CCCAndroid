package edu.cuc.ccc;

import org.junit.Test;

import edu.cuc.ccc.packets.DeviceInfo;

public class DeviceInfoTest {

    // OK:{"Ver":1,"DevType":"PC","DevName":"AAA","DevMACAddr":[{"type":"LAN","addr":"aa:bb:cc:dd:ee:ff"}]}
    private String testJSONString1 = "{\"Ver\":1,\"DevType\":\"PC\",\"DevName\":\"AAA\",\"DevMACAddr\":[{\"type\":\"LAN\",\"addr\":\"aa:bb:cc:dd:ee:ff\"}]}";
    // not json
    private String testJSONString2 = "asdflaghaadh{asdg,.asdf}";
    // miss
    private String testJSONString3 = "{\"Ver\":1,\"DevType\":\"ss\",\"DevName\":\"AAA\",\"DevMACAddr\":[{\"type\":\"LAN\",\"addr\":\"aa:bb:cc:dd:ee:ff\"}]}";

    @Test
    public void testParseDeviceInfoJson1() {
        // test OK
        DeviceInfo.parseJSONStr(testJSONString1);
    }

    //    @Test(expected = Exception.class)
    public void testParseDeviceInfoJson2() {
        // test OK
        try {
//            DeviceInfo.parseJSONStr(testJSONString2);
        } catch (Exception e) {

        }
    }

    @Test
    public void testParseDeviceInfoJson3() {
        // test OK
        DeviceInfo info = DeviceInfo.parseJSONStr(testJSONString3);
        assert (info != null);
        assert (info.getDeviceType() == DeviceInfo.DeviceType.Unknown);
    }
}
