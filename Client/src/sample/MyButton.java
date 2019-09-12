package sample;

import javafx.scene.control.Button;

public class MyButton extends Button {
    private String buttonFileName;

    public String getButtonFileName() {
        return buttonFileName;
    }

    public void setButtonFileName(String s){
        this.buttonFileName = s;
    }
}
