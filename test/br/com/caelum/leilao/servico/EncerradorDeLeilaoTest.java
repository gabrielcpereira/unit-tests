package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {
		
	private EnviadorDeEmail carteiro;
	private RepositorioDeLeiloes daoFalso;
	private EncerradorDeLeilao encerrador;
	
	@Before
	public void inicializarTestes() {
		carteiro = mock(EnviadorDeEmail.class);
		daoFalso = mock(LeilaoDao.class);		
		encerrador = new EncerradorDeLeilao(this.daoFalso, this.carteiro);
	}
	
	@Test	
	public void testeEncerrarDoisLeiloes() {
		Leilao leilao1 = criarLeilao(-15);		
		Leilao leilao2 = criarLeilao(-15);
				
        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));        
        
        encerrador.encerra();

        InOrder order = inOrder(daoFalso, carteiro);               
        // leilao 1
        order.verify(daoFalso, times(1)).atualiza(leilao1);
        order.verify(carteiro, times(1)).envia(leilao1);
        assertTrue(leilao1.isEncerrado());
        // leilao 2
        order.verify(daoFalso, times(1)).atualiza(leilao2);
        order.verify(carteiro, times(1)).envia(leilao2);
        assertTrue(leilao2.isEncerrado());
        // check either all were closed
        assertEquals(2, this.encerrador.getTotalEncerrados());
	}

	
	@Test
	public void testeLeiloesQueIniciaramOntemNaoDevemSerEncerrados() {
		Leilao leilao1 = criarLeilao(-1);		
		Leilao leilao2 = criarLeilao(-1);

        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
                
        this.encerrador.encerra();

        assertTrue(!leilao1.isEncerrado());
        assertTrue(!leilao2.isEncerrado());
        assertEquals(0, this.encerrador.getTotalEncerrados());
	}
	
	@Test
	public void testeNaoExisteLeilaoParaSerEncerrado() {		
		when(this.daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		
		assertEquals(0, this.encerrador.getTotalEncerrados());
	}
	
	@Test
	public void testeMetodoVerifyMockito() {
		Leilao leilao1 = criarLeilao(-7);		
				
        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));       
        this.encerrador.encerra();
        
        verify(this.daoFalso, times(1)).atualiza(leilao1);
	}
	
	@Test
    public void testeNaoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
        Leilao leilao1 = criarLeilao(-1);
        Leilao leilao2 = criarLeilao(-1);

        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
        this.encerrador.encerra();

        assertEquals(0, this.encerrador.getTotalEncerrados());
        
        verify(this.daoFalso, never()).atualiza(leilao1);
        assertTrue(!leilao1.isEncerrado());
        
        verify(this.daoFalso, never()).atualiza(leilao2);
        assertTrue(!leilao2.isEncerrado());

    }

	@Test
	public void testeDoisLeiloesVencidosLeilaoUmErroAtualiza() {
		Leilao leilao1 = criarLeilao(-15);		
		Leilao leilao2 = criarLeilao(-15);

        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
                
        this.encerrador.encerra();

        assertTrue(!leilao1.isEncerrado());
        verify(carteiro, never()).envia(leilao1);
        
        assertTrue(leilao2.isEncerrado());
        assertEquals(1, this.encerrador.getTotalEncerrados());
	}
	
	@Test
	public void testeDoisLeiloesVencidosLeilaoUmErroEnvioEmail() {
		Leilao leilao1 = criarLeilao(-15);		
		Leilao leilao2 = criarLeilao(-15);

        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
        doThrow(new RuntimeException()).when(carteiro).envia(leilao1);
                
        this.encerrador.encerra();

        assertTrue(!leilao1.isEncerrado());       
        assertTrue(leilao2.isEncerrado());
        assertEquals(1, this.encerrador.getTotalEncerrados());
	}
	
	@Test
	public void testeDoisLeiloesVencidosDoisLeiloesErroAtualizacao() {
		Leilao leilao1 = criarLeilao(-15);		
		Leilao leilao2 = criarLeilao(-15);

        when(this.daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
        doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
                
        this.encerrador.encerra();
        // none call method to send email
        verify(carteiro, never()).envia(any(Leilao.class));
        // none object was your status updated to finished
        assertEquals(0, this.encerrador.getTotalEncerrados());
	}

	private Leilao criarLeilao(int quantidadeDias) {
		Calendar dataLeilao = Calendar.getInstance();
		dataLeilao.add(Calendar.DAY_OF_MONTH, quantidadeDias);
		
		Leilao leilao1 = new Leilao("Leilão 1", dataLeilao);
		return leilao1;
	}
}
