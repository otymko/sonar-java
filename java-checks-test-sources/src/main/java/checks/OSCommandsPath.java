package checks;

import java.io.File;
import java.io.IOException;

public class OSCommandsPath {
  private static final String NON_COMPLIANT_COMMAND = "make";
  private static final String COMPLIANT_COMMAND = "/usr/bin/make.exe";

  private static final String[] NON_COMPLIANT_COMMAND_ARRAY = new String[]{"make"};
  private static final String[] COMPLIANT_COMMAND_ARRAY = new String[]{"/usr/bin/make.exe"};

  private static final String[] ENVIRONMENT = new String[]{"DEBUG=true"};
  private static final File FILE = null;

  public void execString() throws IOException {
    Runtime.getRuntime().exec("make");  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec("/usr/bin/make.exe");  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND);  // Compliant

    String nonCompliantCommand = "make";
    Runtime.getRuntime().exec(nonCompliantCommand);  // Compliant FN Cannot read from non-final strings
    String compliantCommand = "/usr/bin/make.exe";
    Runtime.getRuntime().exec(compliantCommand);  // Compliant

    Runtime.getRuntime().exec("make", ENVIRONMENT);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec("/usr/bin/make.exe", ENVIRONMENT);  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND, ENVIRONMENT);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND, ENVIRONMENT);  // Compliant

    Runtime.getRuntime().exec(nonCompliantCommand, ENVIRONMENT);  // Compliant FN Cannot read from non-final strings
    Runtime.getRuntime().exec(compliantCommand, ENVIRONMENT);  // Compliant

    Runtime.getRuntime().exec("make", ENVIRONMENT, FILE);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec("/usr/bin/make.exe", ENVIRONMENT, FILE);  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND);  // Compliant

    Runtime.getRuntime().exec(nonCompliantCommand, ENVIRONMENT, FILE);  // Compliant FN Cannot read from non-final strings
    Runtime.getRuntime().exec(compliantCommand, ENVIRONMENT, FILE);  // Compliant
  }

  private void execArray() throws IOException {
    Runtime.getRuntime().exec(new String[]{"make"});  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(new String[]{"/usr/bin/make.exe"});  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND_ARRAY);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND_ARRAY);  // Compliant

    String[] nonCompliantCommandArray = new String[]{"make"};
    Runtime.getRuntime().exec(nonCompliantCommandArray);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    String[] compliantCommandArray = new String[]{"/usr/bin/make.exe"};
    Runtime.getRuntime().exec(compliantCommandArray);  // Compliant

    Runtime.getRuntime().exec(new String[]{"make"}, ENVIRONMENT);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(new String[]{"/usr/bin/make.exe"}, ENVIRONMENT);  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND_ARRAY, ENVIRONMENT);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND_ARRAY, ENVIRONMENT);  // Compliant

    Runtime.getRuntime().exec(nonCompliantCommandArray, ENVIRONMENT);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(compliantCommandArray, ENVIRONMENT);  // Compliant

    Runtime.getRuntime().exec(new String[]{"make"}, ENVIRONMENT, FILE);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(new String[]{"/usr/bin/make.exe"}, ENVIRONMENT, FILE);  // Compliant

    Runtime.getRuntime().exec(NON_COMPLIANT_COMMAND_ARRAY, ENVIRONMENT, FILE);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(COMPLIANT_COMMAND_ARRAY, ENVIRONMENT, FILE);  // Compliant

    Runtime.getRuntime().exec(nonCompliantCommandArray, ENVIRONMENT, FILE);  // Noncompliant {{Make sure the "PATH" used to find this command includes only what you intend.}}
    Runtime.getRuntime().exec(compliantCommandArray, ENVIRONMENT, FILE);  // Compliant
  }
}
