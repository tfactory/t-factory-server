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
package cesarhernandezgt.clientRest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import cesarhernandezgt.dto.AgentDto;
import cesarhernandezgt.dto.InstanceDto;
import cesarhernandezgt.dto.ServerXml;

/**
 * Class used for accessing the RestFull Web Service (agent) located on the
 * remote slave servers.
 * 
 * @author cesarHernandezGt
 *
 */
@ManagedBean
@RequestScoped
public class AgentRestClient {

	private Client client = ClientBuilder.newClient();
	
	/**
	 * This method makes a GET request to the Agente Rest API. Obtain and
	 * AgentDto.java object. If the call is successfully the object status is
	 * OK, otherwise it will contain the HTTP error code obtained.
	 * 
	 * @param pTarget
	 *            Targe URL.
	 *            (http://xxx.xxx.xxx.xxx:xxxx/t-factory-agent/api)
	 * @return AgentDto object.
	 */
	public AgentDto obtainAgenteSrv(String pTarget){
		
		AgentDto objAgentDto; 
		
		try {
			WebTarget myTarget = client.target(pTarget+"/agent/");
			Invocation.Builder invocationBuilder = myTarget.request(MediaType.APPLICATION_JSON); 
			Response response = invocationBuilder.get();
			
			if( response.getStatus() == 200){
				objAgentDto = response.readEntity(AgentDto.class);
			}else{
				objAgentDto = new AgentDto();
				objAgentDto.setStatus( String.valueOf( response.getStatus()));
			}
		} catch (Exception e) {
			System.out.println("Error trying consume the service: "+pTarget);
			//e.printStackTrace();
			objAgentDto = new AgentDto();
			objAgentDto.setStatus("Invalid URL.");
		}
		
		
		return objAgentDto;
	}
	
	
	
