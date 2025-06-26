package com.garage.garage_back.facture.ligneFactureProduit;

import com.garage.garage_back.model.LigneFactureProduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LigneFactureProduitServiceImpl implements LigneFactureProduitService {

    @Autowired
    private LigneFactureProduitRepository repository;

    @Override
    public LigneFactureProduit save(LigneFactureProduit ligne) {
        return repository.save(ligne);
    }

    @Override
    public List<LigneFactureProduit> findAll() {
        return repository.findAll();
    }
}
