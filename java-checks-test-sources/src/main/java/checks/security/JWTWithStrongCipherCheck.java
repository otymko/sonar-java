package checks.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static org.apache.commons.lang.time.DateUtils.addMinutes;

public class JWTWithStrongCipherCheck {

  private static final String SECRET_KEY = Base64.getEncoder().encodeToString("not-so-secret".getBytes());
  private static final String LOGIN = "login";

  /**
   * JSON web token handling with https://github.com/auth0/java-jwt
   */
  public void auth0JWT() {
    JWTVerifier nonCompliantVerifier = JWT.require(Algorithm.none()) // Noncompliant [[sc=52;ec=68]] {{Use only strong cipher algorithms when verifying the signature of this JWT.}}
      .withSubject(LOGIN)
      .build();

    JWTVerifier nonCompliantVerifier2 = JWT.require(none()) // Noncompliant
      .withSubject(LOGIN)
      .build();

    JWTVerifier nonCompliantVerifier3 = JWT.require(com.auth0.jwt.algorithms.Algorithm.none()) // Noncompliant
      .withSubject(LOGIN)
      .build();

    JWTVerifier compliantVerifier1 = JWT.require(Algorithm.HMAC256(SECRET_KEY)) // Compliant
      .withSubject(LOGIN)
      .build();

    JWTVerifier compliantVerifier2 = JWT.require(new MyAlgorithm("name", "description")) // Compliant
      .withSubject(LOGIN)
      .build();

    String tokenNotSigned = JWT.create()
      .withSubject(LOGIN) // Compliant
      .withExpiresAt(addMinutes(new Date(), 20))
      .withIssuedAt(new Date())
      .sign(Algorithm.none()); // Noncompliant [[sc=13;ec=29]] {{Use only strong cipher algorithms when signing this JWT.}}

    String tokenSigned = JWT.create()
      .withSubject(LOGIN) // Compliant
      .withExpiresAt(addMinutes(new Date(), 20))
      .withIssuedAt(new Date())
      .sign(Algorithm.HMAC256(SECRET_KEY)); // Compliant

  }

  /**
   * JSON web token handling with https://github.com/jwtk/jjwt
   */
  public void jwtkJWT() {
    String tokenNotSigned = getTokenNotSigned();
    String tokenSigned = getTokenSigned();

    // PARSE WITHOUT SIGNATURE TESTCASES
    Object body1 = Jwts.parser().parse(tokenNotSigned).getBody(); // Noncompliant [[sc=34;ec=39]] {{The JWT signature (JWS) should be verified before using this token.}}
    Object body2 = Jwts.parser().parse(tokenSigned).getBody(); // Noncompliant

    // Despite the fact that we set a signing key, parse is subject to the none algorithm. See rule description.
    Object body3 = Jwts.parser().setSigningKey(SECRET_KEY).parse(tokenNotSigned).getBody(); // Noncompliant [[sc=60;ec=65]] {{The JWT signature (JWS) should be verified before using this token.}}
    Object body4 = Jwts.parser().setSigningKey(SECRET_KEY).parse(tokenSigned).getBody(); // Noncompliant

    // parseClaimsJws WITH SIGNATURE TESTCASES
    Object body5 = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(tokenNotSigned).getBody(); // Compliant
    Object body6 = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(tokenSigned).getBody(); // Compliant

    // parseClaimsJwt WITHOUT SIGNATURE TESTCASES
    Object body7 = Jwts.parser().parseClaimsJwt(tokenNotSigned).getBody(); // Compliant
    Object body8 = Jwts.parser().parseClaimsJwt(tokenSigned).getBody(); // Compliant
  }

  private static String getTokenNotSigned() {
    return Jwts.builder() // Noncompliant [[sc=17;ec=24]] {{Sign this token using a strong cipher algorithm.}}
      .setId("123")
      .setSubject(LOGIN)
      .setIssuedAt(new Date())
      .setExpiration(addMinutes(new Date(), 20))
      .compact();
  }

  private String getTokenSignedWithNone() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
    DESKeySpec dks = new DESKeySpec("mysuperkey".getBytes());
    Key key = keyFactory.generateSecret(dks);

    return Jwts.builder() // Compliant, not accepted by JWTK  (throws IllegalArgumentException).
      .setId("123")
      .setSubject(LOGIN)
      .setIssuedAt(new Date())
      .setExpiration(addMinutes(new Date(), 20))
      .signWith(SignatureAlgorithm.NONE, key)
      .compact();
  }

  private static String getTokenSigned() {
    return Jwts.builder() // Compliant
      .setId("123")
      .setSubject(LOGIN)
      .setIssuedAt(new Date())
      .setExpiration(addMinutes(new Date(), 20))
      .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
      .compact();
  }


  private class MyAlgorithm extends Algorithm {

    protected MyAlgorithm(String name, String description) {
      super(name, description);
    }

    @Override
    public void verify(DecodedJWT decodedJWT) throws SignatureVerificationException {

    }

    @Override
    public byte[] sign(byte[] bytes) throws SignatureGenerationException {
      return new byte[0];
    }
  }
}
