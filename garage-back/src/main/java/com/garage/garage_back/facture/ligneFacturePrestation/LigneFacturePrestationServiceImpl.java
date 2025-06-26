package com.garage.garage_back.facture.ligneFacturePrestation;

import com.garage.garage_back.model.LigneFacturePrestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LigneFacturePrestationServiceImpl implements LigneFacturePrestationService {

    @Autowired
    private LigneFacturePrestationRepository repository;

    @Override
    public LigneFacturePrestation save(LigneFacturePrestation ligne) {
        return repository.save(ligne);
    }

    @Override
    public List<LigneFacturePrestation> findAll() {
        return repository.findAll();
    }
}