package com.baulsupp.oksocial.completion;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static java.util.stream.Collectors.toList;

public class CompletionQuery {
  public static List<String> getIds(OkHttpClient client, String url, String path, String key)
      throws IOException {
    try {
      Map<String, Object> map =
          AuthUtil.makeJsonMapRequest(client, new Request.Builder().url(url).build());

      List<Map<String, Object>> surveys = (List<Map<String, Object>>) map.get(path);

      return surveys.stream().map(m -> (String) m.get(key)).collect(toList());
    } catch (IOException ioe) {
      return Lists.newArrayList();
    }
  }
}
