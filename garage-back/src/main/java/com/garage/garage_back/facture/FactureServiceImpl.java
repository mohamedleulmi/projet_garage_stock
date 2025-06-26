package com.garage.garage_back.facture;

import com.garage.garage_back.client.ClientRepository;
import com.garage.garage_back.facture.ligneFacturePrestation.LigneFacturePrestationRepository;
import com.garage.garage_back.facture.ligneFacturePrestation.LignePrestationDTO;
import com.garage.garage_back.facture.ligneFactureProduit.LigneFactureProduitRepository;
import com.garage.garage_back.facture.ligneFactureProduit.LigneProduitDTO;
import com.garage.garage_back.model.*;
import com.garage.garage_back.produit.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FactureServiceImpl implements FactureService {

    @Autowired
    private FactureRepository factureRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private LigneFactureProduitRepository ligneProduitRepository;
    @Autowired private LigneFacturePrestationRepository lignePrestationRepository;

    @Override
    public Facture createFacture(Facture dto) {
        Client client = clientRepository.findById(dto.getClient().getId()).orElseThrow();

        Facture facture = new Facture();
        facture.setNumero(generateNumero());
        facture.setDateCreation(LocalDate.now());
        facture.setClient(client);

        double totalHT = 0;
        double totalTVA = 0;

        List<LigneFactureProduit> lignesProduit = new ArrayList<>();
        for (LigneFactureProduit ligne : dto.getLignesProduit()) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId()).orElseThrow();

            LigneFactureProduit lfp = new LigneFactureProduit();
            lfp.setFacture(facture);
            lfp.setProduit(produit);
            lfp.setQuantite(ligne.getQuantite());
            lfp.setPrixUnitaireHT(produit.getPrixUnitaireHT());
            lfp.setTva(produit.getTva());

            double ht = produit.getPrixUnitaireHT() * ligne.getQuantite();
            double ttc = ht * (1 + produit.getTva() / 100);

            lfp.setTotalHT(ht);
            lfp.setTotalTTC(ttc);

            produit.setStockActuel(produit.getStockActuel() - ligne.getQuantite());
            produit.setStockVendu(produit.getStockVendu() + ligne.getQuantite());

            produitRepository.save(produit);

            totalHT += ht;
            totalTVA += ttc - ht;

            lignesProduit.add(lfp);
        }

        List<LigneFacturePrestation> lignesPrestation = new ArrayList<>();
        for (LigneFacturePrestation ligne : dto.getLignesPrestation()) {
            LigneFacturePrestation lfp = new LigneFacturePrestation();
            lfp.setFacture(facture);
            lfp.setDescription(ligne.getDescription());
            lfp.setPrixHT(ligne.getPrixHT());
            lfp.setTva(ligne.getTva());
            lfp.setTotalTTC(ligne.getPrixHT() * (1 + ligne.getTva() / 100));

            totalHT += ligne.getPrixHT();
            totalTVA += ligne.getPrixHT() * ligne.getTva() / 100;

            lignesPrestation.add(lfp);
        }

        facture.setTotalHT(totalHT);
        facture.setTotalTVA(totalTVA);
        facture.setTotalTTC(totalHT + totalTVA);

        facture = factureRepository.save(facture);
        ligneProduitRepository.saveAll(lignesProduit);
        lignePrestationRepository.saveAll(lignesPrestation);

        return facture;
    }

    private String generateNumero() {
        String base = "FAC-" + LocalDate.now().toString().replace("-", "");
        int count = (int) factureRepository.count() + 1;
        return base + String.format("-%03d", count);
    }

    @Override
    public List<Facture> getAllFactures() {
        return factureRepository.findAll();
    }

    @Override
    public Facture getFactureById(Long id) {
        return factureRepository.findById(id).orElse(null);
    }
}
