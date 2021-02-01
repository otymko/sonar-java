package checks;

import java.io.IOException;

public class OSCommandsPath {
  private static final String NON_COMPLIANT_COMMAND = "make";
  private static final String COMPLIANT_COMMAND = "/usr/bin/make.exe";

  private static final String[] NON_COMPLIANT_COMMAND_ARRAY = new String[]{"make"};
  private static final String[] COMPLIANT_COMMAND_ARRAY = new String[]{"/usr/bin/make.exe"};

  public void calls() throws IOException {
    Runtime.getRuntime().exec("make");  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec("/usr/bin/make.exe");  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND);  // Compliant

    String nonCompliantCommand = "make";
    Runtime.getRuntime().exec(nonCompliantCommand);  // Compliant FN Cannot read from non-final strings
    String compliantCommand = "/usr/bin/make.exe";
    Runtime.getRuntime().exec(compliantCommand);  // Compliant

    Runtime.getRuntime().exec(new String[]{"make"});  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(new String[]{"/usr/bin/make.exe"});  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND_ARRAY);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND_ARRAY);  // Compliant

    String[] nonCompliantCommandArray = new String[]{"make"};
    Runtime.getRuntime().exec(nonCompliantCommandArray);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    String[] compliantCommandArray = new String[]{"/usr/bin/make.exe"};
    Runtime.getRuntime().exec(compliantCommandArray);  // Compliant
  }
}
