package stunts.wojbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table
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
