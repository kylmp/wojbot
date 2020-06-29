package stunts.wojbot.core.handler;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReactionHandler {

    public void handleReaction(MessageReactionAddEvent event) {
//        User reactor = event.getMember().getUser();
//        MessageReaction reaction = event.getReaction();
//
//        if (reactor.isBot()) {
//            return;
//        }
//
//        event.getChannel().sendMessage(reactor.getName()+" reacted with "+ reaction.getReactionEmote().getName()).queue();
    }

}
