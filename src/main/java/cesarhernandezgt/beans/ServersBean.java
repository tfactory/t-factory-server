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
package cesarhernandezgt.beans;

import cesarhernandezgt.clientRest.AgentRestClient;
import cesarhernandezgt.dto.AgentDto;
import org.primefaces.context.RequestContext;
import tfactory.server.jpa.entity.ServerAgent;
import tfactory.server.jpa.exception.TFactoryExceptionCodeEnum;
import tfactory.server.jpa.exception.TFactoryJPAException;
import tfactory.server.jpa.service.GenericService;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@RequestScoped
public class ServersBean {

	// Attributes for adding a new server
	private String agentPathOnServer;
	private Boolean mostrarPanelGridAvanzServer = false;
	
	@ManagedProperty("#{globalConf.AgentPortNumber}") //default: 8989
	private String puertoServidorAgente;
	
	@ManagedProperty("#{globalConf.AgentRestContext}") //default: tfactory-agent/api
	private String contextoServidorAgente;

	/**
	 * Oject containing all messages from messages_*_*.properties. 
	 */
	@ManagedProperty("#{msg}")
	private ResourceBundle msgProperties;
	
	//Attribute used to store the details of a remote Server
	private ServerAgent serverSeleccionado;
	
	public ServerAgent getServerSeleccionado() {
		return serverSeleccionado;
	}

	public void setServerSeleccionado(ServerAgent serverSeleccionado) {
		this.serverSeleccionado = serverSeleccionado;
	}

	//Injection AgenteRestClient Bean
	@ManagedProperty(value = "#{agentRestClient}")
	private AgentRestClient agenteRestclientSvc;
	
	
	public void setAgenteRestclientSvc(AgentRestClient agenteRestclientSvc) {
		this.agenteRestclientSvc = agenteRestclientSvc;
	}

	//Injection Aplicacion Bean
	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean app;

	public void setApp(ApplicationBean app) {
		this.app = app;
	}

