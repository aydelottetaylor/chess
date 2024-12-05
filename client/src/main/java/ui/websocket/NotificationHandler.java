package ui.websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void notify(ErrorMessage notification);
    void notify(NotificationMessage notification);
    void notify(LoadGameMessage notification);
}