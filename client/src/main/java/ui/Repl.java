package ui;

import ui.websocket.NotificationHandler;
import websocket.messages.*;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final Client client;

    public Repl(String serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to the chess server! Login to start.");
        System.out.println(SET_TEXT_COLOR_YELLOW + "Type 'help' to see a list of commands.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt(client.getState());
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_GREEN+ result.replace("Error: ", ""));
            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(SET_TEXT_COLOR_RED + msg);
            }
        }
        System.out.println();
    }

    public void notify(NotificationMessage notification) {
        System.out.println(SET_TEXT_COLOR_YELLOW + notification.getMessage());
        printPrompt(client.getState());
    }

    public void notify(ErrorMessage notification) {
        System.out.println(SET_TEXT_COLOR_RED + notification.getErrorMessage());
        printPrompt(client.getState());
    }

    public void notify(LoadGameMessage notification) {
        try {
            client.currentGame = notification.getGame();
            System.out.print("\n" + client.redrawBoard());
            printPrompt(client.getState());
        } catch (Exception ex) {
            System.out.println(SET_TEXT_COLOR_RED + "Error loading game.");
        }
    }

    private void printPrompt(String state) {
        System.out.print("\n" + SET_TEXT_COLOR_BLUE + "[" + state + "] >>> " + SET_TEXT_COLOR_BLUE);
    }

}