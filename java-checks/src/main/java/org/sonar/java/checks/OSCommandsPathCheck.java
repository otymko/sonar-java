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
  private static final MethodMatchers EXEC_MATCHER = MethodMatchers.create()
    .ofTypes("java.lang.Runtime")
    .names("exec")
    .addParametersMatcher("java.lang.String")
    .addParametersMatcher("java.lang.String[]")
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
      NewArrayTree newArray = (NewArrayTree) expressionTree;
      if (!newArray.symbolType().is("java.lang.String[]")) {
        return;
      }
      NewArrayArgumentVisitor visitor = new NewArrayArgumentVisitor();
      newArray.accept(visitor);
      if (visitor.isCompliant) {
        return;
      }
      reportIssue(tree, MESSAGE);
    } else if (expressionTree.is(Tree.Kind.IDENTIFIER)) {
      IdentifierTree identifier = (IdentifierTree) expressionTree;
      Symbol symbol = identifier.symbol();
      Type type = symbol.type();
      if (type.is("java.lang.String")) {
        Optional<String> command = identifier.asConstant(String.class);
        if (!command.isPresent() || isCompliant(command.get())) {
          return;
        }
        reportIssue(tree, MESSAGE);
      } else if (type.is("java.lang.String[]")) {
        Tree declaration = symbol.declaration();
        if (!declaration.is(Tree.Kind.VARIABLE)) {
          return;
        }
        VariableTree variable = (VariableTree) declaration;
        ExpressionTree initializer = variable.initializer();
        if (!initializer.is(Tree.Kind.NEW_ARRAY)) {
          return;
        }
        NewArrayTree newArray = (NewArrayTree) initializer;
        if (!newArray.symbolType().is("java.lang.String[]")) {
          return;
        }
        NewArrayArgumentVisitor visitor = new NewArrayArgumentVisitor();
        newArray.accept(visitor);
        if (visitor.isCompliant) {
          return;
        }
        reportIssue(tree, MESSAGE);
      }
    }
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
