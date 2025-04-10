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
import br.com.marcielli.BancoM.entity.TaxasManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
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

		// Buscar cliente por ID
		Optional<Cliente> clienteExiste = clienteRepository.findById(contaParaCriar.getId());

		if (clienteExiste.isPresent()) {

			Cliente simClienteExiste = clienteExiste.get();
			contaParaCriar.setCliente(simClienteExiste);

			// Validar dados
			if (contaParaCriar.getSaldoConta() < 0) {
				throw new ContaTipoContaNaoExisteException("O saldo inicial da conta precisa ser positivo");
			}

			// Padrão de Design Factory

			contaCriada = ContaFactory.criarConta(contaParaCriar);

			if (contaCriada != null) {

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

		Conta contaCorrenteNova = null;
		Conta contaPoupancaNova = null;

		if (dadosParaAtualizar == null) {
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

			if (saldoAntigoDaContaDeletada <= 1000) {

				// Conta Corrente
				taxaManutencaoMensalCC = 12.00f;

				// Todas
				categoriaConta = CategoriaConta.COMUM;

				// Conta Poupança
				taxaAcrescRendPP1 = 0.005f;
				taxaMensalPP2 = (float) (Math.pow(1 + taxaAcrescRendPP1, 1.0 / 12) - 1);

			}

			if (saldoAntigoDaContaDeletada > 1000 && saldoAntigoDaContaDeletada <= 5000) {

				// Conta Corrente
				taxaManutencaoMensalCC = 8.00f;

				// Todas
				categoriaConta = CategoriaConta.SUPER;

				// Conta Poupança
				taxaAcrescRendPP1 = 0.007f;
				taxaMensalPP2 = (float) (Math.pow(1 + taxaAcrescRendPP1, 1.0 / 12) - 1);
			}

			if (saldoAntigoDaContaDeletada > 5000) {

				// Conta Corrente
				taxaManutencaoMensalCC = 0f;

				// Todas
				categoriaConta = CategoriaConta.PREMIUM;

				// Conta Poupança
				taxaAcrescRendPP1 = 0.009f;
				taxaMensalPP2 = (float) (Math.pow(1 + taxaAcrescRendPP1, 1.0 / 12) - 1);
			}

			
			String numConta = gerarNumeroDaConta();

			//Era poupança - > Atualizar Corrente
			if (tipoParaAtualizar == TipoConta.CORRENTE) {

				String numContaCorrente = numConta.concat("-CC");

				taxasDaContaCC = new Taxas(saldoAntigoDaContaDeletada, TipoConta.CORRENTE);
				List<Taxas> novaTaxaCC = new ArrayList<Taxas>();
				novaTaxaCC.add(taxasDaContaCC);

				contaCorrenteNova = new ContaCorrente(clienteAntigoDaContaDeletada, TipoConta.CORRENTE, categoriaConta,
						saldoAntigoDaContaDeletada, numContaCorrente, novaTaxaCC);
				
				contaCorrenteNova.setCategoriaConta(categoriaConta);
				contaCorrenteNova.setTipoConta(TipoConta.CORRENTE);
				
				if(contaCorrenteNova.getTransferencia() != null) {
					contaCorrenteNova.getTransferencia().addAll(transfDaContaDeletada);
				}
				
				contaCorrenteNova.setStatus(true);
				
				if(contaCorrenteNova != null) {
					
					contaAntigaDeletar.setStatus(false);
					contaRepository.save(contaCorrenteNova);
					
				}
				
				//Antigo manter na Poupanca como false
				//Novo, ir para Corrente como true

				return contaCorrenteNova;

			}

			//Era Corrente -> Atualizar Poupança
			if (tipoParaAtualizar == TipoConta.POUPANCA) {

				String numContaPoupanca = numConta.concat("-PP");
				taxasDaContaPP = new Taxas(saldoAntigoDaContaDeletada, TipoConta.POUPANCA);
				List<Taxas> novaTaxaPP = new ArrayList<Taxas>();
				novaTaxaPP.add(taxasDaContaPP);

				contaPoupancaNova = new ContaPoupanca(clienteAntigoDaContaDeletada, TipoConta.POUPANCA, categoriaConta,
						saldoAntigoDaContaDeletada, numContaPoupanca, novaTaxaPP);
				
				contaPoupancaNova.setCategoriaConta(categoriaConta);
				contaPoupancaNova.setTipoConta(TipoConta.POUPANCA);
				
				if(contaPoupancaNova.getTransferencia() !=null) {
					contaPoupancaNova.getTransferencia().addAll(transfDaContaDeletada);
				}
				

				contaPoupancaNova.setStatus(true);
				
				if(contaPoupancaNova != null) {
				
					contaAntigaDeletar.setStatus(false);
					contaRepository.save(contaPoupancaNova);
					
				}

				//Antigo manter na Corrente como false
				//Novo, ir para Poupança como true

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
	public boolean deleteConta(Long clienteId, Long contaId) {

		Optional<Cliente> clienteH2 = clienteRepository.findById(clienteId);
		Optional<Conta> contaH2 = contaRepository.findById(contaId);

		if (clienteH2.isPresent() && contaH2.isPresent()) {
			
				Cliente clienteConta = clienteH2.get();
				Conta contaCliente = contaH2.get();

				for(Conta clienteTemConta : clienteConta.getContas()) {
					if(clienteTemConta.getId() == contaCliente.getId()) {
						
						clienteConta.getContas().remove(clienteTemConta);						
						contaRepository.deleteById(contaCliente.getId());
						//contaCliente.setStatus(false);
						break;
						
					} 
				}
		} else {
			
			throw new ContaNaoEncontradaException("A conta não pode ser deletada porque não existe no banco.");
			
		}

		return true;

	}


	// Transferência TED
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idPessoaReceber, Long idContaReceber, Transferencia dadosContaEnviar) {

		if (idPessoaReceber == null || idContaReceber == null || dadosContaEnviar.getIdClienteOrigem() == null
				|| dadosContaEnviar.getIdContaOrigem() == null) {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Confirme os seus dados.");
		}

		// PathVariable
		Optional<Cliente> encontraRecebedorPorId = clienteRepository.findById(idPessoaReceber);
		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);

		// RequestBody
		Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
		Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());

		float valorTransferencia = dadosContaEnviar.getValor();

		if (valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		if (encontraRecebedorPorId.isPresent() && encontraContaRecebedorPorId.isPresent()
				&& encontraPagadorPorId.isPresent() && encontraContaPagadorPorId.isPresent()) {

			Cliente clienteReceber = encontraRecebedorPorId.get();
			Conta contaReceber = encontraContaRecebedorPorId.get();
			
			Cliente clientePagador = encontraPagadorPorId.get();
			Conta contaPagador = encontraContaPagadorPorId.get();
			
			if(contaReceber.isStatus() == false || contaPagador.isStatus() == false ) {
				throw new ContaNaoRealizouTransferenciaException("Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
			}
			

			if (clienteReceber.getId() != null && contaReceber != null) {

				// Conta pagador -> Request Body (idContaOrigem) -> Conta Recebedor ->
				// PathVariable (id)
				Transferencia novaTransferencia = new Transferencia(contaPagador.getId(), contaReceber.getId());

				List<Conta> contasTransferidas = novaTransferencia.transferirTed(contaPagador, valorTransferencia,
						contaReceber);

				if (contasTransferidas != null) {

					for (Conta contasT : contasTransferidas) {

						// Pagador
						if (contasT.getId() == contaPagador.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaPagador;
								minhaContaCorrente.getTransferencia().add(novaTransferencia);
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaPagador;
								minhaContaPoupanca.getTransferencia().add(novaTransferencia);
								contaRepository.save(minhaContaPoupanca);
							}

						}

						// Recebedor
						if (contasT.getId() == contaReceber.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
								contaRepository.save(minhaContaPoupanca);
							}

						}

					}

				}

			} else {
				throw new ContaNaoRealizouTransferenciaException(
						"O cliente para o qual você está tentando transferir não tem essa conta. Confirme os seus dados.");
			}

		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Confirme os seus dados.");
		}

		return true;
	}

	// Transferência PIX
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirPIX(String pixAleatorio, Transferencia dadosContaEnviar) {

		Conta contaReceber = null;

		// RequestBody
		Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
		Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());

		float valorTransferencia = dadosContaEnviar.getValor();

		if (valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		if (encontraContaPagadorPorId.isPresent() && encontraPagadorPorId.isPresent()) {

			Conta dadosParaEnviar = encontraContaPagadorPorId.get();

			Transferencia novaTransferencia = null;
			
			if(dadosParaEnviar.isStatus() == false ) {
				throw new ContaNaoRealizouTransferenciaException("Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
			}
			
			boolean pixExiste = false;

			for (Conta getContaPix : getAll()) {
				
				if (getContaPix.getTipoConta() == TipoConta.CORRENTE) {

					ContaCorrente minhaContaCorrente = (ContaCorrente) getContaPix;
					

					if (pixAleatorio.equals(minhaContaCorrente.getPixAleatorio())) {
						
						if(minhaContaCorrente.isStatus() == false) {
							throw new ContaNaoRealizouTransferenciaException("Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
						}

						novaTransferencia = new Transferencia(dadosContaEnviar.getIdClienteOrigem(),
								minhaContaCorrente.getCliente().getId());
						contaReceber = minhaContaCorrente;
						
						pixExiste = true;

					}
				}

				if (getContaPix.getTipoConta() == TipoConta.POUPANCA) {

					ContaPoupanca minhaContaPoupanca = (ContaPoupanca) getContaPix;
					
					if (pixAleatorio.equals(minhaContaPoupanca.getPixAleatorio())) {
						
						if(minhaContaPoupanca.isStatus() == false) {
							throw new ContaNaoRealizouTransferenciaException("Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
						}

						novaTransferencia = new Transferencia(dadosContaEnviar.getIdClienteOrigem(),
								minhaContaPoupanca.getCliente().getId());
						contaReceber = minhaContaPoupanca;
					}
					
					pixExiste = true;

				}
			}
			
			if(pixExiste == false) {
				//Pix não existe
				throw new ContaNaoRealizouTransferenciaException("O PIX "+pixAleatorio+" que você digitou é inválido. Tente transferir para um PIX válido.");
				
			}
			
			List<Conta> contasTransferidas = novaTransferencia.transferirPix(dadosParaEnviar, valorTransferencia,
					contaReceber);

			if (contasTransferidas != null) {

				for (Conta contasT : contasTransferidas) {

					// Pagador
					if (contasT.getId() == dadosParaEnviar.getId()) {

						if (contasT.getTipoConta() == TipoConta.CORRENTE) {

							ContaCorrente minhaContaCorrente = (ContaCorrente) dadosParaEnviar;
							minhaContaCorrente.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaCorrente);

						}

						if (contasT.getTipoConta() == TipoConta.POUPANCA) {

							ContaPoupanca minhaContaPoupanca = (ContaPoupanca) dadosParaEnviar;
							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaPoupanca);
						}

					}

					// Recebedor
					if (contasT.getId() == contaReceber.getId()) {

						if (contasT.getTipoConta() == TipoConta.CORRENTE) {

							ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
							contaRepository.save(minhaContaCorrente);

						}

						if (contasT.getTipoConta() == TipoConta.POUPANCA) {

							ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
							contaRepository.save(minhaContaPoupanca);
						}

					}

				}

			}

		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"O PIX não foi realizado. Confirme os seus dados.");
		}

		return true;
	}

	// Transferência DEPOSITO
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirDEPOSITAR(Long idClienteReceber, Long idContaReceber, Transferencia dadosContaEnviar) {
		
		if(idClienteReceber == null || idContaReceber == null) {
			throw new ContaNaoRealizouTransferenciaException(
					"O depósito não foi realizado. Confirme os seus dados.");
		}
				
		//Param
		Optional<Cliente> encontrarClienteRecebedorPorId = clienteRepository.findById(idClienteReceber);
		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);
		
		float valorDeposito = dadosContaEnviar.getValor();

		if (valorDeposito <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"O depósito não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}
		
		if( encontraContaRecebedorPorId.isPresent() && encontrarClienteRecebedorPorId.isPresent()) {
			
			//Conta dadosParaEnviar = encontraContaPagadorPorId.get();
			Conta contaReceber = encontraContaRecebedorPorId.get();
			Cliente clienteReceber = encontrarClienteRecebedorPorId.get();
			
			if(contaReceber.isStatus() == false) {
				throw new ContaNaoRealizouTransferenciaException(
						"A conta foi desativada.  Confirme os seus dados e faça o depósito em uma conta ativa.");
			}
			
			boolean clienteTemConta = false;
			for(Conta contas : clienteReceber.getContas()) {
				if(contas.getId() == contaReceber.getId()) {
					clienteTemConta = true;
				}
			}
			
			if(clienteTemConta) {
				
				Transferencia novoDeposito = new Transferencia();

				List<Conta> contasDeposito = novoDeposito.depositar(valorDeposito, contaReceber);

				if (contasDeposito != null) {
					
					for (Conta contasT : contasDeposito) {
						
						// Recebedor
						if (contasT.getId() == contaReceber.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
								minhaContaCorrente.getTransferencia().add(novoDeposito);
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
								minhaContaPoupanca.getTransferencia().add(novoDeposito);
								contaRepository.save(minhaContaPoupanca);
							}

						}

					}
					
				}
				
			}
			
			
			
			
		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"O depósito não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		
		return true;
		
	}

	// Transferência sacar
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirSACAR(Long idClientePegar, Long idContaPegar, Transferencia dadosContaEnviar) {
		
		if(idClientePegar == null || idContaPegar == null) {
			throw new ContaNaoRealizouTransferenciaException(
					"O saque não foi realizado. Confirme os seus dados.");
		}
				
		//Param
		Optional<Cliente> encontrarClienteSaquePorId = clienteRepository.findById(idClientePegar);
		Optional<Conta> encontraContaSaquePorId = contaRepository.findById(idContaPegar);
		
		float valorSaque = dadosContaEnviar.getValor();

		if (valorSaque <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"O saque não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}
		
		if(encontraContaSaquePorId.isPresent() && encontrarClienteSaquePorId.isPresent()) {
			
			
			//Conta dadosParaEnviar = encontraContaPagadorPorId.get();
			Conta contaSacar = encontraContaSaquePorId.get();
			Cliente clienteSacar = encontrarClienteSaquePorId.get();
			
			
			if(contaSacar.isStatus() == false) {
				throw new ContaNaoRealizouTransferenciaException(
						"A conta foi desativada.  Confirme os seus dados e faça o saque através de uma conta ativa.");
			}
			
			
			boolean clienteTemConta = false;
			for(Conta contas : clienteSacar.getContas()) {
				if(contas.getId() == contaSacar.getId()) {
					clienteTemConta = true;
				}
			}
			
			if(clienteTemConta) {
				
				Transferencia novoSaque = new Transferencia();

				List<Conta> contasSaque = novoSaque.sacar(valorSaque, contaSacar);

				if (contasSaque != null) {
					
					for (Conta contasT : contasSaque) {
						
						// Retirou
						if (contasT.getId() == contaSacar.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaSacar;
								minhaContaCorrente.getTransferencia().add(novoSaque);
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaSacar;
								minhaContaPoupanca.getTransferencia().add(novoSaque);
								contaRepository.save(minhaContaPoupanca);
							}

						}

					}
					
				}
				
			}			
			
		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"O saque não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		
		return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public float[] exibirSaldo(Long clienteId) {

		Optional<Cliente> clienteVerSaldo = clienteRepository.findById(clienteId);

		float[] saldoContas = { 0, 0, 0 };

		if (clienteVerSaldo.isPresent()) {

			Cliente cliente = clienteVerSaldo.get();
			
			for(Conta getContas : cliente.getContas()) {
				
				if(getContas.isStatus() == true && getContas.getTipoConta() == TipoConta.CORRENTE) {
					saldoContas[0] += getContas.getSaldoConta();
					
					if(getContas.isStatus() == false) {
						saldoContas[0] += 0;
					}
				}
				
				if(getContas.isStatus() == true && getContas.getTipoConta() == TipoConta.POUPANCA) {
					saldoContas[1] += getContas.getSaldoConta();					
					
					if(getContas.isStatus() == false) {
						saldoContas[1] += 0;
					}
				}
				
				
				
				
				
			}

		}

		saldoContas[2] = saldoContas[0] + saldoContas[1];

		return saldoContas;

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


}
