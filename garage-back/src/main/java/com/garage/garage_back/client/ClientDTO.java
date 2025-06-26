package com.garage.garage_back.client;

import lombok.Data;

@Data
public class ClientDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String vehicule;
}
