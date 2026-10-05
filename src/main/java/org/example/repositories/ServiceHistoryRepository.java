package org.example.repositories;

import org.example.models.ServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, Long> {

    List<ServiceHistory> findByCustomerId(Long customerId);
}