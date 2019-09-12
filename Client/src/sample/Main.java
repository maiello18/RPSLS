package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sun.nio.cs.HistoricallyNamedCharset;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application{
    private int portNumber;
    private String IPString;
    private NetworkConnection conn; // = createClient();
    private static TextArea messages = new TextArea();
    Text welcomeText = new Text("RPSLS Client"); //welcome text
    DropShadow textDropShadow = new DropShadow();
    TextField input = new TextField();
    private DropShadow dropShadow = new DropShadow();
    //to choose the port number and ip
    private ChoiceBox portChoiceBox = new ChoiceBox();
    private TextField IPTextField = new TextField("Type IP Here");
    //connect to server button
    private Button connectToServerButton = new Button("Connect To Server");
    //game state stuff
    //how many points each player has
    //private  Text scoreboardTA = new Text("Scoreboard");
    private  Text whatTheOpponentPlayedText = new Text("What Opponent Played");
    //buttons with images to choose what to play
    private MyButton rockButton = new MyButton();
    private MyButton paperButton = new MyButton();
    private MyButton scissorsButton = new MyButton();
    private MyButton lizardButton = new MyButton();
    private MyButton spockButton = new MyButton();
    //way to display messages from server is there already...from the fx nextworking example
    private Button playAgainButton = new Button("Click To Play Again");
    private Button quitButton = new Button("Quit");

    private VBox messageInputVBox;
    private HBox RPSLSHBox;
    private BorderPane clientSceneBorderPane;
    private VBox gameDataVBox;
    private VBox topWelcomeBox;
    private HBox buttonsHBox;
    private HashMap<String,MyButton> RPSLSButtonsMap = new HashMap<String,MyButton>();

    Color c = Color.web("FFFFFF",1.0);
    Color b = Color.web("2e4053",1.0);

    public static TextArea getMessages() {
        return messages;
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
        clientSceneBorderPane = new BorderPane();
        portChoiceBox.getItems().add("Select A Port");
        portChoiceBox.getItems().add("5555");
        portChoiceBox.getItems().add("6666");
        portChoiceBox.getItems().add("7777");
        portChoiceBox.getItems().add("8888");
        portChoiceBox.getItems().add("9999");
        portChoiceBox.getSelectionModel().selectFirst();

        RPSLSButtonsMap.put("rock",rockButton);
        RPSLSButtonsMap.put("paper",paperButton);
        RPSLSButtonsMap.put("scissors",scissorsButton);
        RPSLSButtonsMap.put("lizard",lizardButton);
        RPSLSButtonsMap.put("spock",spockButton);

        topWelcomeBox = new VBox(welcomeText);
        messageInputVBox = new VBox(10, messages, input);
        //gameDataVBox = new VBox(10,scoreboardTA,whatTheOpponentPlayedText);
        RPSLSHBox = new HBox(5, rockButton, paperButton,scissorsButton,lizardButton,spockButton);
        connectToServerButton.setDisable(true);
        buttonsHBox = new HBox(10,IPTextField,portChoiceBox,connectToServerButton,playAgainButton,quitButton);
        //styling
        messageInputVBox.setEffect(dropShadow);
        connectToServerButton.setEffect(dropShadow);
        rockButton.setEffect(dropShadow);
        paperButton.setEffect(dropShadow);
        scissorsButton.setEffect(dropShadow);
        lizardButton.setEffect(dropShadow);
        spockButton.setEffect(dropShadow);
        IPTextField.setEffect(dropShadow);
        quitButton.setEffect(dropShadow);
        playAgainButton.setEffect(dropShadow);
        clientSceneBorderPane.setStyle("-fx-background-color: #3498db;");
        portChoiceBox.setEffect(dropShadow);

        //setting up button images
        for(Map.Entry<String, MyButton> button : RPSLSButtonsMap.entrySet()){
            String key = button.getKey();
            MyButton btn = button.getValue();

            Image pic = new Image(key+".png");
            ImageView v = new ImageView(pic);
            v.setFitHeight(300);
            v.setFitWidth(90);
            v.setPreserveRatio(true);

            btn.setGraphic(v);
            btn.setButtonFileName(key);
        }


            //alignment
        messageInputVBox.setAlignment(Pos.CENTER);
        RPSLSHBox.setAlignment(Pos.CENTER);
        //gameDataVBox.setAlignment(Pos.CENTER);
        topWelcomeBox.setAlignment(Pos.CENTER);
        buttonsHBox.setAlignment(Pos.CENTER);


        //setting sizes
        messageInputVBox.setPrefSize(300, 400);
        //gameDataVBox.setPrefSize(200, 400);
        RPSLSHBox.setPrefSize(400, 200);
        buttonsHBox.setPrefSize(800, 200);

        //somethings need to start disabled
        connectToServerButton.setDisable(true);
        playAgainButton.setDisable(true);

        //adding to main borderpane
        clientSceneBorderPane.setCenter(RPSLSHBox);
        clientSceneBorderPane.setLeft(messageInputVBox);
        clientSceneBorderPane.setBottom(buttonsHBox);
        //clientSceneBorderPane.setRight(gameDataVBox);
        clientSceneBorderPane.setTop(topWelcomeBox);
    }

    private Parent createContent() {
        input.setOnAction(event -> {
            String message = "Client: ";
            message += input.getText();
            input.clear();

            //messages.appendText(message + "\n");

            try {
                conn.send(message);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        });

        createPartsOfScene();

        EventHandler<ActionEvent> IPIsEnteredEH = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                TextField tf = (TextField)event.getSource();

                IPString = tf.getText();

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
                    playAgainButton.setDisable(false);
                    connectToServerButton.setDisable(false);
                }

            }
        };

        EventHandler<ActionEvent> connectToServerEH = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Button b = (Button) event.getSource();

                try {
                    conn = createClient();
                    conn.startConn();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        EventHandler<ActionEvent> quitButtonEH = new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                if(conn == null){
                    Platform.exit();
                }
                else{
                    try {
                        conn.closeConn();
                        Platform.exit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        EventHandler<ActionEvent> RPSLSButtonsEH = new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                MyButton b = (MyButton) event.getSource();
                String whatWasPlayed = b.getButtonFileName();
                String message = "Server:\n Opponent Played their hand...\n Now make your move...\n";

                if(conn != null){
                    try {
                        conn.send(message);
                        conn.send(whatWasPlayed);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        EventHandler<ActionEvent> playAgainButtonEH = new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                Button b = (Button) event.getSource();
                String message = "YesPlayAgain";

                if(conn != null){
                    try {
                        conn.send(message);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //set on action
        playAgainButton.setOnAction(playAgainButtonEH);
        connectToServerButton.setOnAction(connectToServerEH);
        IPTextField.setOnAction(IPIsEnteredEH);
        portChoiceBox.setOnAction(portIsChosenEH);
        quitButton.setOnAction(quitButtonEH);
        for(Map.Entry<String, MyButton> button : RPSLSButtonsMap.entrySet()){
            String key = button.getKey();
            MyButton btn = button.getValue();

            btn.setOnAction(RPSLSButtonsEH);
        }



        return clientSceneBorderPane;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO Auto-generated method stub
        primaryStage.setTitle("RPSLS Client");
        primaryStage.setScene(new Scene(createContent()));
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

    private Client createClient() {
        return new Client(IPString, portNumber, data -> {
            Platform.runLater(()->{
                messages.appendText(data.toString() + "\n");
            });
        });
    }


}