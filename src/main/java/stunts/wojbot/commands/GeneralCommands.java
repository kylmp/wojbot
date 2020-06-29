package stunts.wojbot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import stunts.wojbot.core.response.WojBotResponse;

@Slf4j
@Service
public class GeneralCommands extends WojBotCommand {

    private final ApplicationContext context;

    public GeneralCommands(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public WojBotResponse evaluate() {
        if (trigger.equals("wojbot")) {
            String commandSelection = arguments.get(0);
            if (commandSelection.equals("version"))
                return getVersion();
            if (commandSelection.equals("help"))
                return getVersion();
            if (commandSelection.equals("shutdown") || commandSelection.equals("stop"))
                shutDown();
        }
        return null;
    }

    private WojBotResponse getVersion() {
        if (!user.hasRole("owner")) {
            return new WojBotResponse("Sorry, you do not have permission for this command");
        }
        return new WojBotResponse(bot.getName()+ " v" +bot.getVersion());
    }

    private void shutDown() {
        if (user.hasRole("owner")) {
            event.getChannel().sendMessage(bot.getName()+" is shutting down...").queue(message1 -> message1.addReaction("\uD83D\uDE22").queue());
            int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
            System.exit(exitCode);
        }
    }

}
