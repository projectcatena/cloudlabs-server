package com.cloudlabs.server.status;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
public class StatusController {

  @GetMapping("/health")
  public ResponseEntity<String> getHealthStatus() {

    return ResponseEntity.ok().build();
  }
}
