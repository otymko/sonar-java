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
package org.sonar.java.checks.security;

import java.util.Collections;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.java.checks.helpers.MethodTreeUtils;
import org.sonar.java.model.ExpressionUtils;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S5659")
public class JWTWithStrongCipherCheck extends IssuableSubscriptionVisitor {

  private static final String MESSAGE_STRONG_CIPHER = "Use only strong cipher algorithms when %s this JWT.";

  private static final String AUTH0_JWT_ALGORITHM = "com.auth0.jwt.algorithms.Algorithm";

  private static final MethodMatchers AUTH0_JWT_REQUIRE = MethodMatchers.create()
    .ofTypes("com.auth0.jwt.JWT")
    .names("require")
    .addParametersMatcher(AUTH0_JWT_ALGORITHM)
    .build();

  private static final MethodMatchers AUTH0_JWT_SIGN = MethodMatchers.create()
    .ofTypes("com.auth0.jwt.JWTCreator$Builder")
    .names("sign")
    .addParametersMatcher(AUTH0_JWT_ALGORITHM)
    .build();

  private static final MethodMatchers ALGORITHM_NONE = MethodMatchers.create()
    .ofTypes(AUTH0_JWT_ALGORITHM)
    .names("none")
    .addWithoutParametersMatcher()
    .build();

  private static final MethodMatchers JWTK_JJWT_PARSE = MethodMatchers.create()
    .ofTypes("io.jsonwebtoken.JwtParser")
    .names("parse")
    .addParametersMatcher("java.lang.String")
    .build();

  private static final MethodMatchers JWTK_JJWT_BUILDER = MethodMatchers.create()
    .ofTypes("io.jsonwebtoken.Jwts")
    .names("builder")
    .addWithoutParametersMatcher()
    .build();

  private static final MethodMatchers JWTK_JJWT_SIGN_WITH = MethodMatchers.create()
    .ofTypes("io.jsonwebtoken.JwtBuilder")
    .names("signWith")
    .withAnyParameters()
    .build();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
  }

  @Override
  public void visitNode(Tree tree) {
    MethodInvocationTree mit = (MethodInvocationTree) tree;
    handleAuth0Jwt(mit);
    handleJwtkJwt(mit);
  }

  private void handleAuth0Jwt(MethodInvocationTree mit) {
    if (AUTH0_JWT_REQUIRE.matches(mit)) {
      reportIfAlgorithmIsNone(mit.arguments().get(0), "verifying the signature of");
    } else if (AUTH0_JWT_SIGN.matches(mit)) {
      reportIfAlgorithmIsNone(mit.arguments().get(0), "signing");
    }
  }

  private void reportIfAlgorithmIsNone(ExpressionTree expressionTree, String action) {
    if (expressionTree.is(Tree.Kind.METHOD_INVOCATION) && ALGORITHM_NONE.matches((MethodInvocationTree) expressionTree)) {
      reportIssue(expressionTree, String.format(MESSAGE_STRONG_CIPHER, action));
    }
  }

  private void handleJwtkJwt(MethodInvocationTree mit) {
    if (JWTK_JJWT_PARSE.matches(mit)) {
      reportIssue(ExpressionUtils.methodName(mit), "The JWT signature (JWS) should be verified before using this token.");
    } else if (JWTK_JJWT_BUILDER.matches(mit) && !MethodTreeUtils.subsequentMethodInvocation(mit, JWTK_JJWT_SIGN_WITH).isPresent()) {
      reportIssue(ExpressionUtils.methodName(mit), "Sign this token using a strong cipher algorithm.");
    }
  }

}
