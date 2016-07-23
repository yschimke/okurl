package com.baulsupp.oksocial.iterm;

import com.baulsupp.oksocial.output.OsxOutputHandler;
import java.io.IOException;
import okhttp3.Response;
import okio.ByteString;

public class ItermOutputHandler extends OsxOutputHandler {
  public static final char ESC = (char)27;
  public static final char BELL = (char)7;

  // https://www.iterm2.com/documentation-images.html
  @Override public void openPreview(Response response) throws IOException {
    String b64 = ByteString.of(response.body().bytes()).base64();

    System.out.print(ESC + "]1337;File=inline=1:");
    System.out.print(b64);
    System.out.print(BELL + "\n");
  }

  public static boolean isAvailable() {
    String term = System.getenv("TERM_PROGRAM");
    String version = System.getenv("TERM_PROGRAM_VERSION");
    return "iTerm.app".equals(term) && version != null && version.startsWith("3.");
  }
}
