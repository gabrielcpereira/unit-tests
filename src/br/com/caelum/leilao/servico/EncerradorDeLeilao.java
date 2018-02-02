package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;

public class EncerradorDeLeilao {

    private final EnviadorDeEmail carteiro;
	private RepositorioDeLeiloes dao;
//	private LeilaoDao dao;
	private int total = 0;

//	public EncerradorDeLeilao(LeilaoDao dao) {
//		this.dao = dao;
//	}
	
	public EncerradorDeLeilao(RepositorioDeLeiloes dao, EnviadorDeEmail carteiro) {
		this.dao = dao;
		this.carteiro = carteiro;
	}
	
	public void encerra() {
		List<Leilao> todosLeiloesCorrentes = dao.correntes();
		for (Leilao leilao : todosLeiloesCorrentes) {
			try {
				if (!this.comecouSemanaPassada(leilao)) {
					continue;
				}								
				this.dao.atualiza(leilao);								
				this.carteiro.envia(leilao);
				
				leilao.encerra();
				total++;
			} catch (Exception e) {
				// log os dados da aplicação
			}
		}
	}

	private boolean comecouSemanaPassada(Leilao leilao) {
		return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}
		return diasNoIntervalo;
	}

	public int getTotalEncerrados() {
		return total;
	}

	public void envia(Leilao leilao) {
		
	}	
}
