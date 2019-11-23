package lab5;

import javafx.util.Pair;

public class GetUrlTime {
    private Pair<String, Integer> msg;

    public GetUrlTime(Pair<String, Integer> msg) {
        this.msg = msg;
    }

    public String getUrl(){
        return msg.getKey();
    }
    public Integer getNum(){
        return  msg.getValue();
    }
    public Integer getTime(){
        //return msg.getValue()
    }
}
