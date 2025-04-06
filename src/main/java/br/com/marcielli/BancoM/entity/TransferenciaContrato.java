package br.com.marcielli.BancoM.entity;

import java.util.List;

public interface TransferenciaContrato {
	
	//public String getTipo();
	
	public String gerarCodigoTransferencia();
	
	public List<Conta> transferirTed(Conta enviar, float valor, Conta receber);
	
	public boolean transferirPix(Conta enviar, float valor, Conta receber);
	
	public boolean depositar(float valor, Conta receber);
	
	public boolean sacar(float valor, Conta receber);
	
	public float exibirSaldo(Conta conta);
	
}
