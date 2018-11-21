package beans;

import com.google.gson.JsonParseException;
import entities.AuditData;
import entities.AuditDataJson;

import persistence.DatabaseConnection;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Dependent
@Stateless
@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditApiBean {
    // Instant MIN
    private static final String DEFAULT_EMPTY_TIME = "-1000000000-01-01T00:00:00Z";

    @Inject
    DatabaseConnection dbConnection;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditById(@QueryParam("logId") String logId) throws IOException {
        AuditData foundEntry = dbConnection.getLogEntry(logId);

        if (foundEntry == null){

            return Response.status(404, "Not Found").build();
        }

        JsonObject json = new AuditDataJson(foundEntry).toJson();
        return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEntries(@QueryParam("from") int from,
                                  @QueryParam("to") int to,
                                  @QueryParam("orderBy") String orderBy) throws IOException {
        // TODO limit returned entries

        List<AuditData> foundEntries = dbConnection.getAllLogEntries();

        // Ensure we have JSON serialisable elements
        return CreateJsonResponseFromList(foundEntries);
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLogEntry(
            @QueryParam("userId") String userId,
            @DefaultValue(DEFAULT_EMPTY_TIME) @QueryParam("fromTime") String fromTime,
            @DefaultValue(DEFAULT_EMPTY_TIME) @QueryParam("toTime") String toTime) throws IOException {


        // We need to take the current time for searching if not provided
        if (toTime.equals(DEFAULT_EMPTY_TIME)){
            toTime = Instant.now().toString();
        }

        List<AuditData> foundEntries = dbConnection.findLogEntry(userId, fromTime, toTime);

        if (foundEntries.isEmpty()){
            return Response.status(404).build();
        }

        return CreateJsonResponseFromList(foundEntries);
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNewEntry(JsonObject httpBody) throws IOException {
        boolean failedToParse = false;
        AuditDataJson newEntity = null;
        try {
            newEntity = AuditDataJson.fromJson(httpBody);
        } catch (JsonParseException e) {
            failedToParse = true;
        }

        if (failedToParse || !newEntity.isValid()){
            return Response.status(400).build();
        }

        dbConnection.addLogData(newEntity);
        return Response.noContent().build();
    }

    private Response CreateJsonResponseFromList(List<AuditData> foundEntries) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        foundEntries.forEach(e->jsonArray.add(new AuditDataJson(e).toJson()));
        JsonArray json = jsonArray.build();
        return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
    }


}