


import three_in_row.logic.ObservableGame;
import three_in_row.ui.text.TextUI;

public class MainWithTextUI {

    public static void main(String[] args) {
        TextUI textUI = new TextUI(new ObservableGame());
        textUI.run();
    }
}