	/**
	 * Makes a Get  request in order to obtain one tomcat instance Dto. Returns null if the instance is not found.
	 * @param pServer Usually in the form: http://xxx.xxx.xxx.xxx:xxxx/
	 * @param pInstancePath
	 * @return InstanceDto object if the instances was found, if not returns null;
	 */
	public InstanceDto obtainInstanceDto(String pServer, String pInstancePath){
		InstanceDto objInstanceDto = null; 
		System.out.println("Start the invocation of service: "+pServer+"/instance/synchronize");
		try {
			WebTarget myTarget = client.target(pServer+"/instance/synchronize");
			Invocation.Builder invocationBuilder = myTarget.request(MediaType.APPLICATION_JSON).header("instancePath", pInstancePath); 
			Response response = invocationBuilder.get();
			
			if( response.getStatus() == 200){
				objInstanceDto = response.readEntity(InstanceDto.class);
			}
		} catch (Exception e) {
			System.out.println("Error trying consume service: "+pServer);
			System.out.println(e);
		}

		return objInstanceDto;
	}	

	
	/**
	 * Makes a Get request to obtain 5 available ports on a remote server.
	 * @param pServerUrl Remote server Url where the Agente is hosted.
	 * @param pInitilaRange Initial (inclusive) range of ports to be searched at the remote server.
	 * @param pFinalRange Final (inclusive) range of ports to be searched at the remote server.
	 * @param pExcludedList Port list to be excluded from the search.
	 * @return List containing the 5 available ports.
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> obtain5AvailablePorts(String pServerUrl, int pInitilaRange, int pFinalRange, List<Integer> pExcludedList){
		System.out.println("Start invocation of service: "+pServerUrl+"/agent/obtain5AvailablePorts");
		
		try {
			WebTarget myTarget = client.target(pServerUrl+"/agent/obtain5AvailablePorts");
			WebTarget myTargetConParamas = myTarget.queryParam("initialRange", pInitilaRange).queryParam("finalRange", pFinalRange).queryParam("excluded", pExcludedList.toArray());
			Invocation.Builder invocationBuilder = myTargetConParamas.request(MediaType.APPLICATION_JSON);
			Response response = invocationBuilder.get();
			
			if( response.getStatus() == 200){
				return response.readEntity(List.class);
			}else{
				System.out.println(" ERROR, HTTP response code: "+response.getStatus());
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error invoking service: "+pServerUrl);
			System.out.println(e);
			return null;
		}
	}
	
	
	
	
	/**
	 * Sends a Multipart HTTP POST containing a Tomcat Template Instance zip format to a remote slave server.
	 * @param pUrlRemoteServer  Url for the remote slave server that host the agent.
	 * @param pTemplateZipFile Template of a tomcat instance in zip format.
	 * @param pPathAtServer Path  inside the remote slave server in which the template will be copied.
	 * @return True if success, otherwise false.
	 */
	public boolean enviarInstanciaAServidorRemoto(String pUrlRemoteServer, File pTemplateZipFile, String pPathAtServer){
		System.out.println("\nStartup of service: "+pUrlRemoteServer+"/agent/upload/instance");
		boolean finalResponse = false;
		
		ClientConfig clientConfig = null;
		WebTarget myTarget = null;
		FileDataBodyPart fileDataBodyPart = null;
	    FormDataMultiPart formDataMultiPart = null;
	    Invocation.Builder invocationBuilder = null;
        Response response = null;
        int responseCode;
        String responseMessageFromServer = null;
        String responseString = null;
        
		try {
			clientConfig = new ClientConfig();
			clientConfig.register(MultiPartFeature.class);
			client =  ClientBuilder.newClient(clientConfig);
			myTarget = client.target(pUrlRemoteServer+"/agent/upload/instance");
			
			 // set file upload values
            fileDataBodyPart = new FileDataBodyPart("uploadFile", pTemplateZipFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            formDataMultiPart = (FormDataMultiPart) new FormDataMultiPart().field("locationPath",pPathAtServer).bodyPart(fileDataBodyPart);
			
            // invoke service
            invocationBuilder = myTarget.request();
            response = invocationBuilder.post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
            
            // get response code
            responseCode = response.getStatus();
            System.out.println(" Obtained HTTP code: " + responseCode);
            
            if (response.getStatus() != 200) {
                System.out.println(" Post http failed, obtained codeo <" + responseCode + "> with message: "+response.readEntity(String.class));
                finalResponse = false;
            }else{
            	// get response message
                responseMessageFromServer = response.getStatusInfo().getReasonPhrase();
                System.out.println(" ResponseMessageFromServer: " + responseMessageFromServer);
     
                // get response string
                responseString = response.readEntity(String.class);
                System.out.println(" Response String FromServer: " + responseString);
                finalResponse = true;
            }
            
		} catch (Exception e) {
			System.out.println(" Fatal Error: "+e);
		
			finalResponse = false;
		}finally{
            // release resources, if any
            fileDataBodyPart.cleanup();
            formDataMultiPart.cleanup();
            try {
				formDataMultiPart.close();
			} catch (IOException e) {
				System.out.println("ERROR trying to close formDataMultiPart.cloase(): "+e);
			}
            //response.close();
           // cliente.close();
        }
		return finalResponse;
	}
	
	
	
	/**
	 * Sends and HTTP Request to the rest service in order to unzip and rename a Zip Tomcat Template hosted on a remote slave server.
	 * @param pServerUrl
	 * @param pRemoteInstancePath
	 * @param pActualName
	 * @param pNewName
	 * @return True if success, otherwise false.
	 */
	public boolean descompresionRenombradoRemoto(String pServerUrl, String pRemoteInstancePath, String pActualName, String pNewName){
		System.out.println("\nStartup of service: "+pServerUrl+"/agent/unzip/instance");
		boolean resultado = false;

		
		try {
			WebTarget myTarget = client.target(pServerUrl+"/agent/unzip/instance");
			WebTarget myTargetWithParams = myTarget.queryParam("pathLocation", pRemoteInstancePath).queryParam("templateName", pActualName).queryParam("newName", pNewName);
			Invocation.Builder invocationBuilder = myTargetWithParams.request(MediaType.APPLICATION_JSON);
			Response response = invocationBuilder.get();
			
			
			
			if( response.getStatus() == 200){
				String responseMessageFromServer = response.getStatusInfo().getReasonPhrase();
                System.out.println(" ResponseMessageFromServer: " + responseMessageFromServer);
				resultado = true;
			}else{
				String responseMessageFromServer = response.getStatusInfo().getReasonPhrase();
                System.out.println(" ResponseMessageFromServer: " + responseMessageFromServer);
                
				System.out.println(" ERROR, obtained HTTP code: "+response.getStatus());
				resultado = false;
			}
		} catch (Exception e) {
			System.out.println(" Error trying consuming service: "+pServerUrl);
			System.out.println(e);
			resultado = false;
		}
		return resultado;
	}
	
	
	/**
	 * Makes a HTTP PUT method for update the port number of a server.xml file inside a Tomcat instance on a remote slave server.
	 * @param pServerUrl Remote server where the agent is hosted.
	 * @param pUbicacionInstancia Path of tomcat instance inside the remote server.
	 * @param pServerXml Object ServerXml.class containing the new port configuration.
	 * @return True if success, otherwise false.
	 */
	public boolean actualizaArchivoServerXmlRemoto(String pServerUrl, String pStandarInstancePath, String pInstanceName, ServerXml pServerXml){
		System.out.println(" Startup Service: "+pServerUrl+"/instance/update/serverxml");
				
		WebTarget myTarget = client.target(pServerUrl+"/instance/update/serverxml");
		Builder invocationBuilder = myTarget.request().header("instancePath", pStandarInstancePath+"/"+pInstanceName); 
		Response response = invocationBuilder.put(Entity.entity(pServerXml, MediaType.APPLICATION_JSON_TYPE));
		
		if( response.getStatus() == 200){
			System.out.println("Update succed.");
           String responseMessageFromServer = response.getStatusInfo().getReasonPhrase();
           System.out.println("ResponseMessageFromServer: " + responseMessageFromServer);
           return(true);
		}else{
			System.out.println("ERROR, HTTP code <" + response.getStatus() + "> message:"+response.readEntity(String.class));
			return(false);
		}

	}
}
