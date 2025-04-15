package br.com.marcielli.BancoM.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.CartaoDeleteDTO;
import br.com.marcielli.BancoM.dto.CartaoMapper;
import br.com.marcielli.BancoM.dto.CartaoResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateResponseDTO;
import br.com.marcielli.BancoM.dto.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.ContaResponseDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.service.CartaoService;

@RestController
@RequestMapping("/cartao")
public class CartaoController {
	
	@Autowired
	private CartaoService cartaoService;	
	
	@Autowired
	private CartaoMapper cartaoMapper;
	
	@Autowired
	private CartaoUpdateMapper cartaoUpdateMapper;
		
	@PostMapping("/salvar")
	public ResponseEntity<CartaoResponseDTO> adicionarCartao(@RequestBody CartaoCreateDTO cartaoCreateDTO) {		

		//Cartao cartao = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao cartaoGravado = cartaoService.save(cartaoCreateDTO);

		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartaoGravado);

		return ResponseEntity.status(HttpStatus.CREATED).body(cartaoResponseDTO);
	}	
	
	@PutMapping("/atualizar/{cartaoId}")
	public ResponseEntity<CartaoUpdateResponseDTO> atualizar(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoUpdateDTO cartaoUpdateDTO) {

		//Conta conta = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao cartaoAtualizado = cartaoService.update(cartaoId, cartaoUpdateDTO);

		CartaoUpdateResponseDTO cartaoResponseDTO = cartaoUpdateMapper.toDTO(cartaoAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}
	
	
	@GetMapping("/listar")
	public ResponseEntity<List<Cartao>> getContas() {
		List<Cartao> cartoes = cartaoService.getAll();
		return new ResponseEntity<List<Cartao>>(cartoes, HttpStatus.OK);
	}

	@GetMapping("/listar/{cartaoId}")
	public Optional<Cartao> getCartaoById(@PathVariable("cartaoId") Long cartaoId) {

		Optional<Cartao> cartaoById = cartaoService.getCartaoById(cartaoId);

		if (!cartaoById.isPresent()) {
			throw new CartaoNaoEncontradoException("Cartão não existe no banco.");
		}

		return cartaoById;
	}

	@DeleteMapping("/deletar/{cartaoId}")
	public ResponseEntity<String> deletar(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoDeleteDTO cartaoDeleteDTO) {
		
		boolean cartaoDeletado = cartaoService.deleteCartao(cartaoId, cartaoDeleteDTO);
		
		if (cartaoDeletado) {
			return new ResponseEntity<String>("Cartão deletado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
		}
	}
		
//
//	// Pagamento Cartão
//	@PostMapping("/pagamento/{idClienteReceber}/{idContaReceber}")
//	public ResponseEntity<String> pagamentoCartao(@PathVariable("idClienteReceber") Long idClienteReceber, @PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia contaEnviar) {
//		
//		boolean pagamento = cartaoService.pagarCartao(idClienteReceber,idContaReceber, contaEnviar);
//
//		if (pagamento) {
//
//			return new ResponseEntity<String>("Pagamento realizado com sucesso.", HttpStatus.CREATED);
//
//		} else {
//
//			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//	
//	
//	
//	
	
	
	
//	@PostMapping("/pagamento/{idClienteReceber}/{idContaReceber}")
//	public ResponseEntity<String> transferirPagarCartao(@PathVariable("idClienteReceber") Long idClienteReceber, @PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia valorDepositar) {
//				
//		boolean pagamento = cartaoService.pagarCartao(idClienteReceber, idContaReceber, valorDepositar);
//
//		if (pagamento) {
//
//			return new ResponseEntity<String>("Pagamento realizado com sucesso.", HttpStatus.OK);
//
//		} else {
//
//			return new ResponseEntity<String>("Dados inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
	
	
	
	
	
	
	
	
	
	
	
	

}
