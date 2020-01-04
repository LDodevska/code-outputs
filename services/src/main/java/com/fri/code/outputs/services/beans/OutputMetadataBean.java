package com.fri.code.outputs.services.beans;

import com.fri.code.outputs.lib.*;
import com.fri.code.outputs.models.converters.OutputMetadataConverter;
import com.fri.code.outputs.models.entities.OutputMetadataEntity;
import com.fri.code.outputs.services.config.ClientMetadata;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class OutputMetadataBean {
    private Logger log = Logger.getLogger(OutputMetadataBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private ClientMetadata appConfig;

    @Inject
    @DiscoverService(value = "code-ide")
    private Optional<String> idePath;

    private Client httpClient;
    private String compilerApiUrl;

    @PostConstruct
    void init() {
        httpClient = ClientBuilder.newClient();
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

    public String getConfig() {
        return String.format("ID: %s\nKey: %s, enabled: %b", appConfig.getClientId(), appConfig.getClientSecret(), appConfig.isExternalServicesEnabled());
    }

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Fallback(fallbackMethod = "getCompilerOutputFallback")
    public CompilerOutput getCompilerOutput(InputMetadata inputMetadata) {
        CompilerOutput output = new CompilerOutput();
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
        if (appConfig.isExternalServicesEnabled()) {
            CompilerReadyInput input = new CompilerReadyInput();
            input.setClientId(appConfig.getClientId());
            input.setClientSecret(appConfig.getClientSecret());
            input.setLanguage("python3"); // treba da e setLanguage(getSubject(exerciseID).getLanguage())
            input.setScript(ideMetadata.getCode());
            if (!inputMetadata.getContent().isEmpty())
                input.setStdin(inputMetadata.getContent());
            input.setVersionIndex("2");
            try {
                log.severe(appConfig.getClientId());
                output = httpClient.target(compilerApiUrl).request().post(Entity.entity(input, MediaType.APPLICATION_JSON), CompilerOutput.class);
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }

        return output;
    }

    public CompilerOutput getCompilerOutputFallback(InputMetadata inputMetadata) {
        return new CompilerOutput();
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
