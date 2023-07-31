package com.cloudlabs.server.subnet;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubnetRepository extends JpaRepository<Subnet, Long> {

    @Transactional
    void deleteBySubnetName(String subnetName);

    Subnet findBySubnetName(String subnetName);
}
