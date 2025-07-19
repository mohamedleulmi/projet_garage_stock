package com.garage.garage_back.facture;

import com.garage.garage_back.model.Facture;

import java.util.List;

public interface FactureService {
    Facture createFacture(Facture facture) throws Exception;
    List<Facture> getAllFactures();
    Facture getFactureById(Long id);
}
