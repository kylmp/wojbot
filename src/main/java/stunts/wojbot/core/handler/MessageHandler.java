package stunts.wojbot.core.handler;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import stunts.wojbot.core.response.WojBotResponse;
import stunts.wojbot.commands.WojBotCommand;
import stunts.wojbot.entity.WojBotUser;
import stunts.wojbot.repository.WojBotUserRepo;
import stunts.wojbot.utils.BotInfo;
import stunts.wojbot.utils.MessageUtils;
import stunts.wojbot.utils.ResponseTypes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MessageHandler {

    private List<WojBotCommand> commands;
    private BotInfo botInfo;
    private WojBotUserRepo userRepo;

    @Value("${command.trigger:!}")
    private String commandTrigger;

    @Autowired
    public MessageHandler(List<WojBotCommand> commands, WojBotUserRepo userRepo) {
        this.commands = commands;
        this.userRepo = userRepo;
    }

    public void handleMessage(MessageReceivedEvent event, BotInfo botInfo) {
        this.botInfo = botInfo;
        String message = event.getMessage().getContentDisplay();
        User author = event.getAuthor();
        if (author.isBot()) {
            this.handleBotMessageText(event);
        }
        else {
            if (message.startsWith(commandTrigger)) {
                WojBotUser user = retrieveUser(author);
                handleCommandLogging(event);
                handleCommandExecution(event, user, message);
            }
        }
    }

    private WojBotUser retrieveUser(User author) {
        Optional<WojBotUser> user = userRepo.findById(author.getIdLong());
        WojBotUser wojBotUser;
        boolean doUpdate;
        if (user.isPresent()) {
            wojBotUser = user.get();
            doUpdate = (System.currentTimeMillis() - wojBotUser.getUpdateDate().getTime()) > 86400000; // Update if record is a day old
        }
        else {
            wojBotUser = new WojBotUser(author.getIdLong(), "basic", author.getName());
            doUpdate = true;
        }
        if (doUpdate) {
            userRepo.save(wojBotUser);
        }
        wojBotUser.setDiscordUser(author);
        return wojBotUser;
    }

    private void handleCommandLogging(MessageReceivedEvent event) {
        log.info("COMMAND RUN BY "+event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    private void handleBotMessageText(MessageReceivedEvent event) {
        //Bots deserve nothing
    }

    private void handleCommandExecution(MessageReceivedEvent event, WojBotUser user, String message) {
        List<String> arguments = MessageUtils.getArguments(message);
        for (WojBotCommand command : commands) {
            command.setBot(botInfo);
            command.setArguments(arguments.subList(1, arguments.size()));
            command.setTrigger(arguments.get(0).substring(commandTrigger.length()));
            command.setEvent(event);
            command.setUser(user);
            WojBotResponse response = command.evaluate();
            if (response != null && !response.getType().equals(ResponseTypes.NONE) && !response.getType().equals(ResponseTypes.REACTION)) {
                sendResponse(response, event.getChannel());
                break;
            }
        }
    }

    private void sendResponse(WojBotResponse response, MessageChannel channel) {
        switch (response.getType()) {
            case TEXT:
                channel.sendMessage(response.getResponseText()).queue();
                break;
            case TEXT_REACTION:
                channel.sendMessage(response.getResponseText()).queue(message1 -> message1.addReaction(response.getReaction()).queue());
                break;
            case EMBED:
                channel.sendMessage(response.getEmbedBuilder().build()).queue();
                break;
            case EMBED_REACTION:
                channel.sendMessage(response.getEmbedBuilder().build()).queue(message1 -> message1.addReaction(response.getReaction()).queue());
                break;
        }
    }

}
