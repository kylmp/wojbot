package stunts.wojbot.core.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.EmbedBuilder;
import stunts.wojbot.utils.ResponseTypes;

@Data
@NoArgsConstructor
public class WojBotResponse {

    private ResponseTypes type = ResponseTypes.NONE;
    private EmbedBuilder embedBuilder;
    private String responseText;
    private String reaction;
    private String channelId;

    public WojBotResponse(String message) {
        this.type = ResponseTypes.TEXT;
        this.responseText = message;
    }

    public WojBotResponse(String message, String channelId) {
        this.type = ResponseTypes.TEXT;
        this.responseText = message;
        this.channelId = channelId;
    }

    public WojBotResponse(EmbedBuilder eb) {
        this.type = ResponseTypes.EMBED;
        this.embedBuilder = eb;
    }

    public WojBotResponse(EmbedBuilder eb, String channelId) {
        this.type = ResponseTypes.EMBED;
        this.embedBuilder = eb;
        this.channelId = channelId;
    }

    public void setEmbedBuilder(EmbedBuilder embedBuilder) {
        this.type = (type == ResponseTypes.REACTION) ? ResponseTypes.EMBED_REACTION : ResponseTypes.EMBED;
        this.embedBuilder = embedBuilder;
    }

    public void setResponseText(String responseText) {
        this.type = (type == ResponseTypes.REACTION) ? ResponseTypes.TEXT_REACTION : ResponseTypes.TEXT;
        this.responseText = responseText;
    }

    public void addReaction(String reaction) {
        if (this.type == ResponseTypes.NONE) {
            this.type = ResponseTypes.REACTION;
        }
        else if (this.type == ResponseTypes.TEXT) {
            this.type = ResponseTypes.TEXT_REACTION;
        }
        else if (this.type == ResponseTypes.EMBED) {
            this.type = ResponseTypes.EMBED_REACTION;
        }
        this.reaction = reaction;
    }

}
