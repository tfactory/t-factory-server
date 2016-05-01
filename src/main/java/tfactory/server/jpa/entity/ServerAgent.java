/**
 * Copyright (C) 2015 Cesar Hernandez. (https://github.com/tfactory)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tfactory.server.jpa.entity;

import cesarhernandezgt.dto.AgentDto;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

/**
 * ServerAgent Entity
 * Created by aalopez on 4/16/16.
 */
@Entity(name = "ServerAgent") //this is required for JPQL
@Table(name = "SERVER_AGENT") //this is required to map database tables
public class ServerAgent implements Serializable {
    @Id
    @Column(name="SERVER_PATH")
    private String path;

    @Column(name="HOSTNAME")
    private String hostname;

    @Column(name="STATUS")
    private String status;

    @Column(name="VERSION")
    private String versionAgent;

    @OneToMany(cascade=ALL, fetch=EAGER)
    @JoinColumn(name="SERVER_PATH", nullable = false)
    private List<ServerInstance> instances;

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
     * Instances of this Server.
     * @return List of registered instances for this server.
     */
    public List<ServerInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ServerInstance> instances) {
        this.instances = instances;
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
