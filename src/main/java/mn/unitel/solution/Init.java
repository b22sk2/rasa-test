package mn.unitel.solution;/*
 * @created_at 28/07/2022 6:32 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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
       /* c = Multi.createFrom().ticks().every(Duration.ofMillis(10)).onItem().call(this::send).subscribe().with(x -> {
        });*/

        Cancellable c = Multi.createBy().repeating().uni(this::send).withDelay(Duration.ofMillis(10)).indefinitely().subscribe().with(System.out::println);

    }

    public void push(DataStore x) {
        queue.add(x);
    }

    private static final Logger logger = Logger.getLogger("rasa");

    Uni send() {

        if (!queue.isEmpty()) {
            long start = System.currentTimeMillis();
            //System.out.println(queue.size());


            DataStore dataStore = queue.poll();
            int size = queue.size();
            logger.infov("total rasa answer size = {0}", size);
            try {
                return rasaClient.send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);
                //Uni.createFrom().item(dataStore).onItem().transform(x -> rasaClient.send(x.getValue(), x.sha1, x.sha256)).subscribe().with(System.out::println);
            } catch (Exception ex) {
                ex.printStackTrace();
                return Uni.createFrom().nullItem();
            }

            // logger.infov("total rasa answer duration = {0} queue size = {1}", System.currentTimeMillis() - start, size);
        }

        return Uni.createFrom().nullItem();
    }


}
