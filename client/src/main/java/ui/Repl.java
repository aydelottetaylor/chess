package ui;

import ui.websocket.NotificationHandler;
import webSocketMessages.Notification;

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
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void notify(Notification notification) {
        System.out.println(SET_TEXT_COLOR_RED + notification.message());
        printPrompt(client.getState());
    }

    private void printPrompt(String state) {
        System.out.print("\n" + SET_TEXT_COLOR_BLUE + "[" + state + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

}