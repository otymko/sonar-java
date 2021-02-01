/*
 * SonarQube Java
 * Copyright (C) 2012-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.java.checks.methods.AbstractMethodDetection;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.NewArrayTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

@Rule(key = "S4036")
public class OSCommandsPathCheck extends AbstractMethodDetection {
  private static final String STRING_ARRAY_TYPE = "java.lang.String[]";
  private static final String STRING_TYPE = "java.lang.String";

  private static final MethodMatchers EXEC_MATCHER = MethodMatchers.create()
    .ofTypes("java.lang.Runtime")
    .names("exec")
    .addParametersMatcher(STRING_TYPE)
    .addParametersMatcher(STRING_TYPE, STRING_ARRAY_TYPE)
    .addParametersMatcher(STRING_TYPE, STRING_ARRAY_TYPE, "java.io.File")
    .addParametersMatcher(STRING_ARRAY_TYPE)
    .addParametersMatcher(STRING_ARRAY_TYPE, STRING_ARRAY_TYPE)
    .addParametersMatcher(STRING_ARRAY_TYPE, STRING_ARRAY_TYPE, "java.io.File")
    .build();

  private static final List<String> STARTS = Arrays.asList(
    "/",
    "./",
    "../",
    "\\",
    ".\\",
    "..\\"
  );
  private static final String MESSAGE = "Make sure the \"PATH\" used to find this command includes only what you intend.";

  @Override
  protected MethodMatchers getMethodInvocationMatchers() {
    return EXEC_MATCHER;
  }

  private static boolean isCompliant(String command) {
    return STARTS.stream().anyMatch(command::startsWith);
  }

  @Override
  protected void onMethodInvocationFound(MethodInvocationTree tree) {
    Arguments arguments = tree.arguments();
    ExpressionTree expressionTree = arguments.get(0);
    if (expressionTree.is(Tree.Kind.STRING_LITERAL)) {
      Optional<String> command = expressionTree.asConstant(String.class);
      if (!command.isPresent() || isCompliant(command.get())) {
        return;
      }
      reportIssue(tree, MESSAGE);
    } else if (expressionTree.is(Tree.Kind.NEW_ARRAY)) {
      if (isNewArrayCommandValid(expressionTree)) {
        return;
      }
      reportIssue(tree, MESSAGE);
    } else if (expressionTree.is(Tree.Kind.IDENTIFIER)) {
      IdentifierTree identifier = (IdentifierTree) expressionTree;
      if (isIdentifierCommandValid(identifier)) {
        return;
      }
      reportIssue(tree, MESSAGE);
    }
  }

  private boolean isIdentifierCommandValid(IdentifierTree identifier) {
    Symbol symbol = identifier.symbol();
    Type type = symbol.type();
    if (type.is(STRING_TYPE)) {
      Optional<String> command = identifier.asConstant(String.class);
      return !command.isPresent() || isCompliant(command.get());
    } else if (type.is(STRING_ARRAY_TYPE)) {
      Tree declaration = symbol.declaration();
      if (declaration == null || !declaration.is(Tree.Kind.VARIABLE)) {
        return true;
      }
      VariableTree variable = (VariableTree) declaration;
      ExpressionTree initializer = variable.initializer();
      if (initializer == null || !initializer.is(Tree.Kind.NEW_ARRAY)) {
        return true;
      }
      return isNewArrayCommandValid(initializer);
    }
    return false;
  }

  private boolean isNewArrayCommandValid(ExpressionTree arrayInitializer) {
    NewArrayTree newArray = (NewArrayTree) arrayInitializer;
    if (!newArray.symbolType().is(STRING_ARRAY_TYPE)) {
      return false;
    }
    NewArrayArgumentVisitor visitor = new NewArrayArgumentVisitor();
    newArray.accept(visitor);
    return visitor.isCompliant;
  }

  static class NewArrayArgumentVisitor extends BaseTreeVisitor {
    private boolean firstArgumentVisited = false;
    private boolean isCompliant = false;

    @Override
    public void visitLiteral(LiteralTree tree) {
      if (firstArgumentVisited) {
        return;
      }
      firstArgumentVisited = true;
      Optional<String> command = tree.asConstant(String.class);
      if (!command.isPresent()) {
        return;
      }
      isCompliant = isCompliant(command.get());
    }
  }
}
