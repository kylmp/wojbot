package stunts.wojbot.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import stunts.wojbot.core.response.WojBotResponder;
import stunts.wojbot.entity.TwitterAccount;
import stunts.wojbot.entity.TwitterKeywords;
import stunts.wojbot.repository.TwitterAccountsRepo;
import stunts.wojbot.repository.TwitterKeywordsRepo;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TwitterService {

    private RestTemplate restTemplate;
    private TwitterAccountsRepo twitterAccountsRepo;
    private TwitterKeywordsRepo twitterKeywordsRepo;
    private WojBotResponder responder;

    @Value("${channel-id.twitter:0}")
    private String twitterChannelId;

    @Value("${channel-id.main:0}")
    private String mainChannelId;

    @Value("${token.twitter:0}")
    private String oauth2Token;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setTwitterRepo(TwitterAccountsRepo twitterAccountsRepo) {
        this.twitterAccountsRepo = twitterAccountsRepo;
    }

    @Autowired
    public void setKeywordRepo(TwitterKeywordsRepo keywordRepo) {
        this.twitterKeywordsRepo = keywordRepo;
    }

    @Autowired
    public void setResponder(WojBotResponder responder) {
        this.responder = responder;
    }

    public void checkAllTimelines() {
        if (oauth2Token.equals("0")) {
            log.debug("No twitter oauth token provided, twitter service is disabled");
        }
        else {
            Iterable<TwitterAccount> accounts = twitterAccountsRepo.findAll();
            for (TwitterAccount account : accounts) {
                if (!account.isMute()) {
                    analyzeTimeline(account);
                }
            }
        }
    }

    private HttpEntity getHttpEntity() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + oauth2Token);
        return new HttpEntity(headers);
    }

    private void analyzeTimeline(TwitterAccount account) {

        Date updateDate = new Date();
        String url = "https://api.twitter.com/1.1/statuses/user_timeline.json" +
                "?user_id="+account.getUserId()+"&since_id="+account.getLastReadId()+"&tweet_mode=extended";
        ResponseEntity<String> responseString = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), String.class);

        try {
            JSONArray response = new JSONArray(responseString.getBody());
            long diffInMs = updateDate.getTime() - account.getUpdatedDate().getTime();
            if (diffInMs >= 300000) {
                if (response.length() > 0) {
                    JSONObject lastTweet = response.getJSONObject(0);
                    account.setLastReadId(lastTweet.getLong("id"));
                }
            }
            else {
                int numTweets = (response.length() <= 3) ? response.length() : 3;
                for (int i = numTweets-1; i >= 0; i--) {
                    JSONObject tweetJson = response.getJSONObject(i);
                    account.setLastReadId(tweetJson.getLong("id"));

                    // Determine if it is a re-tweet or a reply
                    boolean retweeted = tweetJson.getString("full_text").startsWith("RT @");
                    boolean reply = !retweeted && !tweetJson.isNull("in_reply_to_screen_name");
                    if (reply) {
                        reply = !tweetJson.getString("in_reply_to_screen_name").equals(account.getHandle());
                    }

                    // Build tweet object
                    Tweet tweet = new Tweet((retweeted) ? tweetJson.getJSONObject("retweeted_status") : tweetJson, reply);

                    // Post tweet in main channel if it contains keyword (allows replies and re-tweets)
                    Optional<List<TwitterKeywords>> keyWords = twitterKeywordsRepo.findAllByUserId(account);
                    if (keyWords.isPresent()) {
                        for (TwitterKeywords keyWord : keyWords.get()) {
                            if (tweet.text.contains(keyWord.getKeyword())) {
                                responder.sendEmbed(tweet.getEmbed(), mainChannelId);
                                break;
                            }
                        }
                    }

                    // Send tweet to twitter channel if it is not a reply or re-tweet
                    if (!(retweeted || reply)) {
                        if (!twitterChannelId.equals("0")) {
                            responder.sendEmbed(tweet.getEmbed(), twitterChannelId);
                        }
                        account.setHandle(tweet.handle);
                        account.setName(tweet.name);
                    }
                }
            }
            account.setUpdatedDate(updateDate);
            twitterAccountsRepo.save(account);
        }
        catch (JSONException | DataAccessException e) {
            log.error("Error parsing or saving twitter timeline response", e);
        }

    }

    private class Tweet {
        private long id;
        private String text;
        private String tweetUrl;

        private String name;
        private String handle;
        private String userPhoto;

        private String replyUserHandle;
        private long replyTweetId;

        boolean hasPic;
        String picUrl;

        Tweet(JSONObject tweet, boolean isReply) {
            // General Tweet Items
            id = tweet.getLong("id");
            text = tweet.getString("full_text").toLowerCase();

            // User Tweet Items
            JSONObject user = tweet.getJSONObject("user");
            handle = user.getString("screen_name");
            name = user.getString("name");
            userPhoto = user.getString("profile_image_url");

            if (tweet.getBoolean("is_quote_status")) {
                JSONObject quote = tweet.getJSONObject("quoted_status");
                JSONObject quoteUser = quote.getJSONObject("user");
                text += "\n\n**Quoted Text (@" + quoteUser.getString("screen_name") + ")**\n"+quote.getString("full_text");
            }

            // Determine if tweet includes a picture
            JSONObject entities = tweet.getJSONObject("entities");
            if (entities.has("media")) {
                JSONArray mediaList = entities.getJSONArray("media");
                for (int i = 0; i < mediaList.length(); i++) {
                    JSONObject media = mediaList.getJSONObject(i);
                    if (media.getString("type").equals("photo")) {
                        hasPic = true;
                        picUrl = media.getString("media_url_https");
                        break;
                    }
                }
            }

            // Reply Tweet Items
            if (isReply) {
                replyUserHandle = tweet.getString("in_reply_to_screen_name");
                replyTweetId = tweet.getLong("in_reply_to_status_id");
            }

            // Tweet URL
            tweetUrl = "https://twitter.com/"+handle+"/status/"+id;
        }

        EmbedBuilder getEmbed() {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(64, 162, 245));
            embed.setDescription(text);
            if (hasPic) {
                embed.setImage(picUrl);
            }
            embed.setAuthor(name + " (@" + handle + ")", tweetUrl, userPhoto);
            embed.setFooter("Twitter", "https://i.imgur.com/0OVsUG9.png");
            return embed;
        }

    }

}
