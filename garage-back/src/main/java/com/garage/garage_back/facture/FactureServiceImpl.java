package com.garage.garage_back.facture;

import com.garage.garage_back.client.ClientRepository;
import com.garage.garage_back.facture.ligneFacturePrestation.LigneFacturePrestationRepository;
import com.garage.garage_back.facture.ligneFactureProduit.LigneFactureProduitRepository;
import com.garage.garage_back.model.*;
import com.garage.garage_back.produit.ProduitRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FactureServiceImpl implements FactureService {

    @Autowired
    private FactureRepository factureRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private LigneFactureProduitRepository ligneProduitRepository;
    @Autowired private LigneFacturePrestationRepository lignePrestationRepository;

    @Override
    @Transactional
    public Facture createFacture(Facture dto) throws Exception {
        // Récupération du client
        Client client = clientRepository.findById(dto.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // Création de la facture sans ID (elle sera persistée ensuite)
        Facture facture = new Facture();
        facture.setNumero(generateNumero());
        facture.setDateCreation(LocalDate.now());
        facture.setClient(client);

        double totalHT = 0;
        double totalTVA = 0;

        // On sauve la facture vide d'abord pour avoir un ID (utile pour setFacture ensuite)
        facture = factureRepository.save(facture);

        List<LigneFactureProduit> lignesProduit = new ArrayList<>();
        List<Produit> produitsToUpdate = new ArrayList<>();

        for (LigneFactureProduit ligne : dto.getLignesProduit()) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            int quantite = ligne.getQuantite();
            if (produit.getStockActuel() < quantite) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + produit.getDesignation());
            }

            double prixUnitaireHT = produit.getPrixUnitaireHT();
            double tva = produit.getTva();
            double ht = prixUnitaireHT * quantite;
            double ttc = ht * (1 + tva / 100);

            LigneFactureProduit lfp = new LigneFactureProduit();
            lfp.setProduit(produit);
            lfp.setQuantite(quantite);
            lfp.setPrixUnitaireHT(prixUnitaireHT);
            lfp.setTva(tva);
            lfp.setTotalHT(ht);
            lfp.setTotalTTC(ttc);

            lignesProduit.add(lfp);

            // Mise à jour du stock
            produit.setStockActuel(produit.getStockActuel() - quantite);
            produit.setStockVendu(produit.getStockVendu() + quantite);
            produitsToUpdate.add(produit);

            totalHT += ht;
            totalTVA += ttc - ht;
        }

        List<LigneFacturePrestation> lignesPrestation = new ArrayList<>();

        for (LigneFacturePrestation ligne : dto.getLignesPrestation()) {
            double prixHT = ligne.getPrixHT();
            double tva = ligne.getTva();
            double ttc = prixHT * (1 + tva / 100);

            LigneFacturePrestation lfp = new LigneFacturePrestation();
            lfp.setDescription(ligne.getDescription());
            lfp.setPrixHT(prixHT);
            lfp.setTva(tva);
            lfp.setTotalTTC(ttc);

            lignesPrestation.add(lfp);

            totalHT += prixHT;
            totalTVA += prixHT * tva / 100;
        }

        facture.setTotalHT(totalHT);
        facture.setTotalTVA(totalTVA);
        facture.setTotalTTC(totalHT + totalTVA);
        facture.setLignesProduit(lignesProduit);
        facture.setLignesPrestation(lignesPrestation);
        // Enregistrement final
        factureRepository.save(facture);
        produitRepository.saveAll(produitsToUpdate);



        this.generatePdf(facture);
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

    private void generatePdf(Facture facture) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50); // marges plus grandes
        String fileName = "facture_" + facture.getNumero() + ".pdf";
        Path path = Paths.get("factures", fileName);
        Files.createDirectories(path.getParent());

        PdfWriter.getInstance(document, new FileOutputStream(path.toFile()));
        document.open();

        // POLICES
        Font fontTitle = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLUE);
        Font fontSubTitle = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
        Font fontBold = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 12);
        Font fontTableHeader = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);

        // LOGO + infos garage en ligne
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3});

        // Logo
        InputStream logoStream = getClass().getResourceAsStream("/static/logo.png");
        if (logoStream != null) {
            Image logo = Image.getInstance(IOUtils.toByteArray(logoStream));
            logo.scaleToFit(100, 50);
            PdfPCell logoCell = new PdfPCell(logo, false);
            logoCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(logoCell);
        } else {
            headerTable.addCell(""); // cellule vide si pas de logo
        }

        // Infos garage
        PdfPCell garageInfo = new PdfPCell();
        garageInfo.setBorder(Rectangle.NO_BORDER);
        garageInfo.addElement(new Paragraph("Garage AutoExpress", fontTitle));
        garageInfo.addElement(new Paragraph("12 Rue du Mécano, 75000 Paris", fontNormal));
        garageInfo.addElement(new Paragraph("Tél : 01 23 45 67 89", fontNormal));
        headerTable.addCell(garageInfo);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // Infos client
        Paragraph clientTitle = new Paragraph("Facturé à :", fontSubTitle);
        clientTitle.setSpacingAfter(10);
        document.add(clientTitle);

        Client c = facture.getClient();
        Paragraph clientInfo = new Paragraph();
        clientInfo.setFont(fontNormal);
        clientInfo.add(c.getNom() + "\n");
        clientInfo.add("Téléphone : " + c.getTelephone() + "\n");
        clientInfo.add("Véhicule : " + c.getVehiculeImatriculation() + "\n");
        document.add(clientInfo);
        document.add(Chunk.NEWLINE);

        // Tableau des produits et prestations
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 2, 1.5f, 1.5f, 2.5f});

        // En-tête coloré
        Stream.of("Désignation", "PU HT", "Quantité", "TVA", "Total TTC").forEach(title -> {
            PdfPCell header = new PdfPCell(new Phrase(title, fontTableHeader));
            header.setBackgroundColor(Color.BLUE);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            table.addCell(header);
        });

        // Contenu lignes produits
        for (LigneFactureProduit ligne : facture.getLignesProduit()) {
            table.addCell(new PdfPCell(new Phrase(ligne.getProduit().getDesignation(), fontNormal)));

            PdfPCell puCell = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getPrixUnitaireHT()), fontNormal));
            puCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(puCell);

            PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(ligne.getQuantite()), fontNormal));
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(qtyCell);

            PdfPCell tvaCell = new PdfPCell(new Phrase(String.format("%.0f%%", ligne.getTva()), fontNormal));
            tvaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(tvaCell);

            PdfPCell totalCell = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getTotalTTC()), fontNormal));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
        }

        // Contenu lignes prestations
        for (LigneFacturePrestation ligne : facture.getLignesPrestation()) {
            table.addCell(new PdfPCell(new Phrase(ligne.getDescription(), fontNormal)));

            PdfPCell puCell = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getPrixHT()), fontNormal));
            puCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(puCell);

            PdfPCell qtyCell = new PdfPCell(new Phrase("1", fontNormal));
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(qtyCell);

            PdfPCell tvaCell = new PdfPCell(new Phrase(String.format("%.0f%%", ligne.getTva()), fontNormal));
            tvaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(tvaCell);

            PdfPCell totalCell = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getTotalTTC()), fontNormal));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
        }

        document.add(table);

        document.add(Chunk.NEWLINE);

        // Encadré totaux
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(30);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10f);

        PdfPCell labelHT = new PdfPCell(new Phrase("Total HT :", fontBold));
        labelHT.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueHT = new PdfPCell(new Phrase(String.format("%.2f €", facture.getTotalHT()), fontNormal));
        valueHT.setBorder(Rectangle.NO_BORDER);
        valueHT.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell labelTVA = new PdfPCell(new Phrase("TVA :", fontBold));
        labelTVA.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueTVA = new PdfPCell(new Phrase(String.format("%.2f €", facture.getTotalTVA()), fontNormal));
        valueTVA.setBorder(Rectangle.NO_BORDER);
        valueTVA.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell labelTTC = new PdfPCell(new Phrase("Total TTC :", fontBold));
        labelTTC.setBorder(Rectangle.TOP);
        labelTTC.setPaddingTop(5);
        PdfPCell valueTTC = new PdfPCell(new Phrase(String.format("%.2f €", facture.getTotalTTC()), fontBold));
        valueTTC.setBorder(Rectangle.TOP);
        valueTTC.setPaddingTop(5);
        valueTTC.setHorizontalAlignment(Element.ALIGN_RIGHT);

        totalsTable.addCell(labelHT);
        totalsTable.addCell(valueHT);
        totalsTable.addCell(labelTVA);
        totalsTable.addCell(valueTVA);
        totalsTable.addCell(labelTTC);
        totalsTable.addCell(valueTTC);

        document.add(totalsTable);

        document.close();
    }


}
