package stunts.wojbot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
public class TwitterAccount {

    @Id
    @Column
    long userId;

    @Column
    String monitorFrequency;

    @Column
    long lastReadId;

    @Column
    String name;

    @Column
    String handle;

    @Column
    Date updatedDate;

    @Column
    boolean mute;

    @OneToMany(mappedBy = "userId")
    private List<TwitterKeywords> phones;

}
