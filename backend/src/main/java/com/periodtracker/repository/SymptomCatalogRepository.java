package com.periodtracker.repository;

import com.periodtracker.entity.SymptomCatalog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymptomCatalogRepository extends JpaRepository<SymptomCatalog, Integer> {
    Optional<SymptomCatalog> findByCode(String code);
}
