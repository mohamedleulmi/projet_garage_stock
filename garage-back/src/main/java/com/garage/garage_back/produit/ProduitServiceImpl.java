package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduitServiceImpl implements ProduitService {

    @Autowired
    private ProduitRepository produitRepository;

    @Override
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @Override
    public List<Produit> getProduitsSousSeuil() {
        return produitRepository.findByStockActuelLessThanEqual(5); // seuil d'exemple
    }

    @Override
    public Produit saveProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    @Override
    public Produit updateProduit(Long id, Produit updatedProduit) {
        Produit existingProduit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        existingProduit.setReference(updatedProduit.getReference());
        existingProduit.setDesignation(updatedProduit.getDesignation());
        existingProduit.setPrixUnitaireHT(updatedProduit.getPrixUnitaireHT());
        existingProduit.setTva(updatedProduit.getTva());
        existingProduit.setStockActuel(updatedProduit.getStockActuel());
        existingProduit.setSeuilAlerte(updatedProduit.getSeuilAlerte());

        return produitRepository.save(existingProduit);
    }

    @Override
    public void deleteProduit(Long id) {
        produitRepository.deleteById(id);
    }
}
