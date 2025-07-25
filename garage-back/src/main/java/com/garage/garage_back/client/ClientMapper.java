package com.garage.garage_back.client;

import com.garage.garage_back.model.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientDTO toClientDTO(Client client);
    Client toClient(ClientDTO clientDTO);
}
