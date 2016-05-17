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

import cesarhernandezgt.dto.InstanceDto;
import tfactory.server.jpa.entity.pk.ServerInstancePK;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Server Instance entity.
 * Created by aalopez on 4/30/16.
 */
@IdClass(ServerInstancePK.class)
@Entity(name = "ServerInstance") //this is required for JPQL
@Table(name = "SERVER_INSTANCE") //this is required to map database tables
public class ServerInstance implements Serializable {

    @Id
    @Column(name = "SERVER_PATH")
    private String serverAgent;

    @Id
    @Column(name = "PATH_LOCATION")
    private String pathLocation;

    @Column(name = "NAME")
    private String name;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SHUTDOWN_PORT")
    private int shutDown;

    @Column(name = "HTTP_PORT")
    private int http;

    @Column(name = "HTTP_REDIRECT_PORT")
    private int httpRedirect;

    @Column(name = "AJP_REDIRECT_PORT")
    private int ajpRedirect;

    @Column(name = "AJP_PORT")
    private int ajp;

    /**
     * ServerAgent this instance belongs to.
     *
     * @return Server Path this instance belongs to.
     */
    public String getServerAgent() {
        return serverAgent;
    }

    public void setServerAgent(String serverAgent) {
        this.serverAgent = serverAgent;
    }

    /**
     * Instance location within the server.
     *
     * @return Instance location path.
     */
    public String getPathLocation() {
        return pathLocation;
    }

    public void setPathLocation(String pathLocation) {
        this.pathLocation = pathLocation;
    }

    /**
     * Instance name.
     *
     * @return Instance name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Status.
     *
     * @return Instance status.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Shutdown port.
     *
     * @return Shutdown port.
     */
    public int getShutDown() {
        return shutDown;
    }

    public void setShutDown(int shutDown) {
        this.shutDown = shutDown;
    }

    /**
     * HTTP port.
     *
     * @return HTTP port.
     */
    public int getHttp() {
        return http;
    }

    public void setHttp(int http) {
        this.http = http;
    }

    /**
     * HTTP redirect port.
     *
     * @return HTTP redirect port.
     */
    public int getHttpRedirect() {
        return httpRedirect;
    }

    public void setHttpRedirect(int httpRedirect) {
        this.httpRedirect = httpRedirect;
    }

    /**
     * AJP redirect port.
     *
     * @return AJP redirect port.
     */
    public int getAjpRedirect() {
        return ajpRedirect;
    }

    public void setAjpRedirect(int ajpRedirect) {
        this.ajpRedirect = ajpRedirect;
    }

    /**
     * AJP Port.
     *
     * @return AJP Port.
     */
    public int getAjp() {
        return ajp;
    }

    public void setAjp(int ajp) {
        this.ajp = ajp;
    }

    @Override
    public int hashCode() {
        int code = 33;
        code *= this.getServerAgent() != null ? this.getServerAgent().hashCode() : 1;
        code *= this.getPathLocation() != null ? this.getPathLocation().hashCode() : 1;
        return code;
    }

    /**
     * Checks whether this is equal to another object.
     * Result is based on {@link #getServerAgent()} and {@link #getPathLocation()}.
     *
     * @param obj Other object.
     * @return {@code true} 1. If this and other object reference are the same. 2. If server agents and instance location are the same. {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ServerInstance)) {
            return false;
        } else if (this.getServerAgent() == null || this.getPathLocation() == null) {
            return false;
        }

        ServerInstance other = (ServerInstance) obj;
        return this.getServerAgent().equals(other.getServerAgent()) && this.getPathLocation().equals(other.getPathLocation());
    }


    /**
     * Synchronizes this entity instance with the dto param.
     *
     * @param dto Where the information comes from.
     */
    public void sync(@NotNull InstanceDto dto) {
        this.setStatus(dto.getStatus());
        this.setName(dto.getName());
        this.setAjp(dto.getServerXml().getAjp());
        this.setAjpRedirect(dto.getServerXml().getAjpRedirect());
        this.setHttp(dto.getServerXml().getHttp());
        this.setHttpRedirect(dto.getServerXml().getHttpRedirect());
        this.setShutDown(dto.getServerXml().getShutDown());
    }


    /**
     * Creates a new instance of the entity based on the information of the dto param.
     *
     * @param dto Where the information comes from.
     * @return New instance of the entity class based on the param's data.
     */
    public static ServerInstance from(@NotNull InstanceDto dto) {
        ServerInstance instance = new ServerInstance();
        instance.setPathLocation(dto.getPathLocation());
        instance.setStatus(dto.getStatus());
        instance.setName(dto.getName());
        if(dto.getServerXml() != null) {
            instance.setAjp(dto.getServerXml().getAjp());
            instance.setAjpRedirect(dto.getServerXml().getAjpRedirect());
            instance.setHttp(dto.getServerXml().getHttp());
            instance.setHttpRedirect(dto.getServerXml().getHttpRedirect());
            instance.setShutDown(dto.getServerXml().getShutDown());
        }

        return instance;
    }
}
