package stunts.wojbot.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import stunts.wojbot.core.response.WojBotResponse;
import stunts.wojbot.entity.TwitterAccount;
import stunts.wojbot.entity.TwitterKeywords;
import stunts.wojbot.repository.TwitterAccountsRepo;
import stunts.wojbot.repository.TwitterKeywordsRepo;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TwitterCommands extends WojBotCommand {

    private TwitterAccountsRepo accountsRepo;
    private TwitterKeywordsRepo keywordsRepo;

    @Autowired
    public void setTwitterAccountsRepo(TwitterAccountsRepo twitterAccountsRepo) {
        this.accountsRepo = twitterAccountsRepo;
    }

    @Autowired
    public void setTwitterKeywordsRepo(TwitterKeywordsRepo twitterKeywordsRepo) {
        this.keywordsRepo = twitterKeywordsRepo;
    }

    @Transactional
    @Override
    public WojBotResponse evaluate() {
        //this.bot = bot;
        if (trigger.equals("twitter")) {
            if (arguments.size() == 0)
                return new WojBotResponse("This command requires additional arguments");
            String commandSelection = arguments.get(0);
            if (commandSelection.equals("addkeyword"))
                return new WojBotResponse(addKeyword());
            if (commandSelection.equals("delkeyword"))
                return new WojBotResponse(delKeyword());
            if (commandSelection.equals("listkeywords"))
                return new WojBotResponse(listKeywords());
            if (commandSelection.equals("mute"))
                return new WojBotResponse(handleMute(true));
            if (commandSelection.equals("unmute"))
                return new WojBotResponse(handleMute(false));
            if (commandSelection.equals("listaccounts"))
                return listAccounts();
        }
        return null;
    }

    private String addKeyword() {
        if (!user.hasRole("trusted"))
            return user.deniedMessage();
        String response;
        if (arguments.size() == 1) {
            response = "Could not add twitter keyword - expected more arguments!";
        }
        else if (arguments.size() == 2) { // Save keyword for all users [!twitter addkeyword <keyword>]
            Iterable<TwitterAccount> accList = accountsRepo.findAll();
            List<TwitterKeywords> kwList = new ArrayList<>();
            for (TwitterAccount acc : accList) {
                TwitterKeywords kw = new TwitterKeywords(acc, arguments.get(1));
                kwList.add(kw);
            }
            try {
                keywordsRepo.saveAll(kwList);
                response = "Keyword saved for all users!";
            } catch (DataAccessException e) {
                log.error("Error saving twitter keyword to all users", e);
                response = "Error saving keyword to database :(";
            }
        }
        else if (arguments.size() == 3) { // Save keyword for specific user [!twitter addkeyword <handle> <keyword>]
            Optional<TwitterAccount> refAcc = accountsRepo.findByHandle(arguments.get(1));
            if (refAcc.isPresent()) {
                try {
                    keywordsRepo.save(new TwitterKeywords(refAcc.get(), arguments.get(2)));
                    response = "Keyword added! Now monitoring **@"+arguments.get(1)+"** tweets for keyword **"+arguments.get(2)+"**";
                } catch (Exception e) {
                    log.error("Error saving twitter keyword to user ID {}", refAcc.get().getUserId(), e);
                    response = "Error saving keyword to database :(";
                }
            }
            else {
                response = "Could not add keyword - "+bot.getName()+" is not monitoring twitter user @"+arguments.get(1)+" (case sensitive)";
            }
        }
        else {
            response = "Could not add twitter keyword - too many arguments!";
        }
        return response;
    }

    @Transactional
    public String delKeyword() {
        if (!user.hasRole("trusted"))
            return user.deniedMessage();
        String response;
        if (arguments.size() == 1) {
            response = "Could not delete twitter keyword - expected more arguments!";
        }
        else if (arguments.size() == 2) { // Delete keyword for all users [!twitter addkeyword <keyword>]
            try {
                int num = keywordsRepo.deleteAllByKeyword(arguments.get(1));
                response = "Keyword deleted for all monitored twitter accounts!";
                if (num == 0) {
                    response = "Keyword was not being used, there's nothing to delete!";
                }
            } catch (DataAccessException e) {
                log.error("Error deleting twitter keyword from all monitored twitter accounts", e);
                response = "Error deleting keyword";
            }
        }
        else if (arguments.size() == 3) { // Delete keyword for specific user [!twitter addkeyword <handle> <keyword>]
            Optional<TwitterAccount> acc = accountsRepo.findByHandle(arguments.get(1));
            if (acc.isPresent()) {
                Optional<TwitterKeywords> keyword = keywordsRepo.findByUserIdAndKeyword(acc.get(), arguments.get(2));
                if (keyword.isPresent()) {
                    try {
                        keywordsRepo.delete(keyword.get());
                        response = "Keyword **"+arguments.get(2)+"** deleted for monitored account **@"+arguments.get(1)+"**";
                    } catch (Exception e) {
                        log.error("Error deleting keyword from user ID {}", acc.get().getUserId(), e);
                        response = "Error deleting keyword from monitored user **@"+arguments.get(1)+"**";
                    }
                }
                else {
                    response = "Could not find monitored keyword **"+arguments.get(2)+"** for user **@"+arguments.get(1)+"**";
                }
            }
            else {
                response = "Could not delete keyword... "+bot.getName()+" is not monitoring twitter user **@"+arguments.get(1)+"** (case sensitive)";
            }
        }
        else {
            response = "Could not delete twitter keyword - too many arguments!";
        }
        return response;
    }

    private WojBotResponse listAccounts() {
        if (!user.hasRole("admin"))
            return new WojBotResponse(user.deniedMessage());
        if (arguments.size() == 1) {
            StringBuilder accounts = new StringBuilder();
            boolean hasMuted = false;
            for (TwitterAccount account : accountsRepo.findAll()) {
                if (account.isMute()) {
                    hasMuted = true;
                    accounts.append(", @").append(account.getHandle());
                }
                else
                    accounts.append(", **@").append(account.getHandle()).append("**");
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(bot.getName()+" Monitored Twitter Accounts");
            String desc = accounts.toString().substring(2);
            if (hasMuted) {
                desc += "\n\n*Non-bold accounts are muted*";
            }
            embed.setDescription(desc);
            embed.setColor(bot.getColor());
            return new WojBotResponse(embed);
        }
        return new WojBotResponse("Error retrieving monitored twitter accounts");
    }

    private String listKeywords() {
        if (!user.hasRole("trusted"))
            return user.deniedMessage();
        if (arguments.size() == 1) {
            return "Please use *!twitter listkeywords <handle>*";
        }
        if (arguments.size() == 2) {
            Optional<TwitterAccount> acc = accountsRepo.findByHandle(arguments.get(1));
            if (acc.isPresent()) {
                Optional<List<TwitterKeywords>> keyWordList = keywordsRepo.findAllByUserId(acc.get());
                if (keyWordList.isPresent()) {
                    StringBuilder keywords = new StringBuilder();
                    for (TwitterKeywords keyword : keyWordList.get()) {
                        keywords.append(", ").append(keyword.getKeyword());
                    }
                    return "Monitored keywords for **@"+arguments.get(1)+"**: "+keywords.toString().substring(2);
                }
                else {
                    return "No keywords assigned for monitored twitter user **@"+arguments.get(1)+"**";
                }
            }
            else {
                return bot.getName()+" is not monitoring twitter user **@"+arguments.get(1)+"**";
            }
        }
        return "Error retrieving twitter keywords";
    }

    private String handleMute(boolean mute) {
        if (!user.hasRole("trusted"))
            return user.deniedMessage();
        String type = (mute) ? "muted" : "unmuted";
        if (arguments.size() == 1) {
            List<TwitterAccount> updatedAccountList = new ArrayList<>();
            for (TwitterAccount account : accountsRepo.findAll()) {
                account.setMute(mute);
                updatedAccountList.add(account);
            }
            accountsRepo.saveAll(updatedAccountList);
            return "All monitored twitter accounts are now "+type;
        }
        if (arguments.size() == 2) {
           Optional<TwitterAccount> curAccount = accountsRepo.findByHandle(arguments.get(1));
           if (curAccount.isPresent()) {
               curAccount.get().setMute(mute);
               accountsRepo.save(curAccount.get());
               return "Monitored twitter account **@"+arguments.get(1)+"** is now "+type;
           }
           else {
               return bot.getName()+" is not monitoring twitter user **@"+arguments.get(1)+"** (case sensitive)";
           }
        }
        return "Too many arguments provided! Use **\"!twitter mute\"** or **\"!twitter unmute\"** to (un)mute all twitter accounts";
    }

}
