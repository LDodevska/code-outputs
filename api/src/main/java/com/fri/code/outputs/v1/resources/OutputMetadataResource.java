package com.fri.code.outputs.v1.resources;


import com.fri.code.outputs.lib.InputMetadata;
import com.fri.code.outputs.lib.OutputMetadata;
import com.fri.code.outputs.services.beans.OutputMetadataBean;
import com.fri.code.outputs.v1.dtos.ApiError;
import com.kumuluz.ee.logs.cdi.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@Log
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
    @Operation(summary = "Get details for output", description = "Returns details for output")
    @ApiResponses({
            @ApiResponse(description = "Output details", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    OutputMetadata.class))),
            @ApiResponse(description = "Output not found", responseCode = "404")
    })
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
    @Operation(summary = "Get output for input", description = "Returns output for specified input")
    @ApiResponses({
            @ApiResponse(description = "Output details", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    OutputMetadata.class))),
            @ApiResponse(description = "Output not found", responseCode = "404")
    })
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
    @Operation(summary = "Get all outputs", description = "Returns all details")
    @ApiResponses({
            @ApiResponse(description = "List of outputs", responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation =
                    OutputMetadata.class)))),
            @ApiResponse(description = "Output not found", responseCode = "404")
    })
    @Path("/all")
    @Timed
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
    @Operation(summary = "Create output", description = "Creates and returns the output")
    @ApiResponses({
            @ApiResponse(description = "New output", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    OutputMetadata.class))),
            @ApiResponse(description = "Can not create output", responseCode = "400")
    })
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
    @Operation(summary = "Get outputs for each input", description = "Returns compiled outputs for given inputs")
    @ApiResponses({
            @ApiResponse(description = "List of outputs", responseCode = "200", content = @Content(array = @ArraySchema( schema = @Schema(implementation =
                    OutputMetadata.class)))),
            @ApiResponse(description = "Internal server error", responseCode = "500")
    })
    @Path("results")
    public Response getResults(List<InputMetadata> inputs) {
        try {
            List<OutputMetadata> outputs = outputMetadataBean.getCompilerOutputsForExercise(inputs);
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
    @Operation(summary = "Delete output", description = "Deletes output")
    @ApiResponses({
            @ApiResponse(description = "Output deleted", responseCode = "204"),
            @ApiResponse(description = "Output not found", responseCode = "404")
    })
    @Path("/{outputID}")
    public Response deleteOutput(@PathParam("outputID") Integer outputID) {
        if (outputMetadataBean.deleteOutputMetadata(outputID))
            return Response.status(Response.Status.NO_CONTENT).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();


    }

    @DELETE
    @Operation(summary = "Delete all outputs", description = "Deletes all outputs")
    @ApiResponses({
            @ApiResponse(description = "Outputs deleted", responseCode = "204" ),
            @ApiResponse(description = "Output not found", responseCode = "404")
    })
    public Response deleteAllOutputs(){
        if(outputMetadataBean.deleteAllOutputs())
            return Response.status(Response.Status.NO_CONTENT).build();
        else return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("lodi-mafija")
    public Response getConfig() {
        return Response.ok(outputMetadataBean.getConfig()).build();
    }

}
