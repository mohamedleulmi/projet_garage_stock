package com.garage.garage_back.client;

import com.garage.garage_back.model.Client;

import java.util.List;

public interface ClientService {
    Client saveClient(Client client);
    Client getClientById(Long id);
    List<Client> getAllClients();
    void deleteClient(Long id);
}