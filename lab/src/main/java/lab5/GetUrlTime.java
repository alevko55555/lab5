package lab5;

import javafx.util.Pair;

public class GetUrlTime {
    private final GetTest test;
    private final Integer num;

    public GetUrlTime(GetTest test, Integer num) {
        this.test = test;
        this.num = num;
    }

    public GetTest getTest() {
        return test;
    }
    public Integer getNum() {
        return num;
    }
}
