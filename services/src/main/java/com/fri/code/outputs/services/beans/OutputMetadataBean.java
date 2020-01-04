package com.fri.code.outputs.services.beans;

import com.fri.code.outputs.lib.*;
import com.fri.code.outputs.models.converters.OutputMetadataConverter;
import com.fri.code.outputs.models.entities.OutputMetadataEntity;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import org.json.JSONObject;

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
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
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
    @DiscoverService(value = "code-ide")
    private Optional<String> idePath;

    private Client httpClient;
    private String compilerApiUrl;
    private List<OutputMetadata> outputs;

    @PostConstruct
    void init() {
        httpClient = ClientBuilder.newClient();
//        baseURL = "http://localhost:8081";
        compilerApiUrl = "https://api.jdoodle.com/v1/execute";
    }

    public OutputMetadata getOutputById(Integer outputID) {
        TypedQuery<OutputMetadataEntity> query = em.createNamedQuery("OutputMetadataEntity.getOutputById", OutputMetadataEntity.class);
        return OutputMetadataConverter.toDTO(query.setParameter(1, outputID).getSingleResult());
    }

    public List<OutputMetadata> getAllOutputs() {
        TypedQuery<OutputMetadataEntity> query = em.createNamedQuery("OutputMetadataEntity.getAll", OutputMetadataEntity.class);
        return query.getResultList().stream().map(OutputMetadataConverter::toDTO).collect(Collectors.toList());
    }

    public OutputMetadata getOutputForInputID(Integer inputID) {

        TypedQuery<OutputMetadataEntity> query = em.createNamedQuery("OutputMetadataEntity.getOutputsForInput", OutputMetadataEntity.class);
        return OutputMetadataConverter.toDTO(query.setParameter(1, inputID).getSingleResult());
    }

    public boolean compareOutputs(OutputMetadata outputMetadata, CompilerOutput outputResult) {
        return outputResult.getOutput().trim().equals(outputMetadata.getCorrectOutput().trim());
    }

    public CompilerOutput getCompilerOutput(InputMetadata inputMetadata) {
        CompilerOutput output;
        IDEMetadata ideMetadata = new IDEMetadata();
        try {
            if (idePath.isPresent()) {
                String totalPath = String.format("%s/v1/script/%d", idePath.get(), inputMetadata.getExerciseID());
                ideMetadata = httpClient
                        .target(totalPath)
                        .request().get(new GenericType<IDEMetadata>() {
                        });
            }
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }
        CompilerReadyInput input = new CompilerReadyInput();
        input.setLanguage("python3"); // treba da e setLanguage(getSubject(exerciseID).getLanguage())
        input.setScript(ideMetadata.getCode());
        if (!inputMetadata.getContent().isEmpty())
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

    public List<OutputMetadata> getCompilerOutputsForExercise(List<InputMetadata> inputs) {
        List<OutputMetadata> outputs = new ArrayList<>();
        for (InputMetadata inp : inputs) {
            CompilerOutput output = getCompilerOutput(inp);
            OutputMetadata outputMetadata = getOutputForInputID(inp.getID());
            Boolean result = compareOutputs(outputMetadata, output);
            outputMetadata = updateSolved(outputMetadata, result, output.getOutput());
            outputs.add(outputMetadata);
        }
        return outputs;
    }

    public OutputMetadata updateSolved(OutputMetadata outputMetadata, Boolean solved, String output) {
        OutputMetadataEntity entity = OutputMetadataConverter.toEntity(outputMetadata);
        try {
            beginTx();
            entity.setUserOutput(output);
            entity.setSolved(solved);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
        return OutputMetadataConverter.toDTO(entity);
    }

    public OutputMetadata createOutputMetadata(OutputMetadata outputMetadata) {
        OutputMetadataEntity outputMetadataEntity = OutputMetadataConverter.toEntity(outputMetadata);

        try {
            beginTx();
            em.persist(outputMetadataEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (outputMetadataEntity.getID() == null) {
            throw new RuntimeException("The output was not saved");
        }

        return OutputMetadataConverter.toDTO(outputMetadataEntity);
    }


    public boolean deleteOutputMetadata(Integer outputID) {
        OutputMetadataEntity outputMetadataEntity = em.find(OutputMetadataEntity.class, outputID);
        if (outputMetadataEntity != null) {
            try {
                beginTx();
                em.remove(outputMetadataEntity);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    public boolean deleteAllOutputs() {
        TypedQuery<OutputMetadataEntity> query = em.createNamedQuery("OutputMetadataEntity.getAll", OutputMetadataEntity.class);
        List<OutputMetadataEntity> outputMetadataList = query.getResultList();
        if (outputMetadataList != null) {
            for (OutputMetadataEntity o : outputMetadataList) {
                try {
                    beginTx();
                    em.remove(o);
                    commitTx();
                } catch (Exception e) {
                    rollbackTx();
                }
            }
        } else
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
