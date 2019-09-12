package testing;

import javafx.application.Platform;
import org.junit.jupiter.api.*;
import sample.*;
import sample.Server;
import sample.NetworkConnection.ConnThread;
import sun.nio.ch.Net;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class RPSLSServerTest {
    private static NetworkConnection conn;
    private static HashMap<String, HashMap<String,String>> rulebook;
    private static ArrayList<String> testStringsContainer;

    private static HashMap<String,String> rockMap;
    private static HashMap<String,String> paperMap;
    private static HashMap<String,String> scissorsMap;
    private static HashMap<String,String> lizardMap;
    private static HashMap<String,String> spockMap;

    // before to initiate the various data for every function
    @BeforeEach
    public void initiate(){
        conn = new Server(5555, data-> {
            Platform.runLater(()->{
                testStringsContainer.add((String)data);
            });
        });
        rulebook = new HashMap<String,HashMap<String,String>>();
        rockMap = new HashMap<String,String>();
        paperMap = new HashMap<String,String>();
        scissorsMap = new HashMap<String,String>();
        lizardMap = new HashMap<String,String>();
        spockMap = new HashMap<String,String>();
        rockMap.put("paper","paper");
        rockMap.put("scissors","rock");
        rockMap.put("lizard","rock");
        rockMap.put("spock","spock");
        paperMap.put("rock","paper");
        paperMap.put("scissors","scissors");
        paperMap.put("lizard","lizard");
        paperMap.put("spock","paper");
        scissorsMap.put("rock","rock");
        scissorsMap.put("paper","scissors");
        scissorsMap.put("lizard","scissors");
        scissorsMap.put("spock","spock");
        lizardMap.put("rock","rock");
        lizardMap.put("paper","lizard");
        lizardMap.put("scissors","scissors");
        lizardMap.put("spock","lizard");
        spockMap.put("rock","spock");
        spockMap.put("paper","paper");
        spockMap.put("scissors","spock");
        spockMap.put("lizard","lizard");
        rulebook.put("rock", rockMap);
        rulebook.put("paper", paperMap);
        rulebook.put("scissors", scissorsMap);
        rulebook.put("lizard", lizardMap);
        rulebook.put("spock", spockMap);
    }

    //after method to just delete the data...this way we know no data is getting mixed up
    @AfterAll
    public static void runAfterTestMethod(){
        conn = null;
        rulebook = null;
        rockMap = null;
        paperMap = null;
        scissorsMap = null;
        lizardMap = null;
        spockMap = null;
        testStringsContainer = null;
    }

    @Test
    public void ServerConstructorTest(){
        Server s = new Server(5555, data-> {
            Platform.runLater(()->{
                testStringsContainer.add((String)data);
                //messages.appendText(data.toString() + "\n");
            });
        });

        assertEquals(5555,s.returnPort(),"server port does not match after constructor call");
        assertNotNull(s,"server is null after constructor call");
    }

    @Test
    public void checkWinnerTest() {
        String rock = "rock";
        String paper = "paper";

        String winner = NetworkConnection.decideWhoWins(rock,paper,rulebook);

        assertEquals("paper",winner,"decideWhoWins() is not correctly deciding winner");

    }

    @Test
    public void RPSLSPlayedTest() {
        String opPlayed = "Opponent Played rock";

        assertTrue(NetworkConnection.RPSLSPlayed(opPlayed),"RPSLSPlayed did not recognize 'Opponent Played' string in input");
    }

    @Test
    public void whatWasPlayedWasSent() {
        String s = "spock";
        assertFalse(NetworkConnection.whatWasPlayedWasSent("test"),"whatWasPlayed() incorrectly verified message as playable string");
        assertTrue(NetworkConnection.whatWasPlayedWasSent(s),"whatWasPlayed() incorrectly verified message as playable string");
    }

    @Test
    public void connClientThreadTest() {
        assertTrue(conn.getClientThreads().size() == 0,"after creating networkconnection the client threads ArrayList does not have 0 size");
    }

    @Test
    public void fillInRulebookTest(){
        conn.fillInRulebook();
        assertTrue(conn.getRuleBook().containsKey("rock"),"fillInRulebook() did not correctly init the rulebook hashmap");
    }

    @Test
    public void startConnTest() {
        try {
            conn.startConn();
        } catch (Exception e) {
            assertTrue(false,"error upon trying to startConn()...caught in try/catch");
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
}

