package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application{

    private int portNumber;
    private NetworkConnection conn;
    private TextArea messages = new TextArea(); //for the example messages
    private TextField input = new TextField();
    private DropShadow dropShadow = new DropShadow();
    //to choose the port number
    private ChoiceBox portChoiceBox = new ChoiceBox();
    //turn server on
    private Button turnServerOnButton = new Button("Turn Server On");
    //turn server off
    private Button turnServerOffButton = new Button("Turn Server Off");
    //game state stuff
    //how many players
    private static Text howManyPlayersTA = new Text("Number Of Players");
    //what each player played
    private static  Text whatThePlayersPlayedTA = new Text("What The Players Played");
    //how many points each player has
    private static  Text scoreboardTA = new Text("Scoreboard");
    //if someone won
    private static  Text doWeHaveAWinnerTA = new Text("Winner?");
    //if the player is playing again
    private static  Text whoIsPlayingAgainTextArea = new Text("Who's Playing Again");

    private VBox messageInputVBox;
    private HBox buttonsHBox;
    private static BorderPane serverSceneBorderPane;
    private VBox gameDataVBox;
    private VBox topWelcomeBox;

    Text welcomeText = new Text("RPSLS Server"); //welcome text
    DropShadow textDropShadow = new DropShadow();

    Color c = Color.web("FFFFFF",1.0);
    Color b = Color.web("2e4053",1.0);

    public static BorderPane getServerSceneBorderPane(){
        return serverSceneBorderPane;
    }
    public static Text getHowManyPlayersTA(){
        return howManyPlayersTA;
    }
    public static  Text getwhatThePlayersPlayedTA(){
        return whatThePlayersPlayedTA;
    }
    //how many points each player has
    public static  Text getscoreboardTA(){
        return scoreboardTA;
    }
    //if someone won
    public static  Text getdoWeHaveAWinnerTA(){
        return doWeHaveAWinnerTA;
    }
    //if the player is playing again
    public static  Text getwhoIsPlayingAgainTextArea(){
        return whoIsPlayingAgainTextArea;
    }
    public static void setHowManyPlayersTA(String s){
        howManyPlayersTA.setText(s);
    }
    public static  void setwhatThePlayersPlayedTA(String s){
        whatThePlayersPlayedTA.setText(s);
    }
    //how many points each player has
    public static  void setscoreboardTA(String s){
        scoreboardTA.setText(s);
    }
    //if someone won
    public static  void setdoWeHaveAWinnerTA(String s){
        doWeHaveAWinnerTA.setText(s);
    }
    //if the player is playing again
    public static  void setwhoIsPlayingAgainTextArea(String s){
        whoIsPlayingAgainTextArea.setText(s);
    }

    private void createPartsOfScene(){
        textDropShadow.setRadius(20.0);
        textDropShadow.setOffsetX(5.0);
        textDropShadow.setOffsetY(5.0);

        textDropShadow.setColor(b);
        welcomeText.setEffect(textDropShadow);
        welcomeText.setFill(c);
        welcomeText.setFont(Font.font(null, FontWeight.BOLD, 50));

        //init
        serverSceneBorderPane = new BorderPane();
        portChoiceBox.getItems().add("Select A Port");
        portChoiceBox.getItems().add("5555");
        portChoiceBox.getItems().add("6666");
        portChoiceBox.getItems().add("7777");
        portChoiceBox.getItems().add("8888");
        portChoiceBox.getItems().add("9999");
        portChoiceBox.getSelectionModel().selectFirst();
        topWelcomeBox = new VBox(welcomeText);
        messageInputVBox = new VBox(10, messages, input);
        gameDataVBox = new VBox(10,howManyPlayersTA,whatThePlayersPlayedTA,scoreboardTA,doWeHaveAWinnerTA,whoIsPlayingAgainTextArea);
        buttonsHBox = new HBox(10, turnServerOffButton, turnServerOnButton,portChoiceBox);
        turnServerOnButton.setDisable(true);
        turnServerOffButton.setDisable(true);

        //styling
        messageInputVBox.setEffect(dropShadow);
        turnServerOffButton.setEffect(dropShadow);
        turnServerOnButton.setEffect(dropShadow);
        serverSceneBorderPane.setStyle("-fx-background-color: #5dade2;");
        portChoiceBox.setEffect(dropShadow);

        //alignment
        messageInputVBox.setAlignment(Pos.CENTER);
        buttonsHBox.setAlignment(Pos.CENTER);
        gameDataVBox.setAlignment(Pos.CENTER);
        topWelcomeBox.setAlignment(Pos.CENTER);

        //setting sizes
        messageInputVBox.setPrefSize(400, 300);
        gameDataVBox.setPrefSize(200, 300);
        buttonsHBox.setPrefSize(600, 200);

        //adding to main borderpane
        serverSceneBorderPane.setLeft(messageInputVBox);
        serverSceneBorderPane.setBottom(buttonsHBox);
        serverSceneBorderPane.setRight(gameDataVBox);
        serverSceneBorderPane.setTop(topWelcomeBox);
    }

    private Parent createContent() {
        input.setOnAction(event -> {
            String message = "Server: ";
            message += input.getText();
            input.clear();

            messages.appendText(message + "\n");
            try {
                conn.send(message);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        });

        createPartsOfScene();

        EventHandler<ActionEvent> turnOnServerEH = new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                try {
                    conn = createServer();
                    conn.startConn();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        EventHandler<ActionEvent> turnOffServerEH = new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                //need this in for each or nah?
                try {
                    conn.closeConn();
                    Platform.exit();
                    //for(NetworkConnection.ClientThread c : conn.getClientThreads()){}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        EventHandler<ActionEvent> portIsChosenEH = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                ChoiceBox b = (ChoiceBox)event.getSource();

                int potentialPort = Integer.parseInt((String)portChoiceBox.getValue());

                //make sure port is a number
                if(potentialPort == (int)potentialPort){
                    portNumber = potentialPort;

                    //disable the port choice box
                    b.setDisable(true);
                    //make the turn on server button active
                    turnServerOnButton.setDisable(false);
                    turnServerOffButton.setDisable(false);
                }

            }
        };

        //set up event handlers
        turnServerOffButton.setOnAction(turnOffServerEH);
        turnServerOnButton.setOnAction(turnOnServerEH);
        portChoiceBox.setOnAction(portIsChosenEH);

        return serverSceneBorderPane;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO Auto-generated method stub
        primaryStage.setTitle("RPSLS Server");
        Scene s = new Scene(createContent(),800,800);
        primaryStage.setScene(s);
        primaryStage.show();
    }

    @Override
    public void init() throws Exception{
        //conn.startConn();
    }

    @Override
    public void stop() throws Exception{
        if(conn != null){
            conn.closeConn();
            Platform.exit();
        }
        else{
            Platform.exit();
        }
    }

    private Server createServer() {
        return new Server(portNumber, data-> {
            Platform.runLater(()->{
                messages.appendText(data.toString() + "\n");
            });
        });
    }

}
