package com.cloudlabs.server.security.jwt;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
  KeyStore loadKeyStore() throws KeyStoreException, NoSuchAlgorithmException,
      CertificateException, IOException;

  RSAPrivateKey loadSigningKey(KeyStore keyStore)
      throws UnrecoverableKeyException, KeyStoreException,
      NoSuchAlgorithmException;

  RSAPublicKey loadValidationKey(KeyStore keyStore) throws KeyStoreException;

  String getUsername(String jwt);

  Claims getAllClaims(String jwt);

  <T> T getClaim(String jwt, Function<Claims, T> claimsResolver);

  Date getExpiration(String jwt);

  String generateToken(Map<String, Object> extraClaims,
      UserDetails userDetails);

  Boolean validateToken(String jwt, UserDetails userDetails);
}
