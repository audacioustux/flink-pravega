package statefun_application;

public class GreetRequest {
    private String who = "1";

    public void setWho(String who) {
        this.who = who;
    }

    public String getWho() {
        return who;
    }

    public GreetRequest() {
        this.who = who;
    }

    public GreetRequest(String who) {
        this.who = who;
    }

    @Override
    public String toString() {
        return "greetRequest{" +
                "who='" + who + '\'' +
                '}';
    }
}
