package mn.unitel.solution;/*
 * @created_at 28/07/2022 6:32 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import mn.unitel.solution.Client.Handover;
import mn.unitel.solution.Client.RasaClient;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@ApplicationScoped
public class Init {
    Cancellable c;
    Queue<DataStore> queue;

    @Inject
    @RestClient
    Handover handover;
    Map<String, OkHttpClient> clientMap;
    OkHttpClient client;
    Pages pages;
    PageInfo info;
    Map<String, PageInfo> pagesInfo;

    public Map<String, PageInfo> getPagesInfo() {
        return pagesInfo;
    }

    ;

    public boolean getLoaded() {
        return loaded;
    }

    String sendMsg = "{\n  \"sender\": \"%s\",\n  \"message\": \"%s\",\n  \"metadata\": \"\"\n}";
    String handoverRequest = "{\n    \"recipient\": {\"id\": %s },\n    \"target_app_id\": \"371291917550\",\n    \"metadata\": \"Talk to an agent\"\n   }";
     boolean loaded = false;

    void onStart(@Observes StartupEvent ev) {
        clientMap = new HashMap<>();
        client = new OkHttpClient().newBuilder().build();
        queue = new LinkedList<>();

        //    startSending();
        readConfiguration();
        loaded = true;
      //  startSending();
        logger.info("read config");

    }

    void startSending() {
        Multi.createFrom().ticks().every(Duration.ofMillis(10)).call(x -> this.send()).onFailure().recoverWithCompletion().subscribe().with(System.out::println);
        // c = Multi.createBy().repeating().uni(this::send).withDelay(Duration.ofMillis(10)).indefinitely().subscribe().with(System.out::println);
    }

    void stopSending(String unitel) {
        c.cancel();

    }

    public void push(DataStore x) {
        queue.add(x);
    }

    Map<String, RasaClient> httpClients;

    public Pages readConfiguration() {

        try {
            File file = new File("Config.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Pages.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Pages que = (Pages) jaxbUnmarshaller.unmarshal(file);
            pagesInfo = new HashMap<>();
            httpClients = new HashMap<>();
            que.getPage().forEach(x -> {
                pagesInfo.put(x.id, x);
                System.out.println(x);
                try {

                    httpClients.put(x.id, RestClientBuilder.newBuilder().baseUri(URI.create(x.url)).build(RasaClient.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }


            });


            return que;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static final Logger logger = Logger.getLogger("rasa");

    Uni<String> send() {
        logger.infov("queue size = {0}", queue.size());

        if (!queue.isEmpty()) {

            DataStore dataStore = queue.poll();
            if (dataStore.getRecipientId() == null) {
                return Uni.createFrom().nullItem();
            }
            PageInfo pageInfo = pagesInfo.get(dataStore.getRecipientId());
            if (pageInfo.maintenanceMode.equals("on")) {
                logger.info("maintenanceMode is on");
                return handleMaintenanceMode(dataStore, pageInfo);
            } else {
                logger.info("maintenanceMode is off");
                try {
                    logger.info("called rasaClient send");
                    //client.newCall(new Request().newBuilder().build().url(dataStore.))
                    PageInfo info = pagesInfo.get(dataStore.recipientId);

                    return RestClientBuilder.newBuilder().baseUri(URI.create(info.url)).build(RasaClient.class).send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.info("failed to call rasa client");
                    return Uni.createFrom().nullItem();
                }
            }


        }
        return Uni.createFrom().nullItem();
    }

    private Uni<String> handleMaintenanceMode(DataStore dataStore, PageInfo pageInfo) {
        if (pageInfo.operation.equals("1")) {
            handover.send(pageInfo.accessToken, String.format(handoverRequest, dataStore.senderId));
            httpClients.get(dataStore.recipientId).send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);
            logger.info("called handoverAPI & rasaClient");
        } else {
            httpClients.get(dataStore.recipientId).send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);
            logger.info("called rasaClient");
        }

        return Uni.createFrom().nothing();
    }

    public String changeMode(String id, String mode) {
        pagesInfo.get(id).setMaintenanceMode(mode);
        return pagesInfo.get(id).getMaintenanceMode();

    }

    public String check(String id) {

        return pagesInfo.get(id).getMaintenanceMode();

    }


}
