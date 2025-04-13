package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;

@Component
public class ClienteMapper { //O component me ajuda a injetar a classe no controlador.
	//Responsável por fazer o mapeamento.
	
	@Autowired
	private ModelMapper mapper;
	
	//Pegar o objeto que está vindo do DTO e transformar em Entidade
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente toEntity(ClienteCreateDTO dto) { //Recebe o objeto que vem do cliente e retorna uma Entidade.	
		Cliente entity = mapper.map(dto, Cliente.class);
		return entity;
	}	
	
	//Pegar os dados de uma entidade e transformar no ResponseDTO
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ClienteResponseDTO toDTO(Cliente entity) {
		ClienteResponseDTO dto = mapper.map(entity, ClienteResponseDTO.class);
		return dto;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<ClienteResponseDTO> toDTO(List<Cliente> clientes) {
		return clientes.stream()
				.map(cliente -> toDTO(cliente)) //Pega objeto por objeto que está dentro da lista que pego como parametro, chamo o toDTO e passando a entidade, add em dto e vou adicionando dentro da lista
				.collect(Collectors.toList());
	}
	
	
}
