package stunts.wojbot.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import stunts.wojbot.service.TwitterService;

@Component
public class TwitterSchedule {

    private TwitterService twitterService;

    @Autowired
    public void setTwitterService(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    @Scheduled(cron="0 * * ? * *") // Every minute
    public void checkTwitter() {
        twitterService.checkAllTimelines();
    }

}
