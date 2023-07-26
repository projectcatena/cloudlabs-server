package com.cloudlabs.server.subnet;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface SubnetRepository extends JpaRepository<Subnet, Long> {

    @Transactional
    void deleteBySubnetName(String subnetName);

    Subnet findBySubnetName(String subnetName);
    
}
