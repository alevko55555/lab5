package lab5;

import javafx.util.Pair;

public class GetTest {
    //private final Pair<String, Integer> msgPair;
    private final String url;
    private final Integer num;

    public GetTest(String url, Integer num) {
        this.url = url;
        this.num = num;
    }

//    public Pair<String, Integer> getMsgPair(){
//        return msgPair;
//    }
    public String getUrl() {
        return  url;
    }
    public Integer getNum() {
        return  num;
    }
}
