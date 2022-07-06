package art.arcane.edict.sender;

import art.arcane.edict.message.Message;

public interface Sender {
    void sendMessage(Message message);
}
