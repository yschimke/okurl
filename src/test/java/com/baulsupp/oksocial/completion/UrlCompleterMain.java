package com.baulsupp.oksocial.completion;

import com.baulsupp.oksocial.Main;

public class UrlCompleterMain {
  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.initialise();

    System.out.println("=");
    main.urlCompletion = args.length > 0 ? args[0] : "https://graph.facebook.com/BooneStudio";
    main.run();
    System.out.println("=");
  }
}
