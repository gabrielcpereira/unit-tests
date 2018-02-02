package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.dominio.Pagamento;

public interface RepositorioDePagamentos {
	public void salva(Pagamento p);
}
