package checks;

import java.io.IOException;

public class OSCommandsPath {
  public void calls() throws IOException {
    Runtime.getRuntime().exec("make");  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec("/usr/bin/make.exe");  // Compliant

    String nonCompliantCommand = "make";
    Runtime.getRuntime().exec(nonCompliantCommand);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    String compliantCommand = "/usr/bin/make.exe";
    Runtime.getRuntime().exec(compliantCommand);  // Compliant

    Runtime.getRuntime().exec(new String[]{"make"});  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(new String[]{"/usr/bin/make.exe"});  // Compliant

    String[] nonCompliantCommandArray = new String[]{"make"};
    Runtime.getRuntime().exec(nonCompliantCommandArray);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    String[] compliantCommandArray = new String[]{"/usr/bin/make.exe"};
    Runtime.getRuntime().exec(compliantCommandArray);  // Compliant
  }
}
