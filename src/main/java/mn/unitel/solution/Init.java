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
    Queue<String> queue;
    @Inject
    @RestClient
    RasaClient rasaClient;

    void onStart(@Observes StartupEvent ev) {
        queue = new LinkedList<>();
        c = Multi.createFrom().ticks().every(Duration.ofMillis(10)).onItem().invoke(this::send).subscribe().with(System.out::println);

    }

    public void push(String x) {
        queue.add(x);
    }

    void send() {

        for (int i = 0; i < 10; i++) {
            if (!queue.isEmpty())
                rasaClient.send(queue.poll());
        }
    }

    public void startSendingRasa() {

    }

    public void stopSendingRasa() {
    }
}
