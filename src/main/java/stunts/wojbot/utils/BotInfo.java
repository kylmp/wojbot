package stunts.wojbot.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class BotInfo {

    @Value("${token.discord}")
    private String discordToken;

    @Value("${bot.name:WojBot}")
    private String name;

    @Value("${app.version:}")
    private String version;

    @Value("${bot.color:536870911}")
    private int color;

}
