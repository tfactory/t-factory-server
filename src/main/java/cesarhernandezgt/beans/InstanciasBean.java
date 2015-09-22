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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.primefaces.context.RequestContext;

import cesarhernandezgt.clientRest.AgentRestClient;
import cesarhernandezgt.dto.InstanceDto;
import cesarhernandezgt.dto.Server;

@ManagedBean
@RequestScoped
public class InstanciasBean {

	private String instancePath;//Path of the instance to be register
	private String nameSelectedServer; //item from the Servers drop down menu 
	private List<SelectItem> serversNameList;
	
	//Used for the selection of one specific row of the Servers Table 
	private Server serverSelectedDto;
	private InstanceDto instanceSelectedDto;
	
	
	// Application Bean Injection
	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean app;

	public void setApp(ApplicationBean app) {
		this.app = app;
	}
	
	
	/**
	 * Oject containing all messages from messages_*_*.properties. 
	 */
	@ManagedProperty("#{msg}")
	private ResourceBundle msgProperties;
	
	//Bean AgentRestClient injection
	@ManagedProperty(value = "#{agentRestClient}")
	private AgentRestClient agenteRestclientSvc;

	public void setAgenteRestclientSvc(AgentRestClient agenteRestclientSvc) {
		this.agenteRestclientSvc = agenteRestclientSvc;
	}
	
	
	/**
	 * Update the icefaces growl component with messages.
	 * @param summary  Message to be showed by the globe.
	 * @param tipo     Severity of the message (info, warning, error).
	 */
	private void agregarMensaje(String summary, Severity tipo) {
		FacesMessage message = new FacesMessage(tipo, summary, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	
	
	
	
	/**
	 * Register a remote tomcat instance.
	 */
	public void btnRegistrarInstancia() {
		System.out.println("\n Start regiter a tomcat instance from the host: "+nameSelectedServer);
	
		// Variable used to hide the modal panel that contains the Server Input fields
		boolean operacionExitosa = false;
		RequestContext context = RequestContext.getCurrentInstance();
		
		System.out.println("Path of tomcat instance to be register: "+instancePath);
				
		//Consumtions of the Rest webservice (the t-agent)
		InstanceDto instanciaDtoRecibida = agenteRestclientSvc.obtainInstanceDto(nameSelectedServer, instancePath);
		
		if (instanciaDtoRecibida != null){
			System.out.println("Tomcat instance obtained "+instanciaDtoRecibida.getName() + " Status:"+instanciaDtoRecibida.getStatus());
			
			
			//We check the status of the instnaceDto object received.
			int resultado = Integer.valueOf(instanciaDtoRecibida.getStatus()) ;
			
			switch (resultado) {
			case 0:
				
				System.out.println("We start to register the instance path :"+ getInstancePath());
				System.out.println("   Server selected has the following path: "+nameSelectedServer);
				
				//The instances is in good shape, now we try to added It to the Master Array of ServerDto objects
				for (Server nodoServidor : app.getListaServidores()) {
					
					//we see if the Server already exist
					if(nodoServidor.getAgentDto().getPath().equalsIgnoreCase(nameSelectedServer)){
						
						System.out.println("The server was found in the master Server list");
						//if the instance list dosen't exist inside the server node, we create it.
						if (nodoServidor.getListInstanceDto() == null){
							System.out.println("The server don't hava a InstanciaDto list already, we procced to create It. HTTP:"+instanciaDtoRecibida.getServerXml().getHttp());
							List<InstanceDto> listaInstanciaDtoNuevo = new ArrayList <InstanceDto>();
							listaInstanciaDtoNuevo.add(instanciaDtoRecibida);
							nodoServidor.setListInstanceDto(listaInstanciaDtoNuevo);
							operacionExitosa = true;
						}else{//If the Intance list already exist inside the server node, we proceed to register the new instance into the existing Instance List.
							System.out.println("Because the Instance list already existe, we register the new IntanceDto");
							
							//check if the instances already exist.
							if (!nodoServidor.getListInstanceDto().contains(instanciaDtoRecibida)) {
								nodoServidor.getListInstanceDto().add(instanciaDtoRecibida);
								System.out.println("InstanceDto added successfully :) !!! HTTP:"+instanciaDtoRecibida.getServerXml().getHttp());
								operacionExitosa = true;
							} else {
								System.out.println("The instanceDto was Not added because a instance with the name: "+instanciaDtoRecibida.getName()+" and path: "+instanciaDtoRecibida.getPathLocation()+" already exist. ");
							}
						}
						break;
					}//if
				}//del for
				
				if (operacionExitosa){
					
					// return data to the view in order to allow JavaScrip to hide the modal panel.
					context.addCallbackParam("operacionExitosa", operacionExitosa);
					
					//Set the message with the severity to be showed in the iceface information glob.
					agregarMensaje(msgProperties.getString("InstanceRegisteredSuccessfully"),FacesMessage.SEVERITY_INFO);
				}else{
					agregarMensaje(msgProperties.getString("InstanceAlreadyExist"), FacesMessage.SEVERITY_ERROR );
				}
				break;
				
			case 1:
				agregarMensaje(msgProperties.getString("InstanceNotAddedError"),FacesMessage.SEVERITY_ERROR);
				agregarMensaje(msgProperties.getString("ToManyConectorsError"), FacesMessage.SEVERITY_WARN );
				System.out.println("The instance was not registered  (there is more than 1 Conectors apart from  HTTP and AJP on server.xml:"+ getInstancePath());
				break;
				
			case 2:
				agregarMensaje(msgProperties.getString("ServerXmlReadingError"), FacesMessage.SEVERITY_ERROR );
				break;
			case 3:
				agregarMensaje(msgProperties.getString("ServerXmlReadingErrorType3"), FacesMessage.SEVERITY_ERROR );
				break;
			case 4:
				agregarMensaje(msgProperties.getString("InstancePathError"), FacesMessage.SEVERITY_ERROR );
				break;
			case 5:
				agregarMensaje(msgProperties.getString("IntanceConfNotFoundError"), FacesMessage.SEVERITY_ERROR );
				break;
			case 6:
				agregarMensaje(msgProperties.getString("ServerXmlNotFoundError"), FacesMessage.SEVERITY_ERROR );
				break;
			case 7:
				agregarMensaje(msgProperties.getString("InstacePathErrorType7"), FacesMessage.SEVERITY_ERROR );
				break;
			}
		}else{
			agregarMensaje(msgProperties.getString("ServerConnectionError"), FacesMessage.SEVERITY_ERROR );
		}	
	}
	
	
	
	
	
	/**
	 * Synchronice a remote tomcat instance in order to obtain his configuration parameteres.
	 */
	public void btnSincronizarInstancia(){
		System.out.println("\n Intance Shyncronization Pressed");
		
		if (serverSelectedDto!=null && instanceSelectedDto!=null) {
			System.out.println("  Server selected:"+serverSelectedDto.getAgentDto().getHostname());
			System.out.println("    Selected instance is:"+instanceSelectedDto.getName());
			
			//We consume webservice Rest
			InstanceDto instanciaDtoRecibida = agenteRestclientSvc.obtainInstanceDto(serverSelectedDto.getAgentDto().getPath(), instanceSelectedDto.getPathLocation());
			
			if (instanciaDtoRecibida != null){
				System.out.println("Obtained Tomcat instance: "+instanciaDtoRecibida.getName() + " Status:"+instanciaDtoRecibida.getStatus());
				
				
				//The instances is in good shape, now we try to added It to the Master Array of ServerDto objects
				int resultado = Integer.valueOf(instanciaDtoRecibida.getStatus()) ;
				
				boolean operacionExitosa = false;
				switch (resultado) {
				case 0:
					
					System.out.println("Remote instance obtained successfully");
					//Now that we got the Instnace, we update his info inside the Instance List for the specific Server Node.
					for (Server nodoServidor : app.getListaServidores()) {
						
						if(nodoServidor.getAgentDto().getPath().equalsIgnoreCase(serverSelectedDto.getAgentDto().getPath())){

							//verifying that the Instance exist and checking that his id match the synch one ****(pathLocation is used in the equals method).***
								int indexInstancia = nodoServidor.getListInstanceDto().indexOf(instanceSelectedDto);
								if (indexInstancia > -1) {
									
									nodoServidor.getListInstanceDto().set(indexInstancia, instanciaDtoRecibida);
									System.out.println("InstanceDto  is in synch:) !!! HTTP:"+instanciaDtoRecibida.getServerXml().getHttp());
									operacionExitosa  = true;
								} else {
									System.out.println("Instance not found in the Instance array of the selected server");
									agregarMensaje(msgProperties.getString("InstanceNotFoundError"), FacesMessage.SEVERITY_ERROR );
								}
							//}
							
							break;
						}//if
					}//del for
					
					if (operacionExitosa){
						
						// return data to the view in order to allow JavaScrip to hide the modal panel.
						agregarMensaje(msgProperties.getString("InstanceSynched"),FacesMessage.SEVERITY_INFO);
					}else{
						agregarMensaje(msgProperties.getString("InstanceNotFoundError"), FacesMessage.SEVERITY_ERROR );
					}
					break;
				case 1:
					agregarMensaje(msgProperties.getString("InstanceSynchedErrorType1"),FacesMessage.SEVERITY_ERROR);
					agregarMensaje(msgProperties.getString("ToManyConectorsError"), FacesMessage.SEVERITY_WARN );
					System.out.println("The instance was not Synched (there is more than 1 Conectors apart from  HTTP and AJP on server.xml:"+ getInstancePath());
					instanceSelectedDto.setStatus("1"); //we change the status to indicate an  error (red Light)
					break;
					
				case 2:
					agregarMensaje(msgProperties.getString("ServerXmlReadingError"), FacesMessage.SEVERITY_ERROR );
					instanceSelectedDto.setStatus("2"); //we change the status to indicate an  error (red Light)
					break;
				case 3:
					agregarMensaje(msgProperties.getString("ServerXmlReadingErrorType3"), FacesMessage.SEVERITY_ERROR );
					instanceSelectedDto.setStatus("3"); //we change the status to indicate an  error (red Light)
					break;
				case 4:
					agregarMensaje(msgProperties.getString("InstancePathError"), FacesMessage.SEVERITY_ERROR );
					instanceSelectedDto.setStatus("4"); //we change the status to indicate an  error (red Light)
					break;
				case 5:
					agregarMensaje(msgProperties.getString("IntanceConfNotFoundError"), FacesMessage.SEVERITY_ERROR );
					instanceSelectedDto.setStatus("5"); //we change the status to indicate an  error (red Light)
					break;
				case 6:
					agregarMensaje(msgProperties.getString("ServerXmlNotFoundError"), FacesMessage.SEVERITY_ERROR );
					instanceSelectedDto.setStatus("6"); //we change the status to indicate an  error (red Light)
					break;
				case 7:
					agregarMensaje(msgProperties.getString("InstacePathErrorType7"), FacesMessage.SEVERITY_ERROR );
					instanceSelectedDto.setStatus("7"); //we change the status to indicate an  error (red Light)
					break;
				}
				
				
				
			}else{
				agregarMensaje(msgProperties.getString("ServerConnectionError"), FacesMessage.SEVERITY_ERROR );
				instanceSelectedDto.setStatus("-1");
			}
			
			
		} else {
			agregarMensaje(msgProperties.getString("InstanceNotFoundError"), FacesMessage.SEVERITY_ERROR );
		}
	}
	
	
	
	
	/**
	 * Unregister selected instance
	 */
	public void btnUnregisterInstance(){
		System.out.println("\n For the server: "+serverSelectedDto.getAgentDto().getHostname());
		System.out.println("       We are going to delete the image: "+instanceSelectedDto.getPathLocation());
		
		if (serverSelectedDto.getListInstanceDto().remove(instanceSelectedDto)) {
			agregarMensaje(msgProperties.getString("UnregisterInstanceSuccess"), FacesMessage.SEVERITY_INFO);
		} else {
			agregarMensaje(msgProperties.getString("UnregisterInstanceError")+ " <" +instanceSelectedDto.getName() + ">", FacesMessage.SEVERITY_ERROR);
		}
		
	}
	
	public void llenarListaServidoresNombre(){
		serversNameList = new ArrayList<SelectItem>(); 
		for (Server nodo : app.getListaServidores()) {
			serversNameList.add(new SelectItem(nodo.getAgentDto().getPath(), nodo.getAgentDto().getHostname()));
		}
	}
	
	
	
	
	/*GETTERS Y SETTERS*/

	public ResourceBundle getMsgProperties() {
		return msgProperties;
	}


	public void setMsgProperties(ResourceBundle msgProperties) {
		this.msgProperties = msgProperties;
	}


	public String getInstancePath() {
		return instancePath;
	}


	public void setInstancePath(String instancePath) {
		this.instancePath = instancePath;
	}


	

	



	public String getNameSelectedServer() {
		return nameSelectedServer;
	}


	public void setNameSelectedServer(String nameSelectedServer) {
		this.nameSelectedServer = nameSelectedServer;
	}




	public List<SelectItem> getServersNameList() {
		if(serversNameList == null){			
			llenarListaServidoresNombre();
		}
		return serversNameList;
	}


	public void setServersNameList(List<SelectItem> serversNameList) {
		this.serversNameList = serversNameList;
	}


	public Server getServerSelectedDto() {
		return serverSelectedDto;
	}


	public void setServerSelectedDto(Server serverSelectedDto) {
		this.serverSelectedDto = serverSelectedDto;
	}





	public InstanceDto getInstanceSelectedDto() {
		return instanceSelectedDto;
	}


	public void setInstanceSelectedDto(InstanceDto instanceSelectedDto) {
		this.instanceSelectedDto = instanceSelectedDto;
	}




	
}
