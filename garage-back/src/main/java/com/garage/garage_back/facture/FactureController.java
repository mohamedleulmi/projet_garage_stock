package com.garage.garage_back.facture;

import com.garage.garage_back.model.Facture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin
public class FactureController {

    @Autowired
    private FactureService factureService;

    @PostMapping
    public FactureDTO createFacture(@RequestBody FactureDTO factureDTO) throws Exception {
        return factureService.createFacture(factureDTO);
    }

    @GetMapping
    public List<FactureDTO> getAllFactures() {
        return factureService.getAllFactures();
    }

    @GetMapping("/{id}")
    public FactureDTO getFactureById(@PathVariable Long id) {
        return factureService.getFactureById(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getFacturePdf(@PathVariable Long id) throws IOException {
        FactureDTO factureDTO = factureService.getFactureById(id); // charge depuis DB
        String fileName = "facture_" + factureDTO.getNumero() + ".pdf";
        Path path = Paths.get("factures", fileName);

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = Files.readAllBytes(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(fileName)
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
