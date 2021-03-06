package UnitTest;

import entities.AuditData;
import entities.AuditDataJson;
import com.google.gson.JsonParseException;
import helpers.AuditDataHelpers;
import org.junit.Assert;
import org.junit.Test;
import beans.helpers.ServiceNames;

import javax.json.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuditDataJsonTest {
    private JsonObject createFakeJson(AuditData auditData) {

        JsonObjectBuilder newJsonObject = Json.createObjectBuilder();


        newJsonObject.add("timestamp", auditData.getTimestamp())
                .add("userId", auditData.getUserId())
                .add("content", auditData.getContent())
                .add("serviceName", auditData.getServiceName());
        return newJsonObject.build();
    }

    @Test
    public void ConvertsFromOtherSerialisingTypeCorrectly() {
        AuditData noSerial = new AuditData(Instant.now().toEpochMilli(),
                "test", "abc123", ServiceNames.GLADOS.toString());

        AuditDataJson convertedInstance = new AuditDataJson(noSerial);
        Assert.assertNotNull(convertedInstance);

        // These are equiv if converted correctly
        Assert.assertEquals(noSerial.getLogId(), convertedInstance.getLogId());
    }

    @Test
    public void SerialisesToJsonCorrectly() {
        Instant now = Instant.now();
        String sampleMsg = "Hello world";
        String userId = "test123";
        String serviceName = ServiceNames.GLADOS.toString();

        final AuditDataJson testInstance = new AuditDataJson(now.toEpochMilli(), sampleMsg, userId, serviceName);
        JsonObject returnedJson = testInstance.toJson();

        Assert.assertEquals(returnedJson.getJsonNumber("timestamp").longValue(), now.toEpochMilli());
        Assert.assertEquals(returnedJson.getString("userId"), userId);

        Assert.assertEquals(returnedJson.getString("content"), sampleMsg);
        Assert.assertEquals(returnedJson.getString("serviceName"), serviceName);
    }

    @Test
    public void SerialisesFromJsonCorrectly() {
        Instant now = Instant.now();
        String sampleMsg = "Hello world";

        String userId = "test123";
        String serviceName = ServiceNames.GLADOS.toString();

        AuditData referenceInstance = new AuditData(now.toEpochMilli(), sampleMsg, userId, serviceName);

        JsonObject testJson = createFakeJson(referenceInstance);

        AuditDataJson returnedClass = AuditDataJson.fromJson(testJson);
        AuditDataHelpers.isAlmostEqual(referenceInstance, returnedClass);
    }

    @Test
    public void SerialisesFromListCorrectly() {
        Instant now = Instant.now();
        String sampleMsg = "Hello world";
        String serviceName = ServiceNames.GLADOS.toString();

        final AuditDataJson testObjOne = new AuditDataJson(now.toEpochMilli(), sampleMsg, "101", serviceName);
        final AuditDataJson testObjTwo = new AuditDataJson(now.toEpochMilli(), sampleMsg, "102", serviceName);
        final AuditDataJson testObjThree = new AuditDataJson(now.toEpochMilli(), sampleMsg, "103", serviceName);

        JsonObject testJson = createFakeJson(testObjOne);
        JsonObject testJson2 = createFakeJson(testObjTwo);
        JsonObject testJson3 = createFakeJson(testObjThree);


        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        jsonArrayBuilder.add(testJson).add(testJson2).add(testJson3);
        JsonArray returnedArray = jsonArrayBuilder.build();

        List<AuditDataJson> processedArray = AuditDataJson.fromJson(returnedArray);

        Assert.assertEquals(processedArray.size(), 3);
        AuditDataHelpers.isAlmostEqual(testObjOne, processedArray.get(0));
        AuditDataHelpers.isAlmostEqual(testObjTwo, processedArray.get(1));
        AuditDataHelpers.isAlmostEqual(testObjThree, processedArray.get(2));
    }

    @Test
    public void SerialisesSelfCorrectly() {
        Instant now = Instant.now();
        String sampleMsg = "Hello world";
        String userId = "test123";
        String serviceName = ServiceNames.GLADOS.toString();

        final AuditDataJson testInstance = new AuditDataJson(now.toEpochMilli(), sampleMsg, userId, serviceName);

        JsonObject returnedJson = testInstance.toJson();
        AuditDataJson returnedObject = AuditDataJson.fromJson(returnedJson);

        Assert.assertTrue(returnedObject.isValid());
        AuditDataHelpers.isAlmostEqual(testInstance, returnedObject);
    }


    @Test
    public void JsonParseExceptionIsThrownForBlank() {
        JsonObject blankObj = Json.createObjectBuilder().build();
        JsonArray blankArray = Json.createArrayBuilder().build();

        assertThrows(JsonParseException.class, () -> {
            AuditDataJson.fromJson(blankObj);
        });
        assertThrows(JsonParseException.class, () -> {
            AuditDataJson.fromJson(blankArray);
        });

    }

    @Test
    public void JsonParseExceptionIsThrownForPartial() {
        JsonObjectBuilder partialObj = Json.createObjectBuilder();
        partialObj.add("logId", "123");
        partialObj.add("timestamp", Instant.now().toEpochMilli());

        JsonObject partialJson = partialObj.build();

        assertThrows(JsonParseException.class, () -> {
            AuditDataJson.fromJson(partialJson);
        });
    }
}
