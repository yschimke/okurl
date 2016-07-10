package com.baulsupp.oksocial.completion;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class UrlList {
  private final String regex;
  private List<String> urls;

  public UrlList(String regex, List<String> urls) {
    this.regex = regex;
    this.urls = urls;
  }

  public List<String> getUrls(String prefix) {
    return urls.stream().filter(u -> u.startsWith(prefix)).collect(toList());
  }

  public static Optional<UrlList> fromResource(String regex, String serviceName)
      throws IOException {
    URL url = UrlList.class.getResource("/urls/" + serviceName + ".txt");
    if (url != null) {
      return Optional.of(new UrlList(regex, Resources.readLines(url, StandardCharsets.UTF_8)));
    } else {
      return Optional.empty();
    }
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

    return new UrlList(regex, newUrls);
  }

  public void toFile(File file, int strip, Optional<String> mainRegex) throws IOException {
    String content =
        mainRegex.orElse(regex) + "\n" + urls.stream().map(u -> u.substring(strip)).collect(joining("\n"));

    Files.write(content, file, StandardCharsets.UTF_8);
  }

  public UrlList combine(UrlList b) {
    List<String> newUrls = Lists.newArrayList();

    newUrls.addAll(urls);
    newUrls.addAll(b.urls);

    return new UrlList(this.regex, newUrls);
  }

  @Override public String toString() {
    return urls.stream().collect(joining("\n"));
  }

  @Override public boolean equals(Object obj) {
    if (!(obj instanceof UrlList)) {
      return false;
    }

    UrlList other = ((UrlList) obj);

    return other.regex.equals(this.regex) && other.urls.equals(this.urls);
  }
}
