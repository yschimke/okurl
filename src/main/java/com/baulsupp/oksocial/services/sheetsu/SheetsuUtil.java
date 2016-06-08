package com.baulsupp.oksocial.services.sheetsu;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

public class SheetsuUtil {
  private SheetsuUtil() {
  }

  public static final Set<String> API_HOSTS =
      Collections.unmodifiableSet(Sets.newHashSet(
          "sheetsu.com")
      );
}
