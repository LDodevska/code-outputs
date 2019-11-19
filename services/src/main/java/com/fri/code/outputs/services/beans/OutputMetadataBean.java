package com.fri.code.outputs.services.beans;

import com.fri.code.outputs.lib.CompilerOutput;
import com.fri.code.outputs.lib.CompilerReadyInput;
import com.fri.code.outputs.lib.InputMetadata;
import com.fri.code.outputs.lib.OutputMetadata;
import com.fri.code.outputs.models.converters.OutputMetadataConverter;
import com.fri.code.outputs.models.entities.OutputMetadataEntity;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import com.kumuluz.ee.discovery.annotations.DiscoverService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class OutputMetadataBean {
    private Logger log = Logger.getLogger(OutputMetadataBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    @DiscoverService(value = "code-inputs")
    private Optional<String> basePath;

    private Client httpClient;
    private String compilerApiUrl;
    private List<OutputMetadata> outputs;

    @PostConstruct
    void init(){
        httpClient = ClientBuilder.newClient();
//        baseURL = "http://localhost:8081";
        compilerApiUrl = "https://api.jdoodle.com/v1/execute";
    }

    public OutputMetadata getOutputForInputID(Integer inputID){
        TypedQuery<OutputMetadataEntity> query = em.createNamedQuery("OutputMetadataEntity.getOutputsForInput", OutputMetadataEntity.class);
        return OutputMetadataConverter.toDTO(query.setParameter(1, inputID).getSingleResult());
    }

    public boolean compareOutputs(OutputMetadata outputMetadata, CompilerOutput outputResult){
        return outputResult.getOutput().trim().equals(outputMetadata.getCorrectOutput().trim());
    }

    public CompilerOutput getCompilerOutput(InputMetadata inputMetadata){
        CompilerOutput output = new CompilerOutput();
        String script = "x=input()\nprint(x)"; // treba da e getScript(exerciseID, currentUserID);
        CompilerReadyInput input = new CompilerReadyInput();
        input.setLanguage("python3"); // treba da e setLanguage(getSubject(exerciseID).getLanguage())
        input.setScript(script);
        if(!inputMetadata.getContent().isEmpty())
            input.setStdin(inputMetadata.getContent());
        input.setVersionIndex("2");
        try {
            output = httpClient.target(compilerApiUrl).request().post(Entity.entity(input, MediaType.APPLICATION_JSON), CompilerOutput.class);
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

        return output;
    }

    public Map<Integer, Boolean> getCompilerOutputsForExercise(List<InputMetadata> inputs){
        Map<Integer, Boolean> outputs = new HashMap();

        for(InputMetadata inp: inputs){
            CompilerOutput output = getCompilerOutput(inp);
            OutputMetadata outputMetadata = getOutputForInputID(inp.getID());
            outputs.put(outputMetadata.getID(), compareOutputs(outputMetadata, output));
        }

        return outputs;
    }

    public OutputMetadata createOutputMetadata(OutputMetadata outputMetadata){
        OutputMetadataEntity outputMetadataEntity = OutputMetadataConverter.toEntity(outputMetadata);

        try {
            beginTx();
            em.persist(outputMetadataEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if(outputMetadataEntity.getID() == null){
            throw new RuntimeException("The output was not saved");
        }

        return OutputMetadataConverter.toDTO(outputMetadataEntity);
    }


    public boolean deleteOutputMetadata(Integer outputID){
        OutputMetadataEntity outputMetadataEntity = em.find(OutputMetadataEntity.class, outputID);
        if (outputMetadataEntity != null) {
            try {
                beginTx();
                em.remove(outputMetadataEntity);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        }
        else
            return false;

        return true;
    }

    public boolean deleteAllOutputs(){
        TypedQuery<OutputMetadataEntity> query = em.createNamedQuery("OutputMetadataEntity.getAll", OutputMetadataEntity.class);
        List<OutputMetadataEntity> outputMetadataList = query.getResultList();
        if (outputMetadataList != null){
            for(OutputMetadataEntity o : outputMetadataList){
                try {
                    beginTx();
                    em.remove(o);
                    commitTx();
                } catch (Exception e) {
                    rollbackTx();
                }
            }
        }
        else
            return false;

        return true;

    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }

}
