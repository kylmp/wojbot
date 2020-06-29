package stunts.wojbot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.User;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table
public class WojBotUser {

    @Id
    @Column
    private long userId;

    @Column
    private String roles;

    @Column
    private String name;

    @Column
    private Date updateDate;

    @Transient
    private User discordUser;

    @PrePersist
    protected void onCreate() {
        updateDate = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = new Date();
    }

    public WojBotUser(long userId, String roles, String name) {
        this.userId = userId;
        this.roles = roles;
        this.name = name;
    }

    public void addRole(String role) {
        roles += "," + role;
    }

    public void removeRole(String role) {
        if (!role.equals("basic")) {
            roles = roles.replace(","+role, "");
        }
    }

    public boolean hasRole(String role) {
        if (role.equals("owner")) {
            for (String r : roles.split(",")) {
                if (r.equals("owner"))
                    return true;
            }
        }
        for (String r : roles.split(",")) {
            if (r.equals(role) || r.equals("admin"))
                return true;
        }
        return false;
    }

    public String deniedMessage() {
        return "You do not have permission for this command";
    }

}
