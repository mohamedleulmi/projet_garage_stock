package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProduitMapper {

    ProduitDTO toDto(Produit produit);

    Produit toEntity(ProduitDTO produitDTO);

}
