package mn.unitel.solution.Switchboard;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.string.StringCommands;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisService {

    private final StringCommands<String, String> commands;

    public RedisService(RedisDataSource ds) {
        commands = ds.string(String.class);
    }

    public void set(String key, String value) {
        commands.set(key, value);
    }

    public String get(String key) {
        return commands.get(key);
    }

    public void del(String key){
        commands.getdel(key);
    }
}