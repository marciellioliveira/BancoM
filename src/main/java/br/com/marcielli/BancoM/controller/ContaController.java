package br.com.marcielli.BancoM.controller;

import java.math.BigDecimal;
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

import br.com.marcielli.BancoM.dto.ContaCorrenteTaxaManutencaoDTO;
import br.com.marcielli.BancoM.dto.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.ContaCreateDepositoDTO;
import br.com.marcielli.BancoM.dto.ContaCreatePixDTO;
import br.com.marcielli.BancoM.dto.ContaCreateSaqueDTO;
import br.com.marcielli.BancoM.dto.ContaCreateTedDTO;
import br.com.marcielli.BancoM.dto.ContaMapper;
import br.com.marcielli.BancoM.dto.ContaResponseDTO;
import br.com.marcielli.BancoM.dto.ContaUpdatePixDTO;
import br.com.marcielli.BancoM.dto.ContaUpdatePixMapper;
import br.com.marcielli.BancoM.dto.ContaUpdatePixResponseDTO;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ContaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/contas")
public class ContaController {

	@Autowired
	private ContaService contaService;

	@Autowired
	private ContaMapper contaMapper;
	
	@Autowired
	private ContaUpdatePixMapper contaUpdatePixMapper;
	
	@PostMapping("") //salvar - Criar uma nova conta.
	public ResponseEntity<ContaResponseDTO> adicionarConta(@Valid @RequestBody ContaCreateDTO contaCreateDTO) {		

		Conta conta = contaMapper.toEntity(contaCreateDTO);

		Conta contaGravada = contaService.save(conta);

		ContaResponseDTO contaResponseDTO = contaMapper.toDTO(contaGravada);

		return ResponseEntity.status(HttpStatus.CREATED).body(contaResponseDTO);

	}	
	
	@GetMapping("/{contaId}") //listar/{clienteId} - Obter detalhes de uma conta
	public Optional<Conta> getContaById(@PathVariable("contaId") Long contaId) {

		Optional<Conta> contaById = contaService.getContaById(contaId);

		if (!contaById.isPresent()) {
			throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
		}

		return contaById;
	}
	
	@PutMapping("/{contaId}") //Atualizar informações de uma conta
	public ResponseEntity<ContaUpdatePixResponseDTO> atualizar(@PathVariable("contaId") Long contaId, @Valid @RequestBody ContaUpdatePixDTO contaUpdatePixDTO) {

		Conta contaAtualizado = contaService.update(contaId, contaUpdatePixDTO);

		ContaUpdatePixResponseDTO contaResponseDTO = contaUpdatePixMapper.toDTO(contaAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(contaResponseDTO);

	}

	@DeleteMapping("/{contaId}") // Remover uma conta
	public ResponseEntity<String> deletar(@PathVariable("contaId") Long contaId) {

		boolean contaDeletada = contaService.deleteConta(contaId);

		if (contaDeletada) {
			return new ResponseEntity<String>("Conta deletada com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("") //listar
	public ResponseEntity<List<Conta>> getContas() {
		List<Conta> contas = contaService.getAll();
		return new ResponseEntity<List<Conta>>(contas, HttpStatus.OK);
	}


	//Pagamentos

	@PostMapping("/{idContaReceber}/transferencia") //TED Transferência
	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreateTedDTO contaTransCreateDTO) {
		
		boolean tedRealizada = contaService.transferirTED(idContaReceber, contaTransCreateDTO);
		
		if(tedRealizada) {
			return new ResponseEntity<String>("Transferência realizada com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}	
	
	@GetMapping("/{clienteId}/saldo") 
	public ResponseEntity<String> exibirSaldo(@PathVariable("clienteId") Long clienteId) {

		BigDecimal saldoAtual = contaService.exibirSaldo(clienteId);

		if (saldoAtual.compareTo(BigDecimal.ZERO) > 0) {
			return new ResponseEntity<String>("Saldo Total: "+saldoAtual, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PostMapping("/{idContaReceber}/pix") //@PostMapping("/transferir/{idContaReceber}/pix")
	public ResponseEntity<String> transferirPIX(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreatePixDTO contaPixCreateDTO) {
		
		boolean pixRealizado = contaService.transferirPIX(idContaReceber, contaPixCreateDTO);
		
		if(pixRealizado) {
			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PostMapping("/{idContaReceber}/deposito") //@PostMapping("/transferir/{idContaReceber}/deposito")
	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreateDepositoDTO contaDepositoCreateDTO) {
		
		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, contaDepositoCreateDTO);
		
		if(depositoRealizado) {
			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PostMapping("/{idContaReceber}/saque") //@PostMapping("/transferir/{idContaReceber}/saque")
	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreateSaqueDTO contaSaqueCreateDTO) {
		
		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, contaSaqueCreateDTO);
		
		if(saqueRealizado) {
			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PutMapping("/{idConta}/manutencao") //Somente Conta Corrente
	public ResponseEntity<String> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta, @Valid @RequestBody ContaCorrenteTaxaManutencaoDTO contaCorrenteTaxaCreateDTO) {
		
		Conta manutencaoCCRealizada = contaService.manutencaoTaxaCC(idConta, contaCorrenteTaxaCreateDTO);
		
		if(manutencaoCCRealizada != null) {
			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	

	@PutMapping("/{idConta}/rendimentos") //Somente Conta Poupança
	public ResponseEntity<String> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta, @Valid @RequestBody ContaCorrenteTaxaManutencaoDTO contaCorrenteTaxaCreateDTO) {
		
		Conta manutencaoCPRealizada = contaService.rendimentoTaxaCP(idConta, contaCorrenteTaxaCreateDTO);
		
		if(manutencaoCPRealizada != null) {
			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
		}
	    
	}
}
