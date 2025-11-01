package ui.api;

import game.api.*;
import java.util.List;

public interface IUserInterface {

    void initialize(IGameContext context);

    void show();

    void hide();

    void update();

    void displayMessage(String message);

    void displayError(String error);

    int getUserChoice(String prompt, List<String> options);

    String getUserInput(String prompt);

    boolean confirm(String message);

    void clear();

    void close();

    void setContext(IGameContext gameContext);
}
