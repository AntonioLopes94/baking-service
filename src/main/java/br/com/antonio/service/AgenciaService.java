package br.com.antonio.service;

import br.com.antonio.domain.AgenciaHttp;
import br.com.antonio.domain.SituacaoCadastral;
import br.com.antonio.domain.http.Agencia;
import br.com.antonio.exceptions.AgenciaNaoAtivaouNaoEncontradaException;
import br.com.antonio.repository.AgenciaRepository;
import br.com.antonio.service.http.SituacaoCadastralHttpService;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    private SituacaoCadastralHttpService situacaoCadastralHttpService;

    private final AgenciaRepository agenciaRepository;
    private final MeterRegistry meterRegistry;

    AgenciaService(AgenciaRepository agenciaRepository,
                   MeterRegistry meterRegistry1) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry1;
    }

    public void cadastrar(Agencia agencia) {
        AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        if(agenciaHttp != null &&
                agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVA)){
            Log.info("A agencia com o CNPJ " + agencia.getCnpj() + " foi cadastrado");
            meterRegistry.counter("agencia_adicionada_counter").increment();
            agenciaRepository.persist(agencia);
        }else {
            Log.info("A agencia com o CNPJ " + agencia.getCnpj() + " não foi cadastrado");
            meterRegistry.counter("agencia_nao_adicionada_counter").increment();
            throw new AgenciaNaoAtivaouNaoEncontradaException();
        }
    }

    public Agencia buscarPorId(Long id){
        return agenciaRepository.findById(id);
    }

    public void deletar(Long id){
        Log.info("A agencia com o Id " + id + " foi deletada");
        agenciaRepository.deleteById(id);
    }

    public void alterar(Agencia agencia){
        Agencia entidadeExistente = agenciaRepository.findById(agencia.getId());
        if (entidadeExistente != null) {
            Log.info("A agencia com o CNPJ " + agencia.getCnpj() + " foi alterada");
            entidadeExistente.setNome(agencia.getNome());
            entidadeExistente.setRazaoSocial(agencia.getRazaoSocial());
            entidadeExistente.setCnpj(agencia.getCnpj());
        } else {
            throw new IllegalStateException("Agência com ID " + agencia.getId() + " não encontrada");
        }
    }
}