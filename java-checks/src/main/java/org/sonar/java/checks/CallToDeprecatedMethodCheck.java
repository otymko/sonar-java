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

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "CallToDeprecatedMethod", repositoryKey = "squid")
@Rule(key = "S1874")
public class CallToDeprecatedMethodCheck extends AbstractCallToDeprecatedCodeChecker {

  @Override
  void checkDeprecatedIdentifier(IdentifierTree identifierTree, Symbol deprecatedSymbol) {
    if (isFlaggedForRemoval(deprecatedSymbol)) {
      // do not overlap with S5738
      return;
    }
    String name = deprecatedSymbol.name();
    if (isConstructor(deprecatedSymbol)) {
      name = deprecatedSymbol.owner().name();
    }
    reportIssue(identifierTree, String.format("Remove this use of \"%s\"; it is deprecated.", name));
  }

  @Override
  void checkOverridingMethod(MethodTree methodTree, Symbol.MethodSymbol deprecatedSymbol) {
    if (isFlaggedForRemoval(deprecatedSymbol)) {
      // do not overlap with S5738
      return;
    }
    reportIssue(methodTree.simpleName(), "Don't override a deprecated method or explicitly mark it as \"@Deprecated\".");
  }
}
