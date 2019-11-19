package com.fri.code.outputs.services.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class PersistenceProducer {
    //se povrzuva mikroservisot so bazata, za da nemas queries do baza, tuku preku EntityManager

    @PersistenceUnit(unitName = "output-jpa")
    private EntityManagerFactory emf;

    @Produces
    @ApplicationScoped
    public EntityManager getEntityManager(){
        return emf.createEntityManager();
    }

    public void disposeEntityManager(@Disposes EntityManager entityManager){
        if (entityManager.isOpen()){
            entityManager.close();
        }
    }
}
