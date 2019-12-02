package lab5;

import javafx.util.Pair;

public class GetUrlTime {
    private final Pair<String, Pair<Integer, Integer>> pair;

    public GetUrlTime(Pair<String, Pair<Integer, Integer>> pair) {
        this.pair = pair;
    }

    public String getUrl() {
        return  pair.getKey();
    }

    public Integer getCount() {
        return pair.getValue().getKey();
    }

    public Integer getTime() {
        return pair.getValue().getKey();
    }
}
