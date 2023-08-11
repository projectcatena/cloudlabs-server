package com.cloudlabs.server.user;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  // Will issue select query first, then delete, so need Transactional
  // annotation
  @Transactional
  void deleteByEmail(String email);
}
