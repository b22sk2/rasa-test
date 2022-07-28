package mn.unitel.solution;/*
 * @created_at 28/07/2022 6:32 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

@ApplicationScoped
public class Init {
    public Cancellable c;
    Queue<DataStore> queue;
    @Inject
    @RestClient
    RasaClient rasaClient;

    void onStart(@Observes StartupEvent ev) {
        queue = new LinkedList<>();
        c = Multi.createFrom().ticks().every(Duration.ofMillis(70)).onItem().invoke(this::send).subscribe().with(System.out::println);

    }

    public void push(DataStore x) {
        queue.add(x);
    }

    void send() {

        if (!queue.isEmpty()) {
            DataStore dataStore = queue.poll();
            System.out.println(rasaClient.send(dataStore.getValue(), dataStore.sha1, dataStore.sha256));
        }

    }

    public void startSendingRasa() {

    }

    public void stopSendingRasa() {
    }
}
