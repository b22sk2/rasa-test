package mn.unitel.solution;/*
 * @created_at 28/07/2022 6:32 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

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
        c = Multi.createFrom().ticks().every(Duration.ofMillis(70)).onItem().invoke(this::send).subscribe().with(x -> {
        });

    }

    public void push(DataStore x) {
        queue.add(x);
    }

    private static final Logger logger = Logger.getLogger("rasa");

    void send() {

        if (!queue.isEmpty()) {
            long start = System.currentTimeMillis();
            //System.out.println(queue.size());
            DataStore dataStore = queue.poll();
            rasaClient.send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);
            logger.infov("total rasa answer duration = {0} queue size = {1}", System.currentTimeMillis() - start, queue.size());
        }

    }

    public void startSendingRasa() {

    }

    public void stopSendingRasa() {
    }
}
