package testing;

import javafx.application.Platform;
import org.junit.jupiter.api.*;
import sample.*;
import sample.MyButton;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class RPSLSClientTest {
    //private static AIPlayer ai;
    private static NetworkConnection conn;
    private static ArrayList<String> testStringsContainer;

    // before to initiate the various data for every function
    @BeforeEach
    public void initiate(){
        conn = new Client("127.0.0.1", 5555, data -> {
            Platform.runLater(()->{
                testStringsContainer.add((String) data);
            });
        });
        testStringsContainer = new ArrayList<String>();
    }

    //after method to just delete the data...this way we know no data is getting mixed up
    @AfterAll
    public static void runAfterTestMethod(){
        conn = null;
        testStringsContainer = null;
    }

    @Test
    public void clientConstructorTest(){
        assertNotNull(conn.getCallback(),"callback is null after client constructor call");
    }

    @Test
    public void startConnTest() {
        int x = 1;
        try {
            conn.startConn();
        } catch (Exception e) {
            assertNull(x,"error upon trying to startConn()...caught in try/catch");
            e.printStackTrace();
        }
    }

    @Test
    public void closeConnTest() {
        try {
            conn.startConn();
            conn.closeConn();
        } catch (Exception e) {
            assertTrue(false,"error upon trying to closeConn()...caught in try/catch");
            e.printStackTrace();
        }
    }

    @Test
    public void getIPTest() {
        Client c = new Client("127.0.0.1", 5555, data -> {
            Platform.runLater(()->{
                testStringsContainer.add((String) data);
            });
        });
        assertEquals("127.0.0.1",c.returnIP(),"client ip is not being created correctly/not being returned correctly");
    }

    @Test
    public void getPortTest() {
        Client c = new Client("0", 5555, data -> {
            Platform.runLater(()->{
                testStringsContainer.add((String) data);
            });
        });
        assertEquals(5555,c.returnPort(),"client port is not being created correctly/not being returned correctly");
    }
}

