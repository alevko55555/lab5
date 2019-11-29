package lab5;

import java.util.Optional;

public class MessageUrlTime {
    private final GetUrlTime result;

    public MessageUrlTime(GetUrlTime result) {
        this.result = result;
    }

    public Optional<GetUrlTime> getUrlTimeOptional() {
        if (result.getTest() != null) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
