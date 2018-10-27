import helpers.LogDataHelpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.aber.dcs.aberfitness.glados.api.LogApi;
import uk.ac.aber.dcs.aberfitness.glados.db.*;

import javax.json.JsonArray;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class LogApiTest {
    @Mock
    private DatabaseConnection dbMock;

    // Ensure we replace the injected concrete type with the mock db connection
    @InjectMocks
    private LogApi apiInstance;

    @Before
    public void setUp() {
        apiInstance = new LogApi();
        MockitoAnnotations.initMocks(this);
    }

    private LogData createExampleLogData() {
        return new LogDataNoSerial(Instant.now(), LoggingLevels.DEBUG, "TestContent",
                "abc123", ServiceNames.GLADOS);
    }

    @Test
    public void findAllCallReturnsAll() throws IOException {
        LogData dummyLogOne = createExampleLogData();
        LogData dummyLogTwo = createExampleLogData();

        List<LogData> mockedData = Arrays.asList(dummyLogOne, dummyLogTwo);
        when(dbMock.getAllLogEntries()).thenReturn(mockedData);

        // This should return JSON
        JsonArray returnedJson = apiInstance.getAllEntries();
        List<LogDataJson> returnedEntries = LogDataJson.fromJson(returnedJson);

        Assert.assertEquals(returnedEntries.size(), mockedData.size());

        for (int i = 0; i < returnedEntries.size(); i++) {
            LogDataHelpers.isAlmostEqual(returnedEntries.get(i), mockedData.get(i));
        }
    }




}
