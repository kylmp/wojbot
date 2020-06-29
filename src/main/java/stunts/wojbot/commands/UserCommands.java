package stunts.wojbot.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import stunts.wojbot.core.response.WojBotResponse;
import stunts.wojbot.entity.WojBotUser;
import stunts.wojbot.repository.WojBotUserRepo;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserCommands extends WojBotCommand {

    private WojBotUserRepo userRepo;

    @Autowired
    public void setWojBotUserRepo(WojBotUserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public WojBotResponse evaluate() {
        if (trigger.equals("addadmin"))
            return addRole("admin");
        if (trigger.equals("addtrusted"))
            return addRole("trusted");
        if (trigger.equals("removeadmin"))
            return removeRole("admin");
        if (trigger.equals("removetrusted"))
            return removeRole("trusted");
        return null;
    }

    private WojBotResponse addRole(String role) {
        if (!user.hasRole("admin"))
            return new WojBotResponse(user.deniedMessage());
        if (role.equals("owner")) {
            WojBotResponse response = new WojBotResponse("You can't do that!");
            response.addReaction("\uD83D\uDE20");
            return response;
        }
        if (arguments.size() == 1) {
            List<Member> members = event.getGuild().getMembersByNickname(arguments.get(0), false);
            if (members.size() == 1) {
                User foundUser = members.get(0).getUser();
                Optional<WojBotUser> wojBotUserOpt = userRepo.findById(foundUser.getIdLong());
                WojBotUser wojBotUser = wojBotUserOpt.orElseGet(() -> new WojBotUser(foundUser.getIdLong(), "basic", foundUser.getName()));
                if (!wojBotUser.hasRole(role)) {
                    wojBotUser.addRole(role);
                    userRepo.save(wojBotUser);
                    return new WojBotResponse(arguments.get(0)+" has been given the role *"+role+"*");
                }
                else
                    return new WojBotResponse(arguments.get(0)+" already has this role");
            }
            else if (members.size() == 0) {
                return new WojBotResponse("Cannot find user with nickname *"+arguments.get(0)+"*");
            }
            else {
                return new WojBotResponse("Error: Found multiple users with nickname *"+arguments.get(0)+"*");
            }
        }
        else {
            return new WojBotResponse("Error: Expected 1 argument, use *!addadmin \"<nickname>\"*");
        }
    }

    private WojBotResponse removeRole(String role) {
        if (user.hasRole("admin")) {
            if (role.equals("basic") || role.equals("owner")) {
                WojBotResponse response = new WojBotResponse("You can't do that!");
                response.addReaction("\uD83D\uDE20");
                return response;
            }
            if (arguments.size() == 1) {
                List<Member> members = event.getGuild().getMembersByNickname(arguments.get(0), false);
                if (members.size() == 1) {
                    User foundUser = members.get(0).getUser();
                    Optional<WojBotUser> wojBotUserOpt = userRepo.findById(foundUser.getIdLong());
                    WojBotUser wojBotUser = wojBotUserOpt.orElseGet(() -> new WojBotUser(foundUser.getIdLong(), "basic", foundUser.getName()));
                    wojBotUser.removeRole(role);
                    userRepo.save(wojBotUser);
                    return new WojBotResponse("Removed role "+role+" from user "+arguments.get(0));
                }
                else if (members.size() == 0) {
                    return new WojBotResponse("Cannot find user with nickname *"+arguments.get(0)+"*");
                }
                else {
                    return new WojBotResponse("Error: Found multiple users with nickname *"+arguments.get(0)+"*");
                }
            }
            else {
                return new WojBotResponse("Error: Expected 1 argument, use *!addadmin \"<nickname>\"*");
            }
        }
        else {
            return new WojBotResponse("You do not have permission for this command");
        }
    }

}
