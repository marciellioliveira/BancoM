package br.com.marcielli.BancoM.entity;

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

	private float limiteCreditoPreAprovado;
	
	private float taxaUtilizacao;

	private float taxaSeguroViagem;

	private float totalGastoMesCredito;

}
