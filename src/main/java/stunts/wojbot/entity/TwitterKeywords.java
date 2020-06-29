package stunts.wojbot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table
public class TwitterKeywords {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TwitterAccount userId;

    @Column
    String keyword;

    public TwitterKeywords(TwitterAccount acc, String keyword) {
        this.userId = acc;
        this.keyword = keyword;
    }

}
