package stunts.wojbot.core.response;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class WojBotResponder {

    @Value("${channel-id.main:0}")
    private String mainChannelId;

    private final JDA jda;

    @Autowired
    public WojBotResponder(JDA jda) {
        this.jda = jda;
    }

    public boolean sendMessage(String message) {
        return sendMessage(new WojBotResponse(message));
    }

    public boolean sendMessage(String message, String channelId) {
        return sendMessage(new WojBotResponse(message, channelId));
    }

    public boolean sendMessage(WojBotResponse message) {
        if (jda != null) {
            String channelId = (message.getChannelId() == null) ? mainChannelId : message.getChannelId();
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                if (message.getReaction() != null)
                    channel.sendMessage(message.getResponseText()).queue(message1 -> message1.addReaction(message.getReaction()).queue());
                else
                    channel.sendMessage(message.getResponseText()).queue();
                return true;
            }
            else
                log.error("Cannot send message from WojBotEventChannel - channel is null, check channel ID");
        }
        log.error("Cannot send message from WojBotEventChannel - JDA is null");
        return false;
    }

    public boolean sendEmbed(EmbedBuilder embedBuilder) {
        return sendEmbed(new WojBotResponse(embedBuilder));
    }

    public boolean sendEmbed(EmbedBuilder embedBuilder, String channelId) {
        return sendEmbed(new WojBotResponse(embedBuilder, channelId));
    }

    public boolean sendEmbed(WojBotResponse message) {
        if (this.jda != null) {
            String channelId = (message.getChannelId() == null) ? mainChannelId : message.getChannelId();
            TextChannel channel = this.jda.getTextChannelById(channelId);
            if (channel != null) {
                if (message.getReaction() != null)
                    channel.sendMessage(message.getEmbedBuilder().build()).queue(message1 -> message1.addReaction(message.getReaction()).queue());
                else
                    channel.sendMessage(message.getEmbedBuilder().build()).queue();
                return true;
            }
            else
                log.error("Cannot send embed from WojBotEventChannel - channel is null, check channel ID");
        }
        log.error("Cannot send embed from WojBotEventChannel - JDA is null");
        return false;
    }

    public boolean updateChannelTopic(String topic) {
        return updateChannelTopic(topic, mainChannelId);
    }

    public boolean updateChannelTopic(String topic, String channelId) {
        TextChannel channel = this.jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.getManager().setTopic(topic).queue();
            return true;
        }
        else {
            log.error("Cannot update topic from WojBotEventChannel - channel is null, check channel ID");
            return false;
        }
    }

    public void updateGame(Game newGame) {
        this.jda.getPresence().setGame(newGame);
    }

    public void sendFile(File file) {
        sendFile(file, mainChannelId);
    }

    public void sendFile(File file, String channelId) {
        TextChannel channel = this.jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.sendFile(file).queue();
        }
        else {
            log.error("Cannot update topic from WojBotEventChannel - channel is null, check channel ID");
        }
    }

}
