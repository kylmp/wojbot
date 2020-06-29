package stunts.wojbot.core;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import stunts.wojbot.core.handler.MessageHandler;
import stunts.wojbot.core.handler.ReactionHandler;
import stunts.wojbot.utils.BotInfo;

@Slf4j
@Component
public class WojBotInitializer extends ListenerAdapter {

    private final BotInfo botInfo;
    private final MessageHandler messageHandler;
    private final ReactionHandler reactionHandler;

    @Autowired
    public WojBotInitializer(BotInfo botInfo, MessageHandler messageHandler, ReactionHandler reactionHandler) {
        this.botInfo = botInfo;
        this.messageHandler = messageHandler;
        this.reactionHandler = reactionHandler;
    }

    @Bean
    public JDA getJda() {
        try {
            return new JDABuilder(AccountType.BOT)
                    .setToken(botInfo.getDiscordToken())
                    .setGame(Game.watching("Basketball"))
                    .setStatus(OnlineStatus.ONLINE)
                    .addEventListener(this)
                    .build();
        } catch (Exception e) {
            log.error("Error Initializing Woj Bot", e);
            return null;
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        messageHandler.handleMessage(event, botInfo);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        reactionHandler.handleReaction(event);
    }

}
