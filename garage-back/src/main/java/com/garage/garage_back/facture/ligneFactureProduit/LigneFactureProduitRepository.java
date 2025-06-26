package com.garage.garage_back.facture.ligneFactureProduit;

import com.garage.garage_back.model.LigneFactureProduit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneFactureProduitRepository extends JpaRepository<LigneFactureProduit, Long> {
}
