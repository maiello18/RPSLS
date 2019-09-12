package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class NetworkConnection {

    private ConnThread connthread = new ConnThread();
    private Consumer<Serializable> callback;
    private ArrayList<ClientThread> clientThreads = new ArrayList<ClientThread>();
    private HashMap<String,HashMap<String,String>> rulebook = new HashMap<>();

    private HashMap<String,String> rockMap = new HashMap<>();
    private HashMap<String,String> paperMap = new HashMap<>();
    private HashMap<String,String> scissorsMap = new HashMap<>();
    private HashMap<String,String> lizardMap = new HashMap<>();
    private HashMap<String,String> spockMap = new HashMap<>();

    public HashMap<String,HashMap<String,String>> getRuleBook(){
        return rulebook;
    }

    public ArrayList<ClientThread> getClientThreads() {
        return clientThreads;
    }

    public NetworkConnection(Consumer<Serializable> callback) {
        this.callback = callback;
        connthread.setDaemon(true);
    }

    public void startConn() throws Exception{
        connthread.start();
    }

    public void send(Serializable data) throws Exception{
        for(ClientThread c : clientThreads){
            c.out.writeObject(data);
        }
    }

    public void closeConn() throws Exception{
        for(ClientThread c : clientThreads){
            c.socket.close();
        }
        if(connthread.socket != null){
            connthread.socket.close();
        }
        //connthread.socket.close();
    }

    abstract protected boolean isServer();
    abstract protected String getIP();
    abstract protected int getPort();

    public class ConnThread extends Thread{
        private Socket socket;
        private ObjectOutputStream out;

        public void run() {
            try(ServerSocket server = new ServerSocket(getPort())){
                while(true) {
                    ClientThread t1 = new ClientThread(server.accept());
                    clientThreads.add(t1);
                    callback.accept("Added Client...");
                    Main.getHowManyPlayersTA().setText("# Clients: " + clientThreads.size());
                    t1.start();
                }

            }
            catch(Exception e) {
                callback.accept("connection Closed");
            }
        }
    }

    public class ClientThread extends Thread{
        private Socket socket;
        private ObjectOutputStream out;
        private int clientScore;
        private String whatClientPlayed;
        private boolean clientHasMadeMove;

        ClientThread(Socket s){
            this.socket = s;
            this.clientScore = 0;
            this.clientHasMadeMove = false;
            this.whatClientPlayed = "";
        }

        public void run() {

            try(//ServerSocket server = new ServerSocket(getPort());
                //Socket socket = isServer() ? server.accept() : new Socket(getIP(), getPort());
                ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

                //this.socket = socket;
                this.out = out;
                socket.setTcpNoDelay(true);

                //this is where we can say "hey you are the only one in the lobby"
                if(clientThreads.size() == 1){
                    for(ClientThread c : clientThreads){
                        c.out.writeObject("Server:\n You're the only one in the lobby...");
                        c.out.writeObject("Server:\n Waiting for more players to connect...");
                    }
                }

                //this is also where we can say "hey theres two people, the game is starting"
                if(clientThreads.size() == 2){
                    for(ClientThread c : clientThreads){
                        c.out.writeObject("Server:\n Two players in lobby...");
                        c.out.writeObject("Server:\n Game starting now...");
                    }
                    ClientThread firstClient = clientThreads.get(0);
                    firstClient.out.writeObject("Server:\n Player 1 Make Your Move...");
                }

                while(true) {
                    //this only happens when user sends something through text message box
                    Serializable data = (Serializable) in.readObject();

                    if(((String) data).equals("YesPlayAgain") && clientThreads.size() != 0){
                        if(this.socket == clientThreads.get(0).socket){
                            if(Main.getwhoIsPlayingAgainTextArea().getText().contains("P2")){
                                String t = Main.getwhoIsPlayingAgainTextArea().getText();
                                Main.getwhoIsPlayingAgainTextArea().setText(t + "\n P1 is");
                            }
                            else{
                                Main.getwhoIsPlayingAgainTextArea().setText("Who's Playing Again\n P1 is");
                            }
                        }
                        else{
                            if(Main.getwhoIsPlayingAgainTextArea().getText().contains("P1")){
                                String t = Main.getwhoIsPlayingAgainTextArea().getText();
                                Main.getwhoIsPlayingAgainTextArea().setText(t + "\n P2 is");
                            }
                            else{
                                Main.getwhoIsPlayingAgainTextArea().setText("Who's Playing Again\n P2 is");
                            }
                        }
                        //if true start a new game
                        if(Main.getwhoIsPlayingAgainTextArea().getText().contains("P1") && Main.getwhoIsPlayingAgainTextArea().getText().contains("P2")){
                            for(ClientThread a : clientThreads){
                                a.whatClientPlayed = "";
                                a.clientHasMadeMove = false;
                                a.clientScore = 0;
                            }
                            Main.getscoreboardTA().setText(sendNewScoreBoard());
                            send("clear screen");
                            clientThreads.get(0).out.writeObject("Server:\n New Game Starting\n Player 1 Make First Move");
                            clientThreads.get(1).out.writeObject("Server:\n New Game Starting\n Player 1 Is Making First Move");
                        }
                    }

                    //here we actually know the button that was pressed
                    if(whatWasPlayedWasSent((String)data)){
                        this.whatClientPlayed = parseDataForWhatClientPlayed((String)data);
                        //check if both client made a move and decide who wins
                        if(bothClientsMadeAMove()){
                            Main.getwhatThePlayersPlayedTA().setText("What The Players Played\n Client #1: " + clientThreads.get(0).whatClientPlayed + "\n Client #2: " + clientThreads.get(1).whatClientPlayed);
                            ClientThread winningThread = decideWhoWins();
                            ClientThread gameWinner = checkForGameWinner();

                            if(gameWinner != null){
                                //we have a winner in game winner var
                                send("Server:\n There is a winner...");
                                for(ClientThread tc : clientThreads){
                                    if(tc == gameWinner){
                                        if(gameWinner == clientThreads.get(0)){
                                            Main.getdoWeHaveAWinnerTA().setText("Winner? YES\nClient #1 is the winner");
                                        }
                                        else{
                                            Main.getdoWeHaveAWinnerTA().setText("Winner? YES\nClient #2 is the winner");
                                        }
                                        tc.out.writeObject("Server:\n YOU WON");
                                    }
                                    else{
                                        tc.out.writeObject("Server:\n Opponent Won");
                                    }
                                }

                                //make sure the clients have made move variables are back to false
                                for(ClientThread ct : clientThreads){
                                    ct.clientHasMadeMove = false;
                                }

                                //ask if they want to play again
                                for(ClientThread ct : clientThreads){
                                    ct.out.writeObject("Server:\n Click Play Again Or Quit");
                                }

                            }
                            else{
                                //make sure the clients have made move variables are back to false
                                for(ClientThread ct : clientThreads){
                                    ct.clientHasMadeMove = false;
                                }

                                //tell the first player to make their move
                                ClientThread firstClient = clientThreads.get(0);
                                ClientThread secondClient = clientThreads.get(1);
                                secondClient.out.writeObject("Server:\n End of hand...\n Player 1 Is Making First Move...");
                                firstClient.out.writeObject("Server:\n End of hand...\n Player 1 Make Your Move...");
                            }



                        }
                    }

                    //just a simple check that a button was pressed
                    if(RPSLSPlayed((String)data)){
                        for(ClientThread c : clientThreads){
                            if(socket != c.socket){
                                c.out.writeObject(data);
                            }
                            else{
                                //c.whatClientPlayed = parseDataForWhatClientPlayed((String)data);
                                c.clientHasMadeMove = true;
                            }
                        }
                    }
                    //else{
                        //for(ClientThread c : clientThreads){
                            //c.out.writeObject(data);
                        //}
                    //}

                }

            }
            catch(Exception e) {
                callback.accept("connection Closed");
            }


        }
    }

    public static boolean RPSLSPlayed (String data){
        if(data.contains("Opponent Played")){
            return true;
        }
        else{
            return false;
        }

    }

    public boolean bothClientsMadeAMove(){
        for(ClientThread c : clientThreads){
            if(c.clientHasMadeMove == false){
                return false;
            }
        }
        return true;
    }

    public String parseDataForWhatClientPlayed(String s){
        if(s.contains("rock")){
            return "rock";
        }
        else if(s.contains("paper")){
            return "paper";
        }
        else if(s.contains("scissors")){
            return "scissors";
        }
        else if(s.contains("lizard")){
            return "lizard";
        }
        else if(s.contains("spock")){
            return "spock";
        }
        else{
            return "";
        }
    }

    public ClientThread decideWhoWins(){
        String winningKey = "";

        if(clientThreads.get(0).whatClientPlayed.equals(clientThreads.get(1).whatClientPlayed)){
            for(ClientThread c : clientThreads){
                try {
                    c.out.writeObject("Server:\n There was a TIE, no points...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        else{

            fillInRulebook(); //fill in the hash map

            winningKey = rulebook.get(clientThreads.get(0).whatClientPlayed).get(clientThreads.get(1).whatClientPlayed);

            if(clientThreads.get(0).whatClientPlayed.equals(winningKey)){
                clientThreads.get(0).clientScore++;
                try {
                    clientThreads.get(1).out.writeObject("Server:\n Opponent won the point...\n they played " + winningKey + "...");
                    clientThreads.get(0).out.writeObject("Server:\n You won the point...");
                    //clientThreads.get(1).out.writeObject(sendNewScoreBoard());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Main.getscoreboardTA().setText("Scoreboard\n Client #1: " + clientThreads.get(0).clientScore + "\n Client #2: " + clientThreads.get(1).clientScore);
                updateScores();
                return clientThreads.get(0);
            }
            else{
                clientThreads.get(1).clientScore++;
                try {
                    clientThreads.get(0).out.writeObject("Server:\n Opponent won the point...\n they played " + winningKey + "...");
                    clientThreads.get(1).out.writeObject("Server:\n You won the point...");
                    //clientThreads.get(0).out.writeObject(sendNewScoreBoard());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Main.getscoreboardTA().setText("Scoreboard\n Client #1: " + clientThreads.get(0).clientScore + "\n Client #2: " + clientThreads.get(1).clientScore);
                updateScores();
                return clientThreads.get(1);
            }
        }
    }

    public ClientThread checkForGameWinner(){
        if(clientThreads.get(0).clientScore == 3){
            return clientThreads.get(0);
        }
        else if(clientThreads.get(1).clientScore == 3){
            return clientThreads.get(1);
        }
        return null;
    }

    public String sendNewScoreBoard(){
        return "Scoreboard: \n P1-" + clientThreads.get(0).clientScore + " P2-" + clientThreads.get(1).clientScore;
    }

    public void updateScores(){
        for(ClientThread c : clientThreads){
            try {
                c.out.writeObject(sendNewScoreBoard());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean whatWasPlayedWasSent(String s){
        if(s.contains("rock")){
            return true;
        }
        else if(s.contains("paper")){
            return true;
        }
        else if(s.contains("scissors")){
            return true;
        }
        else if(s.contains("lizard")){
            return true;
        }
        else if(s.contains("spock")){
            return true;
        }
        else{
            return false;
        }
    }

    public void fillInRulebook(){
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

    public static String decideWhoWins(String a, String b, HashMap<String, HashMap<String,String>> rb){
        if(a.equals(b)){
            return "tie";
        }
        else{
            return rb.get(a).get(b);
        }
    }
}


