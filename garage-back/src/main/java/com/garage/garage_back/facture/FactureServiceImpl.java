package com.garage.garage_back.facture;

import com.garage.garage_back.client.ClientMapper;
import com.garage.garage_back.client.ClientRepository;
import com.garage.garage_back.facture.ligneFacturePrestation.LigneFacturePrestationRepository;
import com.garage.garage_back.facture.ligneFacturePrestation.LignePrestationDTO;
import com.garage.garage_back.facture.ligneFactureProduit.LigneFactureProduitRepository;
import com.garage.garage_back.facture.ligneFactureProduit.LigneProduitDTO;
import com.garage.garage_back.model.*;
import com.garage.garage_back.produit.ProduitRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FactureServiceImpl implements FactureService {


    private final FactureRepository factureRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final FactureMapper factureMapper;
    private final ClientMapper clientMapper;

    FactureServiceImpl(FactureRepository factureRepository, ProduitRepository produitRepository, ClientRepository clientRepository, FactureMapper factureMapper, ClientMapper clientMapper) {
        this.factureRepository = factureRepository;
        this.produitRepository = produitRepository;
        this.clientRepository = clientRepository;
        this.factureMapper = factureMapper;
        this.clientMapper = clientMapper;
    }

    @Override
    @Transactional
    public FactureDTO createFacture(FactureDTO factureDTO) throws Exception {
        // Récupération du client
        Client client = clientRepository.findById(factureDTO.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // Création de la facture sans ID (elle sera persistée ensuite)
        Facture facture = new Facture();
        facture.setNumero(generateNumero());
        facture.setDateCreation(LocalDate.now());
        facture.setClient(client);

        double totalHT = 0;
        double totalTVA = 0;

        List<LigneFactureProduit> lignesProduit = new ArrayList<>();
        List<Produit> produitsToUpdate = new ArrayList<>();

        for (LigneProduitDTO ligneProduitDTO : factureDTO.getLignesProduit()) {
            Produit produit = produitRepository.findById(ligneProduitDTO.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            int quantite = ligneProduitDTO.getQuantite();

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

        for (LignePrestationDTO lignePrestationDTO : factureDTO.getLignesPrestation()) {
            double prixHT = lignePrestationDTO.getPrixHT();
            double tva = lignePrestationDTO.getTva();
            double ttc = prixHT * (1 + tva / 100);

            LigneFacturePrestation lfp = new LigneFacturePrestation();
            lfp.setDescription(lignePrestationDTO.getDescription());
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
        return factureMapper.toDto(facture);
    }

    private String generateNumero() {
        String base = "FAC-" + LocalDate.now().toString().replace("-", "");
        int count = (int) factureRepository.count() + 1;
        return base + String.format("-%03d", count);
    }

    @Override
    public List<FactureDTO> getAllFactures() {
        return factureRepository.findAll().stream().map(factureMapper::toDto).toList();
    }

    @Override
    public FactureDTO getFactureById(Long id) {
        Facture facture =  factureRepository.findById(id).orElseThrow(() -> new RuntimeException("Facture non trouvé"));
        return factureMapper.toDto(facture);
    }

    private void generatePdf(Facture facture) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        String fileName = "facture_" + facture.getNumero() + ".pdf";
        Path path = Paths.get("factures", fileName);
        Files.createDirectories(path.getParent());

        PdfWriter.getInstance(document, new FileOutputStream(path.toFile()));
        document.open();

        // === Polices ===
        Font fontBold = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 12);
        Font fontSectionTitle = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(44, 62, 80)); // Bleu nuit
        Font fontTableHeader = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);

        // === Bannière ===
        InputStream bannerStream = getClass().getResourceAsStream("/static/banner.png");
        if (bannerStream != null) {
            Image banner = Image.getInstance(IOUtils.toByteArray(bannerStream));
            banner.scaleToFit(PageSize.A4.getWidth() - 100, 150);
            banner.setAlignment(Image.ALIGN_CENTER);
            document.add(banner);
            document.add(Chunk.NEWLINE);
        }

        // === Séparateur
        LineSeparator separator = new LineSeparator();
        separator.setLineWidth(1.7f);
        separator.setLineColor(Color.LIGHT_GRAY);
        document.add(new Chunk(separator));
        document.add(Chunk.NEWLINE);

        // === Infos Garage / Client
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell garageCell = new PdfPCell();
        garageCell.setBorder(Rectangle.NO_BORDER);
        garageCell.setPadding(5);
        garageCell.addElement(new Paragraph("Garage AutoExpress", fontBold));
        garageCell.addElement(new Paragraph("12 Rue du Mécano", fontNormal));
        garageCell.addElement(new Paragraph("75000 Paris", fontNormal));
        garageCell.addElement(new Paragraph("Tél : 01 23 45 67 89", fontNormal));
        infoTable.addCell(garageCell);

        Client c = facture.getClient();
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.NO_BORDER);
        clientCell.setPadding(5);
        clientCell.addElement(new Paragraph("Facturé à :", fontBold));
        clientCell.addElement(new Paragraph(c.getNom(), fontNormal));
        clientCell.addElement(new Paragraph("Tél : " + c.getTelephone(), fontNormal));
        clientCell.addElement(new Paragraph("Véhicule : " + c.getVehiculeImatriculation(), fontNormal));
        infoTable.addCell(clientCell);

        document.add(infoTable);
        document.add(Chunk.NEWLINE);
        document.add(new Chunk(separator));
        document.add(Chunk.NEWLINE);


        // === Section PRODUITS ===
        Paragraph titreProduitVendu = new Paragraph("Produits vendus", fontSectionTitle);
        titreProduitVendu.setAlignment(Element.ALIGN_CENTER);
        titreProduitVendu.setSpacingBefore(10);
        titreProduitVendu.setSpacingAfter(10);
        document.add(titreProduitVendu);

        PdfPTable tableProduits = new PdfPTable(5);
        tableProduits.setWidthPercentage(80);
        tableProduits.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableProduits.setWidths(new float[]{4, 2, 1.5f, 1.5f, 2.5f});

        Stream.of("Désignation", "PU HT", "Quantité", "TVA", "Total TTC").forEach(title -> {
            PdfPCell header = new PdfPCell(new Phrase(title, fontTableHeader));
            header.setBackgroundColor(new Color(44, 62, 80)); // Bleu nuit
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            tableProduits.addCell(header);
        });

        boolean alternate = false;
        for (LigneFactureProduit ligne : facture.getLignesProduit()) {
            Color rowColor = alternate ? new Color(236, 240, 241) : Color.WHITE; // Gris clair
            alternate = !alternate;

            PdfPCell cell1 = new PdfPCell(new Phrase(ligne.getProduit().getDesignation(), fontNormal));
            cell1.setBackgroundColor(rowColor);
            tableProduits.addCell(cell1);

            PdfPCell pu = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getPrixUnitaireHT()), fontNormal));
            pu.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pu.setBackgroundColor(rowColor);
            tableProduits.addCell(pu);

            PdfPCell qty = new PdfPCell(new Phrase(String.valueOf(ligne.getQuantite()), fontNormal));
            qty.setHorizontalAlignment(Element.ALIGN_CENTER);
            qty.setBackgroundColor(rowColor);
            tableProduits.addCell(qty);

            PdfPCell tva = new PdfPCell(new Phrase(String.format("%.0f%%", ligne.getTva()), fontNormal));
            tva.setHorizontalAlignment(Element.ALIGN_CENTER);
            tva.setBackgroundColor(rowColor);
            tableProduits.addCell(tva);

            PdfPCell total = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getTotalTTC()), fontNormal));
            total.setHorizontalAlignment(Element.ALIGN_RIGHT);
            total.setBackgroundColor(rowColor);
            tableProduits.addCell(total);
        }

        document.add(tableProduits);

        // === Section PRESTATIONS ===
        Paragraph titrePrestation = new Paragraph("Prestations effectuées", fontSectionTitle);
        titrePrestation.setAlignment(Element.ALIGN_CENTER);
        titrePrestation.setSpacingBefore(10);
        titrePrestation.setSpacingAfter(10);
        document.add(titrePrestation);

        PdfPTable tablePrestations = new PdfPTable(5);
        tablePrestations.setWidthPercentage(80);
        tablePrestations.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablePrestations.setWidths(new float[]{4, 2, 1.5f, 1.5f, 2.5f});

        Stream.of("Désignation", "PU HT", "Quantité", "TVA", "Total TTC").forEach(title -> {
            PdfPCell header = new PdfPCell(new Phrase(title, fontTableHeader));
            header.setBackgroundColor(new Color(44, 62, 80));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            tablePrestations.addCell(header);
        });

        alternate = false;
        for (LigneFacturePrestation ligne : facture.getLignesPrestation()) {
            Color rowColor = alternate ? new Color(236, 240, 241) : Color.WHITE;
            alternate = !alternate;

            PdfPCell cell1 = new PdfPCell(new Phrase(ligne.getDescription(), fontNormal));
            cell1.setBackgroundColor(rowColor);
            tablePrestations.addCell(cell1);

            PdfPCell pu = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getPrixHT()), fontNormal));
            pu.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pu.setBackgroundColor(rowColor);
            tablePrestations.addCell(pu);

            PdfPCell qty = new PdfPCell(new Phrase("1", fontNormal));
            qty.setHorizontalAlignment(Element.ALIGN_CENTER);
            qty.setBackgroundColor(rowColor);
            tablePrestations.addCell(qty);

            PdfPCell tva = new PdfPCell(new Phrase(String.format("%.0f%%", ligne.getTva()), fontNormal));
            tva.setHorizontalAlignment(Element.ALIGN_CENTER);
            tva.setBackgroundColor(rowColor);
            tablePrestations.addCell(tva);

            PdfPCell total = new PdfPCell(new Phrase(String.format("%.2f €", ligne.getTotalTTC()), fontNormal));
            total.setHorizontalAlignment(Element.ALIGN_RIGHT);
            total.setBackgroundColor(rowColor);
            tablePrestations.addCell(total);
        }

        document.add(tablePrestations);
        document.add(Chunk.NEWLINE);

        // === Totaux
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
