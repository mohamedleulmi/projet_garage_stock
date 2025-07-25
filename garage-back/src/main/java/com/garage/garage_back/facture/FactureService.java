package com.garage.garage_back.facture;

import com.garage.garage_back.model.Facture;

import java.util.List;

public interface FactureService {
    FactureDTO createFacture(FactureDTO factureDTO) throws Exception;
    List<FactureDTO> getAllFactures();
    FactureDTO getFactureById(Long id);
}
