package com.garage.garage_back.facture;

import com.garage.garage_back.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactureRepository extends JpaRepository<Facture, Long> {
    boolean existsByNumero(String numero);
}
