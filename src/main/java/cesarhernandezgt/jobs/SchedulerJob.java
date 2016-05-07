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
package cesarhernandezgt.jobs;

import cesarhernandezgt.clientRest.AgentRestClient;
import cesarhernandezgt.dto.AgentDto;
import org.quartz.*;
import tfactory.server.jpa.entity.ServerAgent;
import tfactory.server.jpa.exception.TFactoryJPAException;
import tfactory.server.jpa.service.GenericService;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Job verify the remote servers status. By using @DisallowConcurrentExecution
 * we prevente multiple instance of this job running at the same time.
 * Documentation: 
 * http://www.quartz-scheduler.org/documentation/quartz-2.x/examples/Example4.
 * 
 * @author cesarHernandezGt
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SchedulerJob implements Job{

	private final static Logger LOGGER = Logger.getLogger(SchedulerJob.class.getName());

	@Override
	public void execute(JobExecutionContext context)
		throws JobExecutionException {
 
		JobDataMap data = context.getJobDetail().getJobDataMap();

		//Gets the current list of servers from database
		GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);
		List<ServerAgent> listaServidores =  service.findAll();

		
		Date timestampInicio = new Date();
		System.out.println("---Start Job Quartz---"+timestampInicio.toString());
		if(listaServidores!=null ){
			System.out.println("Server list has:" + listaServidores.size());
			
			if (listaServidores.size()>0) {
				sincronizarListaServidores(listaServidores);
			}
			
			
		}else{
			System.out.println(" Error Job, Server List is NULL.");
		}
		
		Date timestampFin = new Date();
		System.out.println("---End of Job Quartz---"+timestampFin.toString());
	}
	
	/**
	 * Sinchronice the entire list of Servers.
	 * @param pListaServidores
	 */
	public void sincronizarListaServidores (List<ServerAgent> pListaServidores){
		AgentRestClient agenteRestclientSvc = new AgentRestClient();
		int exitoso = 0;
		int fallidos = 0;
		
		AgentDto objAgenteDtoAux = null;
		for (ServerAgent servidor : pListaServidores) {
			objAgenteDtoAux= agenteRestclientSvc.obtainAgenteSrv(servidor.getPath());
			
			if(objAgenteDtoAux.getStatus().equalsIgnoreCase("ok")){
				exitoso ++;
				//procedemos a actualizar datos del sevidor
				servidor.setStatus(objAgenteDtoAux.getStatus());
				servidor.setHostname(objAgenteDtoAux.getHostname());
				servidor.setVersionAgent(objAgenteDtoAux.getVersionAgent());
				System.out.println("Succes sync with server:  "+ servidor.getHostname());
			}else {
				fallidos++;
				servidor.setStatus("offline");
				System.out.println("Error trying sync with the server <" +objAgenteDtoAux.getStatus() + ">");
			}

			//update database
			try
			{
				GenericService<ServerAgent> service = GenericService.of(ServerAgent.class);
				service.updateEntity(servidor);

				System.out.println(" Synchronizations successful: "+exitoso);
				System.out.println(" Synchronizations faild: "+fallidos);
			}
			catch(TFactoryJPAException ex)
			{
				LOGGER.log(Level.SEVERE, "error updating server in database from job.", ex);
			}
		}
		
		
		
		
	}

}
