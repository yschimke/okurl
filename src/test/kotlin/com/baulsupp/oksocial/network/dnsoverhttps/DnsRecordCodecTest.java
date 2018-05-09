package com.baulsupp.oksocial.network.dnsoverhttps;

import okio.ByteString;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DnsRecordCodecTest {
  @Test
  public void testGoogleDotComEncoding() throws Exception {
    String encoded = encodeQuery("google.com", false);

    assertEquals("AAABAAABAAAAAAAABmdvb2dsZQNjb20AAAEAAQ", encoded);
  }

  private String encodeQuery(String host, boolean includeIpv6) {
    return DnsRecordCodec.INSTANCE.encodeQuery(host, includeIpv6).base64Url().replace("=", "");
  }

  @Test
  public void testGoogleDotComEncodingWithIPv6() throws Exception {
    String encoded = encodeQuery("google.com", true);

    assertEquals("AAABAAACAAAAAAAABmdvb2dsZQNjb20AAAEAAQZnb29nbGUDY29tAAAcAAE", encoded);
  }

  @Test
  public void testGoogleDotComDecodingFromCloudflare() throws Exception {
    List<InetAddress> encoded = DnsRecordCodec.INSTANCE.decodeAnswers("test.com", ByteString.decodeHex(
      "00008180000100010000000006676f6f676c6503636f6d0000010001c00c00010001000000430004d83ad54e"));

    assertEquals(Collections.singletonList(InetAddress.getByName("216.58.213.78")), encoded);
  }

  @Test
  public void testGoogleDotComDecodingFromGoogle() throws Exception {
    List<InetAddress> decoded = DnsRecordCodec.INSTANCE.decodeAnswers("test.com", ByteString.decodeHex(
      "0000818000010003000000000567726170680866616365626f6f6b03636f6d0000010001c00c0005000100000a6d000603617069c012c0300005000100000cde000c04737461720463313072c012c042000100010000003b00049df00112"));

    assertEquals(Collections.singletonList(InetAddress.getByName("157.240.1.18")), decoded);
  }

  @Test
  public void testGoogleDotComDecodingFromGoogleIPv6() throws Exception {
    List<InetAddress> decoded = DnsRecordCodec.INSTANCE.decodeAnswers("test.com", ByteString.decodeHex(
      "0000818000010003000000000567726170680866616365626f6f6b03636f6d00001c0001c00c0005000100000a1b000603617069c012c0300005000100000b1f000c04737461720463313072c012c042001c00010000003b00102a032880f0290011faceb00c00000002"));

    assertEquals(
      Collections.singletonList(InetAddress.getByName("2a03:2880:f029:11:face:b00c:0:2")),
      decoded);
  }

  @Test
  public void testGoogleDotComDecodingNxdomainFailure() throws Exception {
    try {
      DnsRecordCodec.INSTANCE.decodeAnswers("sdflkhfsdlkjdf.ee", ByteString.decodeHex(
        "0000818300010000000100000e7364666c6b686673646c6b6a64660265650000010001c01b00060001000007070038026e7303746c64c01b0a686f73746d61737465720d6565737469696e7465726e6574c01b5adb12c100000e10000003840012750000000e10"));
      fail();
    } catch (UnknownHostException uhe) {
      assertEquals("sdflkhfsdlkjdf.ee: NXDOMAIN", uhe.getMessage());
    }
  }
}
