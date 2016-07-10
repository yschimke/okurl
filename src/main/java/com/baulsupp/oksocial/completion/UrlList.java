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
  public enum Match {
    EXACT, SITE, HOSTS
  }

  private Match match;
  private List<String> urls;

  public UrlList(Match match, List<String> urls) {
    this.match = match;
    this.urls = urls;
  }

  public List<String> getUrls(String prefix) {
    return urls.stream().filter(u -> u.startsWith(prefix)).collect(toList());
  }

  public static Optional<UrlList> fromResource(String serviceName)
      throws IOException {
    URL url = UrlList.class.getResource("/urls/" + serviceName + ".txt");
    if (url != null) {
      return Optional.of(
          new UrlList(Match.SITE, Resources.readLines(url, StandardCharsets.UTF_8)));
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

    return new UrlList(match, newUrls);
  }

  public void toFile(File file, int strip, String prefix) throws IOException {
    String content = regex(prefix) + "\n" + urls.stream()
        .map(u -> u.substring(strip))
        .collect(joining("\n"));

    Files.write(content, file, StandardCharsets.UTF_8);
  }

  private String regex(String prefix) {
    switch (match) {
      case EXACT:
        return prefix;
      case HOSTS:
        return "[^/]*:?/?/?[^/]*";
      case SITE:
        return prefix + ".*";
      default:
        throw new IllegalArgumentException();
    }
  }

  public UrlList combine(UrlList b) {
    List<String> newUrls = Lists.newArrayList();

    newUrls.addAll(urls);
    newUrls.addAll(b.urls);

    Match newMatch;
    if (match == b.match) {
      newMatch = match;
    } else {
      newMatch = Match.EXACT;
    }

    return new UrlList(newMatch, newUrls);
  }

  @Override public String toString() {
    return urls.stream().collect(joining("\n"));
  }

  @Override public boolean equals(Object obj) {
    if (!(obj instanceof UrlList)) {
      return false;
    }

    UrlList other = ((UrlList) obj);

    return other.match == this.match && other.urls.equals(this.urls);
  }

  @Override public int hashCode() {
    return match.hashCode() ^ urls.hashCode();
  }
}
