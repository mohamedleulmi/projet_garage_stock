package com.garage.garage_back.facture.ligneFacturePrestation;

import com.garage.garage_back.model.LigneFacturePrestation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneFacturePrestationRepository extends JpaRepository<LigneFacturePrestation, Long> {
}
