package tfactory.server.jpa.entity;

import cesarhernandezgt.dto.AgentDto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ServerAgent Entity
 * Created by aalopez on 4/16/16.
 */
@Entity(name = "ServerAgent") //this is required for JPQL
@Table(name = "SERVER_AGENT") //this is required to map database tables
public class ServerAgent {

    @Id
    private String path;

    private String hostname;

    private String status;

    private String versionAgent;

    /**
     * Path to the agent on a remote server
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Hostname.
     */
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Server status.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Server agent version.
     */
    public String getVersionAgent() {
        return versionAgent;
    }

    public void setVersionAgent(String version) {
        this.versionAgent = version;
    }

    /**
     * Creates a new instance of the entity based on the information of the AgentDto param.
     *
     * @param dto Where the information comes from.
     * @return New instance of the entity class based on the param's data.
     */
    public static ServerAgent from(AgentDto dto) {
        ServerAgent server = new ServerAgent();
        server.setPath(dto.getPath());
        server.setHostname(dto.getHostname());
        server.setStatus(dto.getStatus());
        server.setVersionAgent(dto.getVersionAgent());

        return server;
    }
}
