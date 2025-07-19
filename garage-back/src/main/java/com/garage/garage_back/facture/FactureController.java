package com.garage.garage_back.facture;

import com.garage.garage_back.model.Facture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin
public class FactureController {

    @Autowired
    private FactureService factureService;

    @PostMapping
    public Facture createFacture(@RequestBody Facture dto) throws Exception {
        return factureService.createFacture(dto);
    }

    @GetMapping
    public List<Facture> getAllFactures() {
        return factureService.getAllFactures();
    }

    @GetMapping("/{id}")
    public Facture getFactureById(@PathVariable Long id) {
        return factureService.getFactureById(id);
    }
}
