package com.baulsupp.oksocial.completion;

import com.baulsupp.oksocial.Main;

import static com.google.common.collect.Lists.newArrayList;

public class UrlCompleterMain {
  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.initialise();

    main.arguments =
        newArrayList(args.length > 0 ? args[0] : "https://graph.facebook.com/BooneStudio");
    main.run();
  }
}
