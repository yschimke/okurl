package com.baulsupp.oksocial.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Protocol;

public class ProtocolUtil {
  public static List<Protocol> parseProtocolList(String protocols) {
    List<Protocol> protocolValues = new ArrayList<>();

    try {
      for (String protocol : protocols.split(",")) {
        protocolValues.add(Protocol.get(protocol));
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }

    if (!protocolValues.contains(Protocol.HTTP_1_1)) {
      protocolValues.add(Protocol.HTTP_1_1);
    }

    return protocolValues;
  }
}
