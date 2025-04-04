package br.com.marcielli.BancoM.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.Funcao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ClienteNaoTemSaldoSuficienteException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaNaoRealizouTransferenciaException;
import br.com.marcielli.BancoM.exception.ContaTipoContaNaoExisteException;
import br.com.marcielli.BancoM.exception.ContaTipoNaoPodeSerAlteradaException;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;
	
	@Transactional
	public Conta save(Conta conta) {
		
		//Validar dados
		if(conta.getSaldoConta() < 0) {
			throw new ContaTipoContaNaoExisteException("O saldo inicial da conta precisa ser positivo");
		}
		
		String numConta = gerarNumeroDaConta(conta);
		CategoriaConta categoriaConta = null;
		Cliente novoCliente = null;

		if(conta.getTipoConta().getDescricao().equalsIgnoreCase("CORRENTE")) {
			
			String numContaCorrente = numConta.concat("-CC");
			
			Conta contaCorrente = new ContaCorrente(conta.getCliente(), TipoConta.CORRENTE, conta.getSaldoConta(), numContaCorrente);

			
			return contaRepository.save(contaCorrente);
			
		} else if(conta.getTipoConta().getDescricao().equalsIgnoreCase("POUPANCA")) {
			
			String numContaPoupanca = numConta.concat("-PP");
		
			Conta contaPoupanca = new ContaPoupanca(conta.getCliente(), TipoConta.POUPANCA, conta.getSaldoConta(), numContaPoupanca);
			
			return contaRepository.save(contaPoupanca);
			
		}
		
		return null;
	}
	
	@Transactional
	public Conta update(Conta conta, Long id) {
		
		Optional<Conta> contaH2 = contaRepository.findById(id);
		
		TipoConta contaAntiga = conta.getTipoConta();
		
		Conta contaAtualizada = null;
		
		if(contaH2.isPresent()) {
			
			if(conta.getTipoConta().getDescricao().equalsIgnoreCase("CORRENTE")) {
				
				if(contaH2.isPresent()) {
					
					CategoriaConta categoriaNova = null;
					float novaTaxaManutencaoMensal = 0;
					contaAtualizada = contaH2.get();					
					
					for(Conta contas : getAll()) {
						if(contas instanceof ContaCorrente) {
							if(contas.getId() == contaAtualizada.getId()) {

								((ContaCorrente) contas).setTaxaManutencaoMensal(novaTaxaManutencaoMensal);
								((ContaCorrente) contas).setTipoConta(conta.getTipoConta());
								((ContaCorrente) contas).setSaldoConta(conta.getSaldoConta());
								((ContaCorrente) contas).atualizaCategoria(conta.getSaldoConta());
							}
						}
					}

					
					return contaRepository.save(contaAtualizada);
				}
				
				
			} else if(conta.getTipoConta().getDescricao().equalsIgnoreCase("POUPANCA")) {
				
				if(contaH2.isPresent()) {
					
					CategoriaConta categoriaNova = null;
					float novaTaxaAcrescRend = 0;
					float novaTaxaMensal = 0;
					contaAtualizada = contaH2.get();
						
					for(Conta contas : getAll()) {
						if(contas instanceof ContaPoupanca) {
							if(contas.getId() == contaAtualizada.getId()) {
								
								if(contaAtualizada.getSaldoConta() <= 1000f) {
									categoriaNova = CategoriaConta.COMUM;
									
									novaTaxaAcrescRend = 0.005f;						
									novaTaxaMensal = (float) (Math.pow(1+novaTaxaAcrescRend, 1.0/12) - 1);

								}
								
								if(contaAtualizada.getSaldoConta() > 1000f && contaAtualizada.getSaldoConta() <= 5000f) {
									categoriaNova = CategoriaConta.SUPER;
									
									novaTaxaAcrescRend = 0.007f;
									novaTaxaMensal = (float) (Math.pow(1+novaTaxaAcrescRend, 1.0/12) - 1);
								}
								
								if(contaAtualizada.getSaldoConta() > 5000f) {
									categoriaNova = CategoriaConta.PREMIUM;
									
									novaTaxaAcrescRend = 0.009f;						
									novaTaxaMensal = (float) (Math.pow(1+novaTaxaAcrescRend, 1.0/12) - 1);
								}
								
								((ContaPoupanca) contas).setTipoConta(conta.getTipoConta());
								((ContaPoupanca) contas).setSaldoConta(conta.getSaldoConta());
								((ContaPoupanca) contas).atualizaCategoria(conta.getSaldoConta());
								((ContaPoupanca) contas).atualizaTaxas(conta.getSaldoConta());
							}
						}
					}
					
					return contaRepository.save(contaAtualizada);
				}
				
			}	
			
		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco.");
		}
		return contaAtualizada;		
	}
	
	public List<Conta> getAll(){
		
		List<Conta> contasH2 = contaRepository.findAll();
		
		if(contasH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existem contas cadastradas no banco.");
		}
		
		return contasH2;
	}
	
	public Optional<Conta> getContaById(Long id){	
		
		Optional<Conta> contaH2 = contaRepository.findById(id);
		
		if(!contaH2.isPresent()) {
			throw new ContaNaoEncontradaException("Conta não encontrada.");
		}
		
		return contaH2;
	}
	
	public String delete(Long contaId) {
		
		Optional<Conta> contaH2 = contaRepository.findById(contaId);
		
		if(contaH2.isPresent()) {
			contaRepository.deleteById(contaId);
			return "deletado";
		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser deletada porque não existe no banco.");
		}
		
	}
	
	
	//Transferência
	@Transactional
	public boolean transferirTED(Long idReceber, Conta contaEnviar) {
		
		Optional<Conta> contaReceber = contaRepository.findById(idReceber);
		Optional<Conta> contaPagar = contaRepository.findById(contaEnviar.getId());
		
		float valorTransferencia = contaEnviar.getValorTransferencia();
		System.err.println("Cota enviar valor: "+contaEnviar.getValorTransferencia());
		
		if(contaReceber.isPresent() && contaPagar.isPresent()) {
			
			Conta recebedor = contaReceber.get();
			Conta pagador = contaPagar.get();		
			Transferencia novaTransferencia = new Transferencia();			
			
			if(novaTransferencia.transferirTed(pagador, valorTransferencia, recebedor)) {
				recebedor.getTransferencia().add(novaTransferencia);
				pagador.getTransferencia().add(novaTransferencia);
				
				contaRepository.save(pagador);
				contaRepository.save(recebedor);
				
			} else {
				throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Confirme os seus dados.");
			}	
			
		} else {
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Confirme os seus dados.");
		}
		
		
		return true;
	}
	
	
	
	//Outros métodos
	
	public String gerarNumeroDaConta(Conta conta) {

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
	
//	public String gerarCodigoTransferencia() {
//
//		int[] sequencia = new int[21];
//		Random random = new Random();
//		String codTransferencia = "";
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//		}
//
//		for (int i = 0; i < sequencia.length; i++) {
//			codTransferencia += Integer.toString(sequencia[i]);
//		}
//
//		return codTransferencia;
//	}
//	

	
	
	
}
