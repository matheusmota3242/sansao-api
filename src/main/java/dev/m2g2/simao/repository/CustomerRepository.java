package dev.m2g2.simao.repository;

import dev.m2g2.simao.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    List<Customer> findAllByActiveTrueOrderByNameAsc();
}
