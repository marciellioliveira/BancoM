package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CartaoCreateTedDTO {
	
	@NotNull(message = "O id do cliente origem deve ser informado.")
	private Long idClienteOrigem;
	
	@NotNull(message = "O id da conta origem deve ser informado.")
	private Long idContaOrigem;
	
	@NotNull(message = "O id do cartão origem deve ser informado.")
	private Long idCartaoOrigem;
	
	@NotNull(message = "O valor da transferência deve ser informado.")
	private BigDecimal valor;
	
	//Request Param ID da conta que vai receber

}
