package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaFactory;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Taxas;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExibirSaldoErroException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaNaoFoiPossivelAlterarNumeroException;
import br.com.marcielli.BancoM.exception.ContaNaoRealizouTransferenciaException;
import br.com.marcielli.BancoM.exception.ContaTipoContaNaoExisteException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta save(Conta contaParaCriar) {
		
		Conta contaCriada = null;
		
		if (contaParaCriar.getId() == null) {	
			throw new ClienteNaoEncontradoException("Para cadastrar uma conta, você precisa ser um cliente do banco.");
		} 

		//Buscar cliente por ID
		Optional<Cliente> clienteExiste = clienteRepository.findById(contaParaCriar.getId());
		
		if(clienteExiste.isPresent()) {		
			
			Cliente simClienteExiste = clienteExiste.get();
			contaParaCriar.setCliente(simClienteExiste);
			
			// Validar dados
			if (contaParaCriar.getSaldoConta() < 0) {
				throw new ContaTipoContaNaoExisteException("O saldo inicial da conta precisa ser positivo");
			}
			
			//Padrão de Design Factory	
			
			contaCriada = ContaFactory.criarConta(contaParaCriar);
			
			if(contaCriada != null) {				
				
				contaRepository.save(contaCriada);
				simClienteExiste.getContas().add(contaCriada);
				clienteRepository.save(simClienteExiste);
			} 	
			
		} else {
			throw new ClienteNaoEncontradoException("Para cadastrar uma conta, você precisa ser um cliente do banco.");
		}
		
		return contaCriada;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta update(Long idContaParaAtualizar, Conta dadosParaAtualizar) {
		
		Optional<Conta> contaParaAtualizar = contaRepository.findById(idContaParaAtualizar);
		
		
		Conta contaAtualizada = null;
		Conta contaAntigaDeletar = null;
		
		//System.err.println("Tipo "+dadosParaAtualizar);
		
		if(dadosParaAtualizar == null) {
			throw new ContaNaoEncontradaException("Não é possível atualizar a conta no momento!");
		}
		
		TipoConta tipoParaAtualizar = dadosParaAtualizar.getTipoConta();
		
		
		if (contaParaAtualizar.isPresent()) {
			
			contaAntigaDeletar = contaParaAtualizar.get();
			float saldoAntigoDaContaDeletada = contaAntigaDeletar.getSaldoConta();
			Cliente clienteAntigoDaContaDeletada = contaAntigaDeletar.getCliente();	
			List<Transferencia> transfDaContaDeletada = contaAntigaDeletar.getTransferencia();
			
			
			CategoriaConta categoriaConta = null;
			float taxaManutencaoMensalCC = 0;	
			float taxaAcrescRendPP1 = 0;
			float taxaMensalPP2 = 0;			
			Taxas taxasDaContaPP = null;
			Taxas taxasDaContaCC = null;

			if(saldoAntigoDaContaDeletada <= 1000) {
				
				//Conta Corrente
				taxaManutencaoMensalCC = 12.00f;
				
				//Todas
				categoriaConta = CategoriaConta.COMUM;
				
				//Conta Poupança
				taxaAcrescRendPP1 = 0.005f;	
				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
				
				
			}
			
			if(saldoAntigoDaContaDeletada > 1000 && saldoAntigoDaContaDeletada <= 5000) {
				
				//Conta Corrente
				taxaManutencaoMensalCC = 8.00f;
				
				//Todas
				categoriaConta = CategoriaConta.SUPER;
				
				//Conta Poupança
				taxaAcrescRendPP1 = 0.007f;
				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
			}
			
			if(saldoAntigoDaContaDeletada > 5000) {
				
				//Conta Corrente
				taxaManutencaoMensalCC = 0f;	
				
				//Todas
				categoriaConta = CategoriaConta.PREMIUM;
				
				//Conta Poupança
				taxaAcrescRendPP1 = 0.009f;	
				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);				
			}	
			
			String numConta = gerarNumeroDaConta();
			
			if(tipoParaAtualizar == TipoConta.CORRENTE) {
				
				String numContaCorrente = numConta.concat("-CC");
				
				taxasDaContaCC = new Taxas(saldoAntigoDaContaDeletada, TipoConta.CORRENTE);
				List<Taxas> novaTaxaCC = new ArrayList<Taxas>();
				novaTaxaCC.add(taxasDaContaCC);
				
				Conta contaCorrenteNova = new ContaCorrente(clienteAntigoDaContaDeletada, TipoConta.CORRENTE, categoriaConta, saldoAntigoDaContaDeletada, numContaCorrente,novaTaxaCC);
				contaCorrenteNova.setTransferencia(transfDaContaDeletada);
				
				contaRepository.save(contaCorrenteNova);		
				contaRepository.deleteById(contaAntigaDeletar.getId());
				
				return contaCorrenteNova;
				
			}
			
			if(tipoParaAtualizar == TipoConta.POUPANCA) {
				
				String numContaPoupanca = numConta.concat("-PP");
				taxasDaContaPP = new Taxas(saldoAntigoDaContaDeletada, TipoConta.POUPANCA);
				List<Taxas> novaTaxaPP = new ArrayList<Taxas>();
				novaTaxaPP.add(taxasDaContaPP);
				
				Conta contaPoupancaNova = new ContaCorrente(clienteAntigoDaContaDeletada, TipoConta.POUPANCA, categoriaConta, saldoAntigoDaContaDeletada, numContaPoupanca,novaTaxaPP);
				contaPoupancaNova.setTransferencia(transfDaContaDeletada);
				
				contaRepository.save(contaPoupancaNova);	
				contaRepository.deleteById(contaAntigaDeletar.getId());
				
				return contaPoupancaNova;
				
			}
			
			return null;


		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco.");
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> getAll() {

		List<Conta> contasH2 = contaRepository.findAll();

		if (contasH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existem contas cadastradas no banco.");
		}

		return contasH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Conta> getContaById(Long id) {

		Optional<Conta> contaH2 = contaRepository.findById(id);

		if (!contaH2.isPresent()) {
			throw new ContaNaoEncontradaException("Conta não encontrada.");
		}

		return contaH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String delete(Long contaId) {

		Optional<Conta> contaH2 = contaRepository.findById(contaId);

		if (contaH2.isPresent()) {
			contaRepository.deleteById(contaId);
			return "deletado";
		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser deletada porque não existe no banco.");
		}

	}

	// Transferência TED
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idPessoaReceber, Long idContaReceber, Transferencia dadosContaEnviar) {
		
		if(idPessoaReceber == null || idContaReceber == null || dadosContaEnviar.getIdClienteOrigem() == null || dadosContaEnviar.getIdContaOrigem() == null) {		
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Confirme os seus dados.");
		}		
				
		//PathVariable
		Optional<Cliente> encontraRecebedorPorId = clienteRepository.findById(idPessoaReceber);		
		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);	
		
		//RequestBody
		Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
		Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());
		
		float valorTransferencia = dadosContaEnviar.getValor();		
		
		if(valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Valor precisa ser maior que 0. Confirme os seus dados.");
		}
		
		if(encontraRecebedorPorId.isPresent() && encontraContaRecebedorPorId.isPresent() && encontraPagadorPorId.isPresent() && encontraContaPagadorPorId.isPresent()) {
			
		Cliente clienteReceber = encontraRecebedorPorId.get();
		Conta contaReceber = encontraContaRecebedorPorId.get();
		
		Cliente clientePagador = encontraPagadorPorId.get();
		Conta contaPagador = encontraContaPagadorPorId.get();
			
			if(clienteReceber.getId() != null && contaReceber != null) {
				
			//Conta pagador -> Request Body (idContaOrigem) -> Conta Recebedor -> PathVariable (id)
			Transferencia novaTransferencia = new Transferencia(contaPagador.getId(),contaReceber.getId());
			
			List<Conta> contasTransferidas = novaTransferencia.transferirTed(contaPagador, valorTransferencia, contaReceber);
			
			if (contasTransferidas != null) {	
				
				for(Conta contasT : contasTransferidas) {
					
					//Pagador
					if(contasT.getId() == contaPagador.getId()) {
						
						if(contasT.getTipoConta() == TipoConta.CORRENTE) {		
							
							ContaCorrente minhaContaCorrente = (ContaCorrente)contaPagador;
							minhaContaCorrente.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaCorrente);
							
						}
						
						if(contasT.getTipoConta() == TipoConta.POUPANCA) {
	
							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)contaPagador;
							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaPoupanca);
						}
						
					}
					
					//Recebedor
					if(contasT.getId() == contaReceber.getId()) {
						
						if(contasT.getTipoConta() == TipoConta.CORRENTE) {	
							
							ContaCorrente minhaContaCorrente = (ContaCorrente)contaReceber;								
							contaRepository.save(minhaContaCorrente);
							
						}
						
						if(contasT.getTipoConta() == TipoConta.POUPANCA) {
							
							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)contaReceber;							
							contaRepository.save(minhaContaPoupanca);
						}
						
					}
					
				}

			}

				
			} else {
				throw new ContaNaoRealizouTransferenciaException("O cliente para o qual você está tentando transferir não tem essa conta. Confirme os seus dados.");
			}			
			
		} else {
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Confirme os seus dados.");
		}
		
		return true;
	}
	
	
	// Transferência PIX
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirPIX(Long idContaReceber, Transferencia dadosContaEnviar) {
		
		if(idContaReceber == null || dadosContaEnviar.getId() == null ||  dadosContaEnviar.getIdClienteOrigem() == null ) {
			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
		}
		
		//Origem
		Optional<Conta> buscarIdContaEnviar = contaRepository.findById(dadosContaEnviar.getId());
				
		//Destino
		Optional<Conta> buscarIdContaReceber = contaRepository.findById(idContaReceber);
		
		float valorTransferencia = dadosContaEnviar.getValor();		
		
		if(valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}
		
		if(buscarIdContaEnviar.isPresent() && buscarIdContaReceber.isPresent()) {
			
			Conta pagador = buscarIdContaEnviar.get();
			Conta recebedor = buscarIdContaReceber.get();
			
			Transferencia novaTransferencia = new Transferencia(pagador.getCliente().getId(), recebedor.getCliente().getId());
			
			List<Conta> contasPixTransferidos = novaTransferencia.transferirPix(pagador, valorTransferencia, recebedor);
			
			if (contasPixTransferidos != null) {	
				
				for(Conta contasTPix : contasPixTransferidos) {
					
					//Pagador
					if(contasTPix.getId() == pagador.getId()) {
						
						if(contasTPix.getTipoConta() == TipoConta.CORRENTE) {		
							
							ContaCorrente minhaContaCorrente = (ContaCorrente)pagador;
							minhaContaCorrente.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaCorrente);
							
						}
						
						if(contasTPix.getTipoConta() == TipoConta.POUPANCA) {
	
							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)pagador;
							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaPoupanca);
						}
						
					}
					
					
					//Recebedor
					if(contasTPix.getId() == recebedor.getId()) {
						
						if(contasTPix.getTipoConta() == TipoConta.CORRENTE) {	
							
							ContaCorrente minhaContaCorrente = (ContaCorrente)recebedor;								
							contaRepository.save(minhaContaCorrente);
							
						}
						
						if(contasTPix.getTipoConta() == TipoConta.POUPANCA) {
							
							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)recebedor;							
							contaRepository.save(minhaContaPoupanca);
						}
						
					}
				}
				
				
		
			
		} else {
			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
		}
	}
		
		return true;
}
	
	
	// Transferência DEPOSITO
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirDEPOSITO(Long idContaReceber, Transferencia dadosContaEnviar) {
//		
//		if(idContaReceber == null || dadosContaEnviar.getId() == null ||  dadosContaEnviar.getIdClienteOrigem() == null ) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		//Origem
//		Optional<Conta> buscarIdContaEnviar = contaRepository.findById(dadosContaEnviar.getId());
//				
//		//Destino
//		Optional<Conta> buscarIdContaReceber = contaRepository.findById(idContaReceber);
//		
//		float valorTransferencia = dadosContaEnviar.getValor();		
//		
//		if(valorTransferencia <= 0) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
//		
//		if(buscarIdContaEnviar.isPresent() && buscarIdContaReceber.isPresent()) {
//			
//			
//		} else {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		return true;
//	}
//	
	
	// Transferência SAQUE
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirSAQUE(Long idContaReceber, Transferencia dadosContaEnviar) {
//		
//		if(idContaReceber == null || dadosContaEnviar.getId() == null ||  dadosContaEnviar.getIdClienteOrigem() == null ) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		//Origem
//		Optional<Conta> buscarIdContaEnviar = contaRepository.findById(dadosContaEnviar.getId());
//				
//		//Destino
//		Optional<Conta> buscarIdContaReceber = contaRepository.findById(idContaReceber);
//		
//		float valorTransferencia = dadosContaEnviar.getValor();		
//		
//		if(valorTransferencia <= 0) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
//		
//		if(buscarIdContaEnviar.isPresent() && buscarIdContaReceber.isPresent()) {
//			
//			
//		} else {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		return true;
//	}
	
	// MANUTENÇÃO - APLICAR TAXA MANUTENÇÃO MENSAL PARA CONTA CORRENTE
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirMANUTENCAO(Long idContaReceber, Transferencia dadosContaEnviar) {
//		
//		if(idContaReceber == null || dadosContaEnviar.getId() == null ||  dadosContaEnviar.getIdClienteOrigem() == null ) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		//Origem
//		Optional<Conta> buscarIdContaEnviar = contaRepository.findById(dadosContaEnviar.getId());
//				
//		//Destino
//		Optional<Conta> buscarIdContaReceber = contaRepository.findById(idContaReceber);
//		
//		float valorTransferencia = dadosContaEnviar.getValor();		
//		
//		if(valorTransferencia <= 0) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
//		
//		if(buscarIdContaEnviar.isPresent() && buscarIdContaReceber.isPresent()) {
//			
//			
//		} else {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		return true;
//	}
	
	//MANUTENÇÃO - APLICAR RENDIMENTOS PARA CONTA POUPANÇA
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirRENDIMENTO(Long idContaReceber, Transferencia dadosContaEnviar) {
//		
//		if(idContaReceber == null || dadosContaEnviar.getId() == null ||  dadosContaEnviar.getIdClienteOrigem() == null ) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		//Origem
//		Optional<Conta> buscarIdContaEnviar = contaRepository.findById(dadosContaEnviar.getId());
//				
//		//Destino
//		Optional<Conta> buscarIdContaReceber = contaRepository.findById(idContaReceber);
//		
//		float valorTransferencia = dadosContaEnviar.getValor();		
//		
//		if(valorTransferencia <= 0) {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
//		
//		if(buscarIdContaEnviar.isPresent() && buscarIdContaReceber.isPresent()) {
//			
//			
//		} else {
//			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
//		}
//		
//		return true;
//	}
	
	
	
	
	
	
	
	
	

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public float exibirSaldo(Long idConta) {

		Optional<Conta> contaVerSaldo = contaRepository.findById(idConta);

		float exibirSaldo = 0;

		if (contaVerSaldo.isPresent()) {

			Conta exibirSaldoConta = contaVerSaldo.get();

			Transferencia saldoAtual = new Transferencia();
			exibirSaldo = saldoAtual.exibirSaldo(exibirSaldoConta);

		} else {
			throw new ContaExibirSaldoErroException(
					"Não foi possível exibir os dados da conta no momento. Tente mais tarde.");
		}

		return exibirSaldo;
	}

	// Outros métodos
	public String gerarNumeroDaConta() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String minhaConta = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			minhaConta += Integer.toString(sequencia[i]);
		}

		return minhaConta;
	}
//	public String gerarNumeroDaConta(Conta conta) {
//
//		int[] sequencia = new int[8];
//		Random random = new Random();
//		String minhaConta = "";
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//		}
//
//		for (int i = 0; i < sequencia.length; i++) {
//			minhaConta += Integer.toString(sequencia[i]);
//		}
//
//		return minhaConta;
//	}

//	public String atualizarNumeroDaConta(String numeroConta) {
//
//		String doisUltimosDigitos = null;
//		String novoNumConta = null;
//
//		if (numeroConta.length() > 2) {
//
//			doisUltimosDigitos = numeroConta.substring(numeroConta.length() - 2);
//
//			if (numeroConta.equalsIgnoreCase("CC")) {
//
//				novoNumConta = numeroConta.replaceAll("CC", "PP");
//
//			} else if (numeroConta.equalsIgnoreCase("PP")) {
//
//				novoNumConta = numeroConta.replaceAll("PP", "CC");
//
//			} else {
//				throw new ContaNaoFoiPossivelAlterarNumeroException(
//						"Não foi possível alterar o número da conta no momento.");
//			}
//
//		} else {
//			doisUltimosDigitos = null;
//		}
//
//		return novoNumConta;
//	}	

}
