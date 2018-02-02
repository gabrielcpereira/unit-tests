package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;

public class GeradorDePagamentos {
	private final RepositorioDePagamentos pagamentos;
	private final RepositorioDeLeiloes leiloes;
	private final Avaliador avaliador;
	private Relogio relogio;	
	
	public GeradorDePagamentos(RepositorioDeLeiloes leiloes, RepositorioDePagamentos pagamentos, Avaliador avaliador, Relogio relogio) {
		this.leiloes = leiloes;
		this.pagamentos = pagamentos;
		this.avaliador = avaliador;
		this.relogio = relogio;
	}

	public void gera() {
		List<Leilao> leiloesEncerrados = leiloes.encerrados();
		for (Leilao leilao : leiloesEncerrados) {
			avaliador.avalia(leilao);

			Calendar diaPagamento = this.obterDiaUtil(this.relogio.hoje());
			Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), diaPagamento);
			pagamentos.salva(novoPagamento);
		}
	}

	private Calendar obterDiaUtil(Calendar hoje) {
		Calendar dataRetorno = hoje;
		int dia = 1;
		while (hoje.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY 
				|| hoje.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {			
			dataRetorno.add(Calendar.DAY_OF_WEEK, dia);
		}
		
		return dataRetorno;
	}
}
