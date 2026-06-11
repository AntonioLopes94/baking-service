package br.com.antonio.service;

import br.com.antonio.domain.AgenciaHttp;
import br.com.antonio.domain.SituacaoCadastral;
import br.com.antonio.domain.http.Agencia;
import br.com.antonio.domain.http.Endereco;
import br.com.antonio.exceptions.AgenciaNaoAtivaouNaoEncontradaException;
import br.com.antonio.repository.AgenciaRepository;
import br.com.antonio.service.http.SituacaoCadastralHttpService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class AgenciaServiceTest {

    @InjectMock
    private AgenciaRepository agenciaRepository;

    @InjectMock
    @RestClient
    private SituacaoCadastralHttpService situacaoCadastralHttpService;

    @Inject
    private AgenciaService agenciaService;

    @Test
    public void deveNaoCadastrarQuandoClientRetornarNull(){
        Agencia agencia = criarAgencia();
        Mockito.when(situacaoCadastralHttpService.buscarPorCnpj("123")).thenReturn(null);

        Assertions.assertThrows(AgenciaNaoAtivaouNaoEncontradaException.class,
                () -> agenciaService.cadastrar(agencia));

        Mockito.verify(agenciaRepository, Mockito.never()).persist(agencia);
    }

    @Test
    public void deveCadastrarQuandoClientRetornarSituacaoCadastralAtiva(){
        Agencia agencia = criarAgencia();

        Mockito.when(situacaoCadastralHttpService.buscarPorCnpj("123")).thenReturn(criarAgenciaHttp());

        agenciaService.cadastrar(agencia);

        Mockito.verify(agenciaRepository).persist(agencia);


    }


    private Agencia criarAgencia(){
        Endereco endereco = new Endereco(1,"Teste","Teste","Teste",1);
        return new Agencia(1L, "Agencia Teste","Razão Teste","123", endereco);
    }

    private AgenciaHttp criarAgenciaHttp(){
        return new AgenciaHttp("Agencia Teste","Razão Teste","123", SituacaoCadastral.ATIVA );
    }
}
