package com.fri.code.outputs.v1.resources;


import com.fri.code.outputs.lib.CompilerOutput;
import com.fri.code.outputs.lib.InputMetadata;
import com.fri.code.outputs.lib.OutputMetadata;
import com.fri.code.outputs.services.beans.OutputMetadataBean;
import com.fri.code.outputs.v1.dtos.ApiError;
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
    @Path("/{outputID}")
    public Response getOutputById(@PathParam("outputID") Integer outputID) {
        try {
            OutputMetadata output = outputMetadataBean.getOutputById(outputID);
            return Response.ok(output).build();
        } catch (Exception e) {
            ApiError apiError = new ApiError();
            apiError.setCode(Response.Status.NOT_FOUND.toString());
            apiError.setMessage(e.getMessage());
            apiError.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return Response.status(Response.Status.NOT_FOUND).entity(apiError).build();
        }
    }

    @GET
    public Response getOutputForInputID(@QueryParam("inputID") Integer inputID) {
        try {
            OutputMetadata output = outputMetadataBean.getOutputForInputID(inputID);
            return Response.ok(output).build();
        } catch (Exception e) {
            ApiError apiError = new ApiError();
            apiError.setCode(Response.Status.NOT_FOUND.toString());
            apiError.setMessage(e.getMessage());
            apiError.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return Response.status(Response.Status.NOT_FOUND).entity(apiError).build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllOutputs() {
        try {
            List<OutputMetadata> outputs = outputMetadataBean.getAllOutputs();
            return Response.ok(outputs).build();
        } catch (Exception e) {
            ApiError apiError = new ApiError();
            apiError.setCode(Response.Status.NOT_FOUND.toString());
            apiError.setMessage(e.getMessage());
            apiError.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return Response.status(Response.Status.NOT_FOUND).entity(apiError).build();
        }
    }

    @POST
    public Response createOutput(OutputMetadata outputMetadata) {

        if (outputMetadata.getInputID() == null || outputMetadata.getCorrectOutput() == null || outputMetadata.getUserOutput() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            try {
                outputMetadata = outputMetadataBean.createOutputMetadata(outputMetadata);
                return Response.status(STATUS_OK).entity(outputMetadata).build();
            } catch (Exception e) {
                ApiError apiError = new ApiError();
                apiError.setCode(Response.Status.BAD_REQUEST.toString());
                apiError.setMessage(e.getMessage());
                apiError.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                return Response.status(Response.Status.BAD_REQUEST).entity(apiError).build();
            }
        }

    }

    //For the given inputs, return the compiled outputs and compare them with the correct ones.
    @POST
    @Path("results")
    public Response getResults(List<InputMetadata> inputs) {
        try {
            Map<Integer, Boolean> outputs = outputMetadataBean.getCompilerOutputsForExercise(inputs);
            return Response.status(Response.Status.OK).entity(outputs).build();
        } catch (Exception e) {
            ApiError apiError = new ApiError();
            apiError.setCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            apiError.setMessage(e.getMessage());
            apiError.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(apiError).build();
        }
    }

    @DELETE
    @Path("/{outputID}")
    public Response deleteOutput(@PathParam("outputID") Integer outputID) {
        if (outputMetadataBean.deleteOutputMetadata(outputID))
            return Response.status(Response.Status.NO_CONTENT).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();


    }

//    @DELETE
//    public Response deleteAllOutputs(){
//        if(outputMetadataBean.deleteAllOutputs())
//            return Response.status(Response.Status.NO_CONTENT).build();
//        else return Response.status(Response.Status.NOT_FOUND).build();
//    }


}