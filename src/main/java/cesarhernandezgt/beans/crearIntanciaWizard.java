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
import cesarhernandezgt.dto.InstanceDto;
import cesarhernandezgt.dto.Server;
import cesarhernandezgt.dto.ServerXml;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import tfactory.server.jpa.entity.ServerAgent;
import tfactory.server.jpa.entity.ServerInstance;
import tfactory.server.jpa.entity.pk.ServerInstancePK;
import tfactory.server.jpa.exception.TFactoryJPAException;
import tfactory.server.jpa.service.GenericService;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


@ManagedBean
@ViewScoped
public class crearIntanciaWizard implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Bean useful for the wizard: Create new Instance on remote server.
	private String ubicStandardTomcatSeleccionada;
	private String plantillaInstanciaSeleccionada;
	private String versionJavaSeleccionada;
	private String nombreInstanciaCrear;
	private String nameSelectedServer;
	
	private int cantidadHeapSeleccionada;
	private int cantidadPermSeleccionada;
	private int puertoHttp;
	private int puertoAjp;
	private int puertoShutdown;
	private int puertoRedirect;
	private int puertoJmx;
	
	/**
	 * Oject containing all messages from messages_*_*.properties. 
	 */
	@ManagedProperty("#{msg}")
	private ResourceBundle msgProperties;
	
	
	private String mjgDialog = "";
	
	// Application Bean Injection
	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean app;
	
	
	//Injeccion de Bean de AgenteRestClient
