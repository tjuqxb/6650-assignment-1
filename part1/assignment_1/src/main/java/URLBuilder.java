public class URLBuilder {
    String baseURL;

    public URLBuilder(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * create a skier post url based on input information
     *
     * @param resortId the resort ID
     * @param seasonId the season ID
     * @param dayId the day ID
     * @param skierId the skierID
     * @return a skier post url based on the information
     */
    public String createSkierPostURL(Integer resortId, Integer seasonId, Integer dayId, Integer skierId) {
        // refer: /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        return baseURL + "/skiers/" + resortId + "/seasons/" + seasonId + "/days/" + dayId + "/skiers/" + skierId;
    }
}
