package br.com.marcielli.BancoM.dto.security;

import br.com.marcielli.BancoM.enuns.TipoCartao;

public record CartaoCreateDTO(Long idUsuario, Long idConta, TipoCartao tipoCartao, String senha) {

}