//	@ManagedProperty(value = "#{agenteRestClient}") no se dejo inyectar poque tiene un escope (request) menor a ViewScoped
	private AgentRestClient agenteRestclientSvc = new AgentRestClient();

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
	 * This method keeps traking of the forward or backward events inside the wizard.
	 * @param event
	 * @return
	 */
	public String onFlowProcess(FlowEvent event) {
        String siguientePaso = event.getNewStep();
		System.out.println("-|-Next Wizard Step: "+ siguientePaso);
		
		/*OBTAIN AVAILABLE PORTS ON REMOTE SERVER*/
		if(siguientePaso.equalsIgnoreCase("configuracion")){
			mjgDialog = "";
			
			if (!obtainFreePortsOnRemoteServer()){
				return "datosGenerales";
			}
		}
	    return siguientePaso;
    }

	
	
	/**
	 * Obtain 5 available ports on remote server and fillout the atributs
	 * of the InstanceDto class: HTTP, AJP, SHUTDOWN, Redirect and JXM.
	 * Also modified the mjgDialog if an error araise.
	 * 
	 * @return True if the 5 ports were obtained successfully, otherwise
	 *         rertun False.
	 */
	private boolean obtainFreePortsOnRemoteServer() {
		System.out.println("\n    We enter to SubStep Configuration.");
		System.out.println("      Obtaining available ports from server: "+nameSelectedServer);
		
		List<InstanceDto> instanciasDtoDelServidor = null;
		
		//obtenmos servidorDto en cuestion y luego su List de InstanciasDto
		if (app.getListaServidores()!=null){			
			for (Server servidor : app.getListaServidores()) {
				if(servidor.getId().equalsIgnoreCase(nameSelectedServer)){
					instanciasDtoDelServidor = servidor.getListInstanceDto();
					break;
				}
			}
			
			List<Integer> puertosAexcluir = new ArrayList<Integer>();
			if(instanciasDtoDelServidor != null && instanciasDtoDelServidor.size() > 0){
			
					//we obtain all the used port from the Instances already register from the remote server
					 for (InstanceDto instancia : instanciasDtoDelServidor) {
						 puertosAexcluir.add(instancia.getServerXml().getHttp());
						 puertosAexcluir.add(instancia.getServerXml().getHttpRedirect());
						 puertosAexcluir.add(instancia.getServerXml().getAjp());
						 puertosAexcluir.add(instancia.getServerXml().getAjpRedirect());
						 puertosAexcluir.add(instancia.getServerXml().getShutDown());
						 //puertosAexcluir.add(instancia.getServerXml().getJmx());//not implemented yet
					}
			}else {
				System.out
						.println(" Info: The remote server dosen't have any previos register Instances, so we continue to look for his available ports");
			}
			
			//call the WS Rest in order to obtain 5 available ports from the remote server. 
			List<Integer> listaPuertosDisponibles = agenteRestclientSvc
					.obtain5AvailablePorts(nameSelectedServer,
							app.getPuertosRangoInical(),
							app.getPuertosRangoFinal(), puertosAexcluir);

			if (listaPuertosDisponibles != null) {
				if (listaPuertosDisponibles.size() == 5) {
					System.out.println("5 available ports were found successfully: "+listaPuertosDisponibles);

					//Populate the Wizard UI values with the 5 port returned by the REST WS.
					puertoHttp = listaPuertosDisponibles.get(0);
					puertoAjp = listaPuertosDisponibles.get(1);
					puertoShutdown = listaPuertosDisponibles.get(2);
					puertoRedirect = listaPuertosDisponibles.get(3);
					puertoJmx = listaPuertosDisponibles.get(4);
					// 
					return true;
				} else {
					// Warning listaPuertos contains less than 5 avilable ports.
					System.out.println("The port list only has <"+ listaPuertosDisponibles.size() +"> available ports and should be 5.");
					mjgDialog = msgProperties.getString("NoEnoughAvailablePortError")+" <"+ listaPuertosDisponibles.size() +">.";
					return false;
				}
			} else {
				// ERROR listaPuertos es null, hubo error al intentar
				// consumir el servicio Rest.
				System.out.println("Port List is NULL on server: "+nameSelectedServer);
				mjgDialog = msgProperties.getString("CannotConnectWithRemoteServer");
				return false;
			}				
		}else{
			//ERROR
			System.out.println("There is not Server List on memory");
			mjgDialog = msgProperties.getString("ErrorRetreivingServerList");
			return false;
		}
	}


	
	/**
	 * This method create, upload, unzip a new Tomcat Instance on Remote Server.
	 * @return Page name for JSF Navigation to continue.
	 */
	public String btnCrearInstanciaEnServerRemoto() {
		//variables used for server information modal panel show/hide vehaviour
				boolean uploadExitoso = false;
				boolean unZipExitos = false;
				boolean cambiosAplicadosExitosos = false;
				RequestContext context = RequestContext.getCurrentInstance();
				
				File archivo = obtenerPlantillaTomcatZip(plantillaInstanciaSeleccionada);
				
				//Find serveDto and then his InstancesDto ArrayList
				GenericService<ServerAgent> serverService = GenericService.of(ServerAgent.class);
				Optional<ServerAgent> optionalServer = serverService.findByPk(nameSelectedServer);
				if(!optionalServer.isPresent())
				{
					context.addCallbackParam("operacionExitosa", false);
					agregarMensaje(MessageFormat.format(msgProperties.getString("serverNotFound"),nameSelectedServer),FacesMessage.SEVERITY_ERROR);
					return null;
				}
				ServerAgent servidorEnCuestion = optionalServer.get();

				//We check if the instance is registred in the system.
				GenericService<ServerInstance> instanceService = GenericService.of(ServerInstance.class);
				ServerInstancePK instancePK = new ServerInstancePK(servidorEnCuestion.getPath(), ubicStandardTomcatSeleccionada+"/"+nombreInstanciaCrear);
				Optional<ServerInstance> optionalInstance = instanceService.findByPk(instancePK);

				if(optionalInstance.isPresent()){
					context.addCallbackParam("operacionExitosa", false);
					agregarMensaje( msgProperties.getString("InstanceAlreadyExistError")+": "+ubicStandardTomcatSeleccionada+"/"+nombreInstanciaCrear,FacesMessage.SEVERITY_ERROR);
					return null;
				}


				//Start the interactions with the Agent at the remote server.
				if (archivo != null) {
					

					//upload the base instance zip to the remote server via the agent.
					uploadExitoso = agenteRestclientSvc.enviarInstanciaAServidorRemoto(nameSelectedServer , archivo, ubicStandardTomcatSeleccionada);
					
					if (uploadExitoso) {
						//extracting only the instance template name: instanceTemplates/apache-tomcat-x.x.x.zip ([n-1] = apache-tomcat-x.x.x.zip)
						String nombreDelZipRemoto[] = plantillaInstanciaSeleccionada.split("/");
						
						 unZipExitos = agenteRestclientSvc.descompresionRenombradoRemoto(nameSelectedServer,ubicStandardTomcatSeleccionada,nombreDelZipRemoto[nombreDelZipRemoto.length-1], nombreInstanciaCrear);
						 
						 if(unZipExitos){
							 InstanceDto nuevaInstanceDto= new InstanceDto();
							 ServerXml nuevoServerXml = new ServerXml();
							 nuevoServerXml.setHttp(puertoHttp);
							 nuevoServerXml.setHttpRedirect(puertoRedirect);
							 nuevoServerXml.setAjp(puertoAjp);
							 nuevoServerXml.setAjpRedirect(puertoRedirect);
							 nuevoServerXml.setShutDown(puertoShutdown);
							 //No implemented yet: set Memory parameters y  JMX port
							 nuevaInstanceDto.setStatus("0");//nice state ;).
							 nuevaInstanceDto.setName(nombreInstanciaCrear);
							 nuevaInstanceDto.setPathLocation(ubicStandardTomcatSeleccionada+"/"+nombreInstanciaCrear	);
							 nuevaInstanceDto.setServerXml(nuevoServerXml);	
							 
							 
							 cambiosAplicadosExitosos = agenteRestclientSvc.actualizaArchivoServerXmlRemoto(nameSelectedServer, ubicStandardTomcatSeleccionada, nombreInstanciaCrear, nuevoServerXml);
							 if (cambiosAplicadosExitosos) {

								 ServerInstance newInstance = ServerInstance.from(nuevaInstanceDto);
								 //set server agent path on the server instance, if not, JPA will fail.
								 newInstance.setServerAgent(servidorEnCuestion.getPath());
								 try
								 {
									 //register the new Instance in the Intance array list
									 servidorEnCuestion.addInstance(newInstance);

									 //update database
									 GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);
									 service.updateEntity(servidorEnCuestion);

									 System.out.println("Full success, new intance was created and registred." + nuevaInstanceDto.getPathLocation());
								 }
								 catch(TFactoryJPAException ex)
								 {
									 context.addCallbackParam("operacionExitosa", true);
									 agregarMensaje(msgProperties.getString("IntanceCreatedButNotRegistred")+nuevaInstanceDto.getPathLocation(),FacesMessage.SEVERITY_WARN);
								 }
							 } else {
								context.addCallbackParam("operacionExitosa", cambiosAplicadosExitosos);
								agregarMensaje(msgProperties.getString("ErrorUpdatingServerXmlFile")+nombreDelZipRemoto[1]+" en servidor remoto: "+nameSelectedServer,FacesMessage.SEVERITY_ERROR);
							 }
						 }else{
							context.addCallbackParam("operacionExitosa", unZipExitos);
							agregarMensaje(msgProperties.getString("InstanceCanNotBeUnzippedError")+nombreDelZipRemoto[1]+" server: "+nameSelectedServer,FacesMessage.SEVERITY_ERROR);
						 }
					}else{
						context.addCallbackParam("operacionExitosa", uploadExitoso);
						agregarMensaje(msgProperties.getString("CannotConnectWithRemoteServer")+nameSelectedServer,FacesMessage.SEVERITY_ERROR);
					}
				} else {
					uploadExitoso = false;
					context.addCallbackParam("operacionExitosa", uploadExitoso);
					agregarMensaje(msgProperties.getString("InstanceTemplatNotFound")+plantillaInstanciaSeleccionada,FacesMessage.SEVERITY_ERROR);
				}
				
		//seteamos mensajes exitoso o de error en Globo de la jsf.		
		if(uploadExitoso && unZipExitos && cambiosAplicadosExitosos){
			context.addCallbackParam("operacionExitosa", true);
			agregarMensaje(msgProperties.getString("IntanceCreatedSuccessfully"),FacesMessage.SEVERITY_INFO);
			
			return "instances.xhtml";
		}else{
			System.out.println(" Error trying create instance:<"+ubicStandardTomcatSeleccionada+"/"+nombreInstanciaCrear+"> On remote server: "+nameSelectedServer);
			context.addCallbackParam("operacionExitosa", uploadExitoso);
			agregarMensaje(msgProperties.getString("InstanceCanNotBeCreated"),FacesMessage.SEVERITY_ERROR);
			
			return null;
		}
	}
	
	

	/**
	 * return the zip template containing the instance that will be sended to remote server.
	 * @param pPlantillaUbicacion Template location.
	 * @return File containging the zip.
	 */
	public File obtenerPlantillaTomcatZip(String pPlantillaUbicacion){
		File archivo = new File(pPlantillaUbicacion);

		if (archivo.exists() && archivo.isFile()) {
			return archivo;
		} else {
			return null;
		}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	public ResourceBundle getMsgProperties() {
		return msgProperties;
	}


	public void setMsgProperties(ResourceBundle msgProperties) {
		this.msgProperties = msgProperties;
	}
	public String getUbicStandardTomcatSeleccionada() {
		return ubicStandardTomcatSeleccionada;
	}



	public void setUbicStandardTomcatSeleccionada(
			String ubicStandardTomcatSeleccionada) {
		this.ubicStandardTomcatSeleccionada = ubicStandardTomcatSeleccionada;
	}



	public String getPlantillaInstanciaSeleccionada() {
		return plantillaInstanciaSeleccionada;
	}



	public void setPlantillaInstanciaSeleccionada(
			String plantillaInstanciaSeleccionada) {
		this.plantillaInstanciaSeleccionada = plantillaInstanciaSeleccionada;
	}



	public String getVersionJavaSeleccionada() {
		return versionJavaSeleccionada;
	}



	public void setVersionJavaSeleccionada(String versionJavaSeleccionada) {
		this.versionJavaSeleccionada = versionJavaSeleccionada;
	}



	public String getNombreInstanciaCrear() {
		return nombreInstanciaCrear;
	}



	public void setNombreInstanciaCrear(String nombreInstanciaCrear) {
		this.nombreInstanciaCrear = nombreInstanciaCrear;
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getNameSelectedServer() {
		return nameSelectedServer;
	}


	public void setNameSelectedServer(String nameSelectedServer) {
		this.nameSelectedServer = nameSelectedServer;
	}


	public int getCantidadHeapSeleccionada() {
		return cantidadHeapSeleccionada;
	}


	public void setCantidadHeapSeleccionada(int cantidadHeapSeleccionada) {
		this.cantidadHeapSeleccionada = cantidadHeapSeleccionada;
	}


	public int getCantidadPermSeleccionada() {
		return cantidadPermSeleccionada;
	}


	public void setCantidadPermSeleccionada(int cantidadPermSeleccionada) {
		this.cantidadPermSeleccionada = cantidadPermSeleccionada;
	}


	public ApplicationBean getApp() {
		return app;
	}


	public void setApp(ApplicationBean app) {
		this.app = app;
	}

	public int getPuertoHttp() {
		return puertoHttp;
	}

	public void setPuertoHttp(int puertoHttp) {
		this.puertoHttp = puertoHttp;
	}

	public int getPuertoAjp() {
		return puertoAjp;
	}

	public void setPuertoAjp(int puertoAjp) {
		this.puertoAjp = puertoAjp;
	}

	public int getPuertoShutdown() {
		return puertoShutdown;
	}

	public void setPuertoShutdown(int puertoShutdown) {
		this.puertoShutdown = puertoShutdown;
	}

	public int getPuertoRedirect() {
		return puertoRedirect;
	}

	public void setPuertoRedirect(int puertoRedirect) {
		this.puertoRedirect = puertoRedirect;
	}

	public int getPuertoJmx() {
		return puertoJmx;
	}

	public void setPuertoJmx(int puertoJmx) {
		this.puertoJmx = puertoJmx;
	}

	public String getMjgDialog() {
		return mjgDialog;
	}

	public void setMjgDialog(String mjgDialog) {
		this.mjgDialog = mjgDialog;
	}




}
