package lab5;

import javafx.util.Pair;

public class GetUrlTime {
    private final GetMessage msg;
    private final Integer num;

    public GetUrlTime(GetMessage msg, Integer num) {
        this.msg = msg;
        this.num = num;
    }

    public GetMessage getMessage() {
        return msg;
    }
    public Integer getNum() {
        return num;
    }
    public Integer getTime(){
        //return msg.getValue()
    }
}
