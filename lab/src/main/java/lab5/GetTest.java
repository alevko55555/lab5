package lab5;

import javafx.util.Pair;

public class GetTest {
    private final Pair<String, Integer> msgPair;

    public GetTest(Pair<String, Integer> pair) {
        this.msgPair = pair;
    }

    public Pair<String, Integer> getMsgPair(){
        return msgPair;
    }
    public String getUrl() {
        return  msgPair.getKey();
    }
    public Integer getNum() {
        return  msgPair.getValue();
    }
}
