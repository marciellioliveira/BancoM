package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@PrimaryKeyJoinColumn(name = "idCartao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartaoCredito extends Cartao {

	private static final long serialVersionUID = 1L;

	private BigDecimal limiteCreditoPreAprovado = new BigDecimal("600");
	
	private BigDecimal taxaUtilizacao;

	private BigDecimal taxaSeguroViagem;

	private BigDecimal totalGastoMesCredito = BigDecimal.ZERO;
	
	
	public void atualizarTotalGastoMes(BigDecimal valor) {		
		
		this.totalGastoMesCredito = totalGastoMesCredito.add(valor);		
		
	}
	
	public void atualizarLimiteCreditoPreAprovado(BigDecimal valor) {
		
		this.limiteCreditoPreAprovado = limiteCreditoPreAprovado.subtract(valor);		
	}
	
	public void alterarLimiteCreditoPreAprovado(BigDecimal valor) {
		this.limiteCreditoPreAprovado = valor;
	}

}
