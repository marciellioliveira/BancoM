package br.com.marcielli.BancoM.entity;

import java.util.List;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "idConta")
public class ContaCorrente extends Conta {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private float taxaManutencaoMensal;
	
	public ContaCorrente() {}	
	
	//Testando esse
	public ContaCorrente(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, List<Taxas> taxas) {
		super(cliente, tipoConta, categoriaConta, saldoConta, numeroConta, taxas);
		
		for(Taxas taxasCorrente : taxas) {
			if(taxasCorrente.getTipoConta() == TipoConta.CORRENTE) {
				this.taxaManutencaoMensal = taxasCorrente.getTaxaManutencaoMensal();
			} 
		}
		
	}

	
	//Usava esse
//	public ContaCorrente(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, float taxaManutencaoMensal, List<Taxas> taxas) {
//		super(cliente, tipoConta, categoriaConta, saldoConta, numeroConta, taxas);
//		this.taxaManutencaoMensal = taxaManutencaoMensal;
//	}
//
//	public ContaCorrente(Cliente cliente, TipoConta tipoConta, float saldoConta, String numeroConta) {
//		super(cliente, tipoConta, saldoConta, numeroConta);		
//	}
	
	

	public float getTaxaManutencaoMensal() {
		return taxaManutencaoMensal;
	}

	public void setTaxaManutencaoMensal(float taxaManutencaoMensal) {
		this.taxaManutencaoMensal = taxaManutencaoMensal;
	}

	@Override
	public String toString() {
		return "ContaCorrente [taxaManutencaoMensal=" + taxaManutencaoMensal + "]";
	}


	
	


	

}
