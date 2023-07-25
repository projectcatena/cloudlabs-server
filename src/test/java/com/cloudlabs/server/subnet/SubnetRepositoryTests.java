package com.cloudlabs.server.subnet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SubnetRepositoryTests {
    
    @Autowired
    private SubnetRepository subnetRepository;

    @BeforeEach
    void setup() {
        Subnet subnet = new Subnet("test-subnet", "10.10.2.0/24");
        subnetRepository.save(subnet);
    }

    @Test
    void deleteBySubnetName() {
        subnetRepository.deleteBySubnetName("test-subnet");
    }
}
