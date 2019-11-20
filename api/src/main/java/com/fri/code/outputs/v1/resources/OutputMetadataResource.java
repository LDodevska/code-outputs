package com.fri.code.outputs.v1.resources;


import com.fri.code.outputs.lib.CompilerOutput;
import com.fri.code.outputs.lib.InputMetadata;
import com.fri.code.outputs.lib.OutputMetadata;
import com.fri.code.outputs.services.beans.OutputMetadataBean;
import javassist.tools.reflect.Compiler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/outputs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OutputMetadataResource {

    private static Response.Status STATUS_OK = Response.Status.OK;

    @Inject
    private OutputMetadataBean outputMetadataBean;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getOutputForInputID(@QueryParam("inputID") Integer inputID){
        try {
            OutputMetadata output = outputMetadataBean.getOutputForInputID(inputID);
            return Response.ok(output).build();
        }
        catch (Exception e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllOutputs(){
        try
        {
            List<OutputMetadata> outputs = outputMetadataBean.getAllOutputs();
            return Response.ok(outputs).build();
        }
        catch (Exception e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

    }

    @POST
    public Response createOutput(OutputMetadata outputMetadata){
        if (outputMetadata.getInputID() == null || outputMetadata.getCorrectOutput() == null || outputMetadata.getUserOutput() == null){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else{
            outputMetadata = outputMetadataBean.createOutputMetadata(outputMetadata);
            return Response.status(STATUS_OK).entity(outputMetadata).build();
        }
    }

    @POST
    @Path("results")
    public Response getResults(List<InputMetadata> inputs){
        Map<Integer, Boolean> outputs = outputMetadataBean.getCompilerOutputsForExercise(inputs);
        return Response.status(Response.Status.OK).entity(outputs).build();
    }


//    @DELETE
//    public Response deleteAllOutputs(){
//        if(outputMetadataBean.deleteAllOutputs())
//            return Response.status(Response.Status.NO_CONTENT).build();
//        else return Response.status(Response.Status.NOT_FOUND).build();
//    }


}
