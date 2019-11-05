package com.fri.code.outputs.services.beans;

import com.fri.code.outputs.lib.OutputMetadata;
import com.fri.code.outputs.models.converters.OutputMetadataConverter;
import com.fri.code.outputs.models.entities.OutputMetadataEntity;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class OutputMetadataBean {
    private Logger log = Logger.getLogger(OutputMetadataBean.class.getName());

    @Inject
    private EntityManager em;

    private List<OutputMetadata> outputs;

    @PostConstruct
    void init(){

        outputs = new ArrayList<OutputMetadata>();
    }

    public OutputMetadata getOutputForInputID(Integer inputID){
        return outputs.stream().filter(output -> output.getInputID().equals(inputID)).collect(Collectors.toList()).get(0);
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
