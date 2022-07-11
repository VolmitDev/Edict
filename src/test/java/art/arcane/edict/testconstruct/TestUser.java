package art.arcane.edict.testconstruct;

import art.arcane.edict.message.Message;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestUser implements User {

    public final List<StringMessage> received = new ArrayList<>();

    @Override
    public boolean canUseContext() {
        return false;
    }

    /**
     * Whether the user can use clickable messages.
     */
    @Override
    public boolean canUseClickable() {
        return false;
    }

    @Override
    public void send(@NotNull Message message) {
        if (message instanceof StringMessage) {
            received.add((StringMessage) message);
        } else {
            received.add(new StringMessage("Received non-string message"));
        }
    }
}
