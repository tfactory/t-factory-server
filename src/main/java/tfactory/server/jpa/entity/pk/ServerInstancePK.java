package tfactory.server.jpa.entity.pk;

import java.io.Serializable;

/**
 * Identifier for ServerInstance entity.
 * This is required since ServerInstance entity has a composite PK.
 *
 * Created by aalopez on 4/30/16.
 */
public class ServerInstancePK implements Serializable {
    private String serverAgent;
    private String pathLocation;

    public String getServerAgent() {
        return serverAgent;
    }

    public void setServerAgent(String serverAgent) {
        this.serverAgent = serverAgent;
    }

    public String getPathLocation() {
        return pathLocation;
    }

    public void setPathLocation(String pathLocation) {
        this.pathLocation = pathLocation;
    }
}
