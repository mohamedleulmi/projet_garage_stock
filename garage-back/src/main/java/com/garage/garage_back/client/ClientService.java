package com.garage.garage_back.client;

import com.garage.garage_back.model.Client;

import java.util.List;

public interface ClientService {
    ClientDTO saveClient(ClientDTO client);
    ClientDTO getClientById(Long id);
    List<ClientDTO> getAllClients();
    void deleteClient(Long id);
}