package statefun_application;

import io.pravega.client.ClientConfig;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.connectors.flink.FlinkPravegaReader;
import io.pravega.connectors.flink.FlinkPravegaWriter;
import io.pravega.connectors.flink.PravegaConfig;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.statefun.flink.io.datastream.SinkFunctionSpec;
import org.apache.flink.statefun.flink.io.datastream.SourceFunctionSpec;
import org.apache.flink.statefun.sdk.io.EgressIdentifier;
import org.apache.flink.statefun.sdk.io.EgressSpec;
import org.apache.flink.statefun.sdk.io.IngressIdentifier;

import java.io.*;
import java.net.URI;

public class PravegaIO {

    public static final IngressIdentifier<GreetRequest> INGRESS_ID = new IngressIdentifier<>(GreetRequest.class, "example", "users");
    public static final EgressIdentifier<GreetResponse> EGRESS_ID = new EgressIdentifier<>("example", "user", GreetResponse.class);

    private final FlinkPravegaReader<GreetRequest> pravegaSource;
    private final FlinkPravegaWriter<GreetResponse> pravegaSink;
    private StreamManager streamManager;
    private StreamConfiguration streamConfig;
    private PravegaConfig pravegaConfig;
    private final ClientConfig clientConfig;

    public PravegaIO(String streamName, String scope, String controllerUri) {

        this.pravegaConfig = PravegaConfig.fromDefaults()
                .withControllerURI(URI.create(controllerUri))
                .withDefaultScope(scope);

        this.clientConfig = ClientConfig.builder()
                .controllerURI(URI.create(controllerUri))
                .build();

        this.streamManager = StreamManager.create(clientConfig);
        streamManager.createScope(scope);

        this.streamConfig = StreamConfiguration.builder().build();
        streamManager.createStream(scope, streamName, streamConfig);

        this.pravegaSource = FlinkPravegaReader.<GreetRequest>builder()
                .forStream(streamName)
                .withPravegaConfig(pravegaConfig)
                .withDeserializationSchema(new requestDeserializationSchema())
                .build();
        this.pravegaSink = FlinkPravegaWriter.<GreetResponse>builder()
                .forStream(streamName)
                .withPravegaConfig(pravegaConfig)
                .withSerializationSchema(new responseSerializationSchema())
                .withEventRouter(event -> "fixedkey")
                .build();
    }

    SourceFunctionSpec<GreetRequest> getIngressSpec() {
        return new SourceFunctionSpec<>(INGRESS_ID, pravegaSource);
    }

    EgressSpec<GreetResponse> getEgressSpec() {
        return new SinkFunctionSpec<>(EGRESS_ID, pravegaSink);
    }

    private static class requestDeserializationSchema extends AbstractDeserializationSchema<GreetRequest> {
        @Override
        public GreetRequest deserialize(byte[] message) throws IOException {
            GreetRequest request = null;
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(message);
                ois = new ObjectInputStream(bais);
                request = (GreetRequest) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    if (ois != null) {
                        ois.close();
                    }
                    if (bais != null) {
                        bais.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return request;
        }

        @Override
        public boolean isEndOfStream(GreetRequest nextElement) {
            return false;
        }
    }

    private static class responseSerializationSchema implements SerializationSchema<GreetResponse> {

        @Override
        public byte[] serialize(GreetResponse greetResponse) {
            byte[] bytes = null;
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(greetResponse);
                oos.flush();
                bytes = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            return bytes;
        }
    }
}
