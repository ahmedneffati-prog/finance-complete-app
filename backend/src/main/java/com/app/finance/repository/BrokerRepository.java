package com.app.finance.repository;

import com.app.finance.entity.Broker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long> {
    List<Broker> findByIsActiveTrue();
    Optional<Broker> findByName(String name);
    boolean existsByName(String name);
}
