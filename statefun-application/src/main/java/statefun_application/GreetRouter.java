package statefun_application;

import org.apache.flink.statefun.sdk.io.Router;

public class GreetRouter implements Router<GreetRequest> {
    @Override
    public void route(GreetRequest message, Downstream<GreetRequest> downstream) {
        downstream.forward(StringStatefulFunction.TYPE, message.getWho(), message);
    }
}
