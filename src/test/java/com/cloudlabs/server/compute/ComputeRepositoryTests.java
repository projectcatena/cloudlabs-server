package com.cloudlabs.server.compute;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ComputeRepositoryTests {

    @Autowired
    private ComputeRepository computeRepository;

    @BeforeEach
    void setup() {
        Compute compute = new Compute("test", "e2-micro","10.10.1.1",60,"windows-server-2019");
        computeRepository.save(compute);
    }

    // @Test
    // void createCompute_whenInstanceDetailsGiven() {
    //     Compute compute = new Compute("test", "e2-micro","10.10.1.1" );
    //     computeRepository.save(compute);
    // }
        
    @Test
    void findCompute_whenInstanceNameGiven() {
        Compute compute = computeRepository.findByInstanceName("test");
        assertNotNull(compute);
    }

    @Test
    void deleteComputeInstance_whenInstanceNameGiven() {
        // Compute compute = computeRepository.findByInstanceName("windows-server-2019");
        computeRepository.deleteByInstanceName("test");
        // computeRepository.delete(compute);
    }
    
}
