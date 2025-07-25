package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;

    ProduitServiceImpl(ProduitRepository produitRepository, ProduitMapper produitMapper) {
        this.produitMapper = produitMapper;
        this.produitRepository = produitRepository;
    }

    @Override
    public List<ProduitDTO> getAllProduits() {
        return produitRepository.findAll().stream().map(produitMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProduitDTO> getProduitsSousSeuil() {
        return produitRepository.findByStockActuelLessThanEqual(5).stream().map(produitMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ProduitDTO saveProduit(ProduitDTO produitDTO) {
        Produit produit = produitMapper.toEntity(produitDTO);
        produitRepository.save(produit);
        return produitMapper.toDto(produit);
    }

    @Override
    public ProduitDTO updateProduit(Long id, ProduitDTO updatedProduit) {
        Produit existingProduit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        existingProduit.setReference(updatedProduit.getReference());
        existingProduit.setDesignation(updatedProduit.getDesignation());
        existingProduit.setPrixUnitaireHT(updatedProduit.getPrixUnitaireHT());
        existingProduit.setTva(updatedProduit.getTva());
        existingProduit.setStockActuel(updatedProduit.getStockActuel());
        existingProduit.setSeuilAlerte(updatedProduit.getSeuilAlerte());

        Produit produitSaved = produitRepository.save(existingProduit);
        return produitMapper.toDto(produitSaved);
    }

    @Override
    public void deleteProduit(Long id) {
        produitRepository.deleteById(id);
    }
}
