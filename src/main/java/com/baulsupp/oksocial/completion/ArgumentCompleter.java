package com.baulsupp.oksocial.completion;

import java.io.IOException;

public interface ArgumentCompleter {
  UrlList urlList(String prefix) throws IOException;
}
