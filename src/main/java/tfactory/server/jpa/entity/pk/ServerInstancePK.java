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

    /**
     * Default constructor
     */
    public ServerInstancePK(){}

    /**
     * Creates an instance of this PK.
     * @param serverAgent ID of the server agent.
     * @param pathLocation Path location of the instance within the server agent.
     */
    public ServerInstancePK(String serverAgent, String pathLocation)
    {
        this.serverAgent = serverAgent;
        this.pathLocation = pathLocation;
    }

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
