package stunts.wojbot.commands;

import lombok.Setter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import stunts.wojbot.core.response.WojBotResponse;
import stunts.wojbot.entity.WojBotUser;
import stunts.wojbot.utils.BotInfo;

import java.util.ArrayList;
import java.util.List;

@Setter
abstract public class WojBotCommand {

    BotInfo bot = new BotInfo();
    WojBotUser user = new WojBotUser();
    List<String> arguments = new ArrayList<>();
    String trigger = "";
    MessageReceivedEvent event = null;

    public abstract WojBotResponse evaluate();

}
