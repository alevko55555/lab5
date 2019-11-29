package lab5;

import javafx.util.Pair;

public class GetUrlTime {
    private final GetTest msg;
    private final Integer num;

    public GetUrlTime(GetTest msg, Integer num) {
        this.msg = msg;
        this.num = num;
    }

    public GetTest getMessage() {
        return msg;
    }
    public Integer getNum() {
        return num;
    }
}
