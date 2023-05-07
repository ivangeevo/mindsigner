package hive.ivangeevo.mindsigner;

import net.minecraft.network.chat.TextComponent;

import static net.minecraftforge.client.ClientCommandHandler.sendMessage;

public class ChatHandler {

    public void handleMessage() {
        boolean client = false;
        String message = new String();
        TextComponent component = new TextComponent(message);
        assert false; sendMessage(String.valueOf(component));
}
}