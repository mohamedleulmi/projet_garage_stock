package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin
public class ProduitController {


    private final ProduitService produitService;

    ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public List<ProduitDTO> getAllProduits() {
        return produitService.getAllProduits();
    }

    @PostMapping
    public ProduitDTO createProduit(@RequestBody ProduitDTO produitDTO) {
        return produitService.saveProduit(produitDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
    }

    @PutMapping("/{id}")
    public ProduitDTO updateProduit(@PathVariable Long id, @RequestBody ProduitDTO updatedProduitDTO) {
        return produitService.updateProduit(id, updatedProduitDTO);
    }
}
