package com.baulsupp.oksocial.completion;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class UrlList {
  private List<String> urls;

  public UrlList(List<String> urls) {
    this.urls = urls;
  }

  public List<String> getUrls() {
    return urls;
  }

  public static Optional<UrlList> fromResource(String serviceName) throws IOException {
    URL url = UrlList.class.getResource("/urls/" + serviceName + ".txt");
    if (url != null) {
      return Optional.of(new UrlList(Resources.readLines(url, StandardCharsets.UTF_8)));
    } else {
      return Optional.empty();
    }
  }

  public List<String> matchingUrls(String prefix) {
    return urls.stream().filter(u -> u.startsWith(prefix)).collect(toList());
  }

  public UrlList replace(String variable, List<String> replacements, boolean keepTemplate) {
    if (replacements.isEmpty()) {
      return this;
    }

    String regexToken = "\\{" + variable + "\\}";
    String literalToken = "{" + variable + "}";

    final List<String> replacementList;
    if (keepTemplate) {
      replacementList = Lists.newArrayList(replacements);
      if (keepTemplate) {
        replacementList.add(literalToken);
      }
    } else {
      replacementList = replacements;
    }

    Function<String, Stream<String>> replacementFunction =
        url -> url.contains("{") ? replacementList.stream().map(s -> url.replaceAll(regexToken, s))
            : Stream.of(url);

    List<String> newUrls = urls.stream().flatMap(replacementFunction).collect(toList());

    return new UrlList(newUrls);
  }
}
