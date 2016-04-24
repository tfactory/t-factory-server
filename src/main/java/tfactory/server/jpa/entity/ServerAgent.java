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

    /**
     * Path to the agent on a remote server
     */
    @Id
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

        return server;
    }
}
