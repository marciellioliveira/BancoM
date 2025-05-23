package br.com.marcielli.BancoM.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.service.AgendarAplicacaoTaxasContasService;

@RestController
@RequestMapping("/api")
public class TesteController {
	
    @Autowired
    private AgendarAplicacaoTaxasContasService agendador;

    @PutMapping("/teste-agendador")
    public String teste() {
        agendador.aplicarTaxaManutencaoMensal();
        return "Método executado manualmente!";
    }
}