	/**
	 * Update the info globe on the web page.
	 * 
	 * @param summary
	 *            Actual message to show.
	 * @param type
	 *            Severity Type(info, warning, error).
	 */
	private void agregarMensaje(String summary, Severity type) {
		FacesMessage message = new FacesMessage(type, summary, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	/**
	 * Add a server in to the system.
	 */
	public void btnAddServer() {
		System.out.println("Starting btnAddServer() "	+ getAgentPathOnServer());
		
		//Hides the add server modal panel
		boolean operacionExitosa = false;
		RequestContext context = RequestContext.getCurrentInstance();
		
		StringBuilder target = new StringBuilder();
		
		//remove the last / of the url provided by the user.
		String strUbicacion = getAgentPathOnServer().replaceAll("/$", "");
		
		//Add HTTP  if the url provided by the user does not have it.
		if( ! strUbicacion.toUpperCase().contains("HTTP") ){
			target.append("http://");
		}
		//We build the complete URL for connecting with the Agent.
		target.append(strUbicacion);
		target.append(":");
		target.append(getPuertoServidorAgente());
		target.append("/");
		target.append(getContextoServidorAgente());
	
		
		System.out.println("Start to consume: " + target.toString());
		
		//Consume Agent Web Service Rest 
		AgentDto objAgenteDto = agenteRestclientSvc.obtainAgenteSrv(target.toString());
				
		if(objAgenteDto != null && objAgenteDto.getStatus().equalsIgnoreCase("ok")){
			
			//To the received object we add the entire path (this is the primary key of server list)
			objAgenteDto.setPath(target.toString());

			try
			{
				//Creation of ServerAgent node.
				ServerAgent serverAgent = ServerAgent.from(objAgenteDto);

				GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);
				service.persistEntity(serverAgent);

				System.out.println("Server Added Successfully <"+ target.toString() +">");
				operacionExitosa = true;
				agregarMensaje(msgProperties.getString("ServerAddedSucc")+ objAgenteDto.getHostname(), FacesMessage.SEVERITY_INFO);

			}catch(TFactoryJPAException ex)
			{
				operacionExitosa = false;
				if(ex.getExceptionCode() == TFactoryExceptionCodeEnum.CONSTRAINT_VIOLATION) {
					System.out.println("The location for the server is already registred <" + target.toString() + ">");
					agregarMensaje(msgProperties.getString("ServerAlreadyExist") + " <" + target.toString() + ">", FacesMessage.SEVERITY_ERROR);
				}
				else {
					agregarMensaje(msgProperties.getString("jpa-persist-error"), FacesMessage.SEVERITY_ERROR);
				}
			}
		}else {
			operacionExitosa = false;
			agregarMensaje(msgProperties.getString("ServerNotAddedError")+" <" +objAgenteDto.getStatus() + ">", FacesMessage.SEVERITY_ERROR);
		}
		
		// Add data to the view call back in order to JavaScript could evaluate some functions
		context.addCallbackParam("operacionExitosa", operacionExitosa);
	}
	
	
	
	/**
	 * 
	 * Method that changes the state flag: mostrarPanelGridAvanzServer to show
	 * or hide the Advanced Options section to add a server.
	 * Currently this feature dosen't work.
	 * 
	 */
	public void btnMostratPanelGridAvanzServer() {
		if (getMostrarPanelGridAvanzServer()) {
			setMostrarPanelGridAvanzServer(false);
		} else {
			setMostrarPanelGridAvanzServer(true);
		}
	}
	
	
	
	/**
	 * Method that makes a synchronization with the remote server that contains the agent.
	 */
	public void btnSincronizarServidor (){
		System.out.println("Server to be synchoronized: "+ serverSeleccionado.getPath());
		
		//We consume the Agent Rest Web Servcice 
		AgentDto objAgenteDtoAux = agenteRestclientSvc.obtainAgenteSrv(serverSeleccionado.getPath());
		
		if(objAgenteDtoAux.getStatus().equalsIgnoreCase("ok")){
			//Server data is updated
			serverSeleccionado.setStatus(objAgenteDtoAux.getStatus());
			serverSeleccionado.setHostname(objAgenteDtoAux.getHostname());
			serverSeleccionado.setVersionAgent(objAgenteDtoAux.getVersionAgent());
			agregarMensaje(msgProperties.getString("SynchSuccessfull")+": "+ serverSeleccionado.getHostname(), FacesMessage.SEVERITY_INFO);
		}else {
			serverSeleccionado.setStatus("offline");
			agregarMensaje(msgProperties.getString("SynchError")+" <" +objAgenteDtoAux.getStatus() + ">", FacesMessage.SEVERITY_ERROR);
		}

		//update database
		try
		{
			GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);
			service.updateEntity(serverSeleccionado);
		}
		catch(TFactoryJPAException ex)
		{
			agregarMensaje(String.format(msgProperties.getString("jpa-update-error"), ex.getExceptionCode()), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	
	/**
	 * Delete (deregistred) a server.
	 */
	public void btnEliminarServidor (){
		System.out.println("Deleting server: "+ serverSeleccionado.getPath());

		try
		{
			GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);
			service.removeEntity(serverSeleccionado);

			agregarMensaje(msgProperties.getString("ServerDeletedSucc")+".", FacesMessage.SEVERITY_INFO);
		}
		catch(TFactoryJPAException ex)
		{
			agregarMensaje(msgProperties.getString("ServerDeletedError")+" <" + serverSeleccionado.getHostname() + ">", FacesMessage.SEVERITY_ERROR);
		}
	}

	
	
	
	/*
	 * GETTERS AND SETTERS
	 */
	

	public String getAgentPathOnServer() {
		return agentPathOnServer;
	}

	public void setAgentPathOnServer(String agentPatnOnServer) {
		this.agentPathOnServer = agentPatnOnServer;
	}

	public Boolean getMostrarPanelGridAvanzServer() {
		return mostrarPanelGridAvanzServer;
	}

	public void setMostrarPanelGridAvanzServer(
			Boolean mostrarPanelGridAvanzServer) {
		this.mostrarPanelGridAvanzServer = mostrarPanelGridAvanzServer;
	}

	public String getPuertoServidorAgente() {
		return puertoServidorAgente;
	}

	public void setPuertoServidorAgente(String puertoServidorAgente) {
		this.puertoServidorAgente = puertoServidorAgente;
	}

	public String getContextoServidorAgente() {
		return contextoServidorAgente;
	}

	public void setContextoServidorAgente(String contextoServidorAgente) {
		this.contextoServidorAgente = contextoServidorAgente;
	}
	public ResourceBundle getMsgProperties() {
		return msgProperties;
	}

	public void setMsgProperties(ResourceBundle msgProperties) {
		this.msgProperties = msgProperties;
	}

	/**
	 * Gets the list of registered servers.
	 * @return List of {@link ServerAgent} from database.
     */
	public List<ServerAgent> getServersList()
	{
		GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);

		return service.findAll();
	}
}
