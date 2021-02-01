package checks;

import java.io.IOException;

public class OSCommandsPath {
  public void calls() throws IOException {
    Runtime.getRuntime().exec("make");  // Sensitive
    Runtime.getRuntime().exec("/usr/bin/make.exe");  // Compliant
  }
}
