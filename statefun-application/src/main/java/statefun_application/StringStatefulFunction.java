package statefun_application;

import org.apache.flink.statefun.sdk.Context;
import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.StatefulFunction;
import org.apache.flink.statefun.sdk.annotations.Persisted;
import org.apache.flink.statefun.sdk.state.PersistedValue;

public class StringStatefulFunction implements StatefulFunction {

    public static final FunctionType TYPE = new FunctionType("apache", "greeter");

    @Persisted
    private final PersistedValue<Integer> seenCount = PersistedValue.of("seen-count", Integer.class);

    @Override
    public void invoke(Context context, Object input) {
        GreetRequest request = (GreetRequest) input;
        GreetResponse response = computePersonalizedGreeting(request);
        context.send(PravegaIO.EGRESS_ID, response);
    }

    private GreetResponse computePersonalizedGreeting(GreetRequest greetMessage) {
        final String name = greetMessage.getWho();
        final int seen = seenCount.getOrDefault(0);
        seenCount.set(seen + 1);
        String greeting = greetText(name, seen);
        return new GreetResponse(name, greeting);
    }

    private static String greetText(String name, int seen) {
        switch (seen) {
            case 0:
                return String.format("Hello %s ! \uD83D\uDE0E", name);
            case 1:
                return String.format("Hello again %s ! \uD83E\uDD17", name);
            case 2:
                return String.format("Third time is a charm! %s! \uD83E\uDD73", name);
            case 3:
                return String.format("Happy to see you once again %s ! \uD83D\uDE32", name);
            default:
                return String.format("Hello at the %d-th time %s \uD83D\uDE4C", seen + 1, name);
        }
    }


}
