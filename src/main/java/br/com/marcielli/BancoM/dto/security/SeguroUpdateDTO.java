package br.com.marcielli.BancoM.dto.security;

import br.com.marcielli.BancoM.enuns.TipoSeguro;

public record SeguroUpdateDTO(Long idUsuario, TipoSeguro tipo) {

}
