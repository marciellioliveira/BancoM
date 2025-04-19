package br.com.marcielli.BancoM.exception;

public class SeguroNaoEncontradoException  extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SeguroNaoEncontradoException() { super("Seguro não encontrado."); }
	
	public SeguroNaoEncontradoException(String message) { super(message); }
}
