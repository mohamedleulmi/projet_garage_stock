package com.garage.garage_back.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String vehiculeImatriculation;
}
