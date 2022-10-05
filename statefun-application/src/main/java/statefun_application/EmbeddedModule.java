package statefun_application;
import java.util.Map;

import org.apache.flink.statefun.sdk.spi.StatefulFunctionModule;
import com.google.auto.service.AutoService;

@AutoService(StatefulFunctionModule.class)
public class EmbeddedModule implements StatefulFunctionModule {

    @Override
    public void configure(Map<String, String> globalConfiguration, Binder binder) {
        String streamName = "test";
        String scope = "test-scope";
        String controllerUri = "tcp://pravega-pravega-controller.pravega.svc.cluster.local:9090";
        PravegaIO ioModule = new PravegaIO(streamName, scope, controllerUri);
        binder.bindIngress(ioModule.getIngressSpec());
        binder.bindEgress(ioModule.getEgressSpec());
        binder.bindFunctionProvider(StringStatefulFunction.TYPE, unused -> new StringStatefulFunction());
        binder.bindIngressRouter(PravegaIO.INGRESS_ID, new GreetRouter());
    }
}