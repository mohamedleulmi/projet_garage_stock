package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Produit findByReference(String reference);
    List<Produit> findByStockActuelLessThanEqual(int seuil);
}
