package com.cloudlabs.server.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

  @Value("${app.security.jwt.keystore-location}")
  private String keyStorePath;

  @Value("${app.security.jwt.keystore-password}")
  private String keyStorePassword;

  @Value("${app.security.jwt.key-alias}")
  private String keyAlias;

  @Value("${app.security.jwt.private-key-passphrase}")
  private String privateKeyPassphrase;

  @Override
  public KeyStore loadKeyStore() {
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
          keyStorePath);
      keyStore.load(resourceAsStream, keyStorePassword.toCharArray());
      return keyStore;
    } catch (KeyStoreException keyStoreException) {
      System.err.println("Loading of keystore has failed: ");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (CertificateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    throw new IllegalArgumentException(
        "Unable to load keystore, have you checked that it exists?");
  }

  @Override
  public RSAPrivateKey loadSigningKey(KeyStore keyStore) {
    Key key;

    try {
      key = keyStore.getKey(keyAlias, privateKeyPassphrase.toCharArray());

      if (key instanceof RSAPrivateKey) {
        return (RSAPrivateKey) key;
      }

    } catch (UnrecoverableKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (KeyStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    throw new IllegalArgumentException(
        "Unable to load signing key, is the passphrase correct?");
  }

  @Override
  public RSAPublicKey loadValidationKey(KeyStore keyStore) {
    Certificate certificate;

    try {
      certificate = keyStore.getCertificate(keyAlias);

      PublicKey publicKey = certificate.getPublicKey();
      if (publicKey instanceof RSAPublicKey) {
        return (RSAPublicKey) publicKey;
      }

    } catch (KeyStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    throw new IllegalArgumentException("Unable to load RSA public key");
  }

  @Override
  public String getUsername(String jwt) {
    return getClaim(jwt, Claims::getSubject);
  }

  @Override
  public <T> T getClaim(String jwt, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaims(jwt);

    return claimsResolver.apply(claims);
  }

  @Override
  public Claims getAllClaims(String jwt) {
    return Jwts.parserBuilder()
        .setSigningKey(loadSigningKey(loadKeyStore()))
        .build()
        .parseClaimsJws(jwt)
        .getBody();
  }

  /*
   * Allow the addition of additional claims such as roles, authorities, or any
   * information that wants to be stored within JWT.
   *
   * Expiration should be short, and refreshing of token should be frequent. In
   * this case, the token is valid for 5 minutes from issued
   * time (miliseconds).
   *
   * Signing key is extracted from key store and the JWT is signed using the
   * RS256 algorithm.
   *
   * Subject of the token is the username.
   *
   * If you want to simply generate a token with extra claims, just input an
   * empty HashMap
   */
  @Override
  public String generateToken(Map<String, Object> extraClaims,
      UserDetails userDetails) {

    Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant expiration = issuedAt.plus(5, ChronoUnit.MINUTES);

    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername()) // user email
        .setIssuedAt(Date.from(issuedAt))
        .setExpiration(Date.from(expiration))
        .signWith(loadSigningKey(loadKeyStore()), SignatureAlgorithm.RS256)
        .compact();

    /*
     * .setIssuedAt(new Date(System.currentTimeMillis()))
     * .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 2))
     */
  }

  @Override
  public Boolean validateToken(String jwt, UserDetails userDetails) {
    final String username = getUsername(jwt);

    // Check if token has not expired by checking if it is before today's date
    Boolean isJwtExpired = getExpiration(jwt).before(new Date());

    // If isJwtExpired is false, means not expired
    return (username.equals(userDetails.getUsername())) && !isJwtExpired;
  }

  @Override
  public Date getExpiration(String jwt) {
    return getClaim(jwt, Claims::getExpiration);
  }
}
