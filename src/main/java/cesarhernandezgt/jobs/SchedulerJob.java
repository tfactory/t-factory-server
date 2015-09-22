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

import java.util.Date;
import java.util.HashSet;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import cesarhernandezgt.clientRest.AgentRestClient;
import cesarhernandezgt.dto.AgentDto;
import cesarhernandezgt.dto.Server;


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
	
	
	@Override
	public void execute(JobExecutionContext context)
		throws JobExecutionException {
 
		JobDataMap data = context.getJobDetail().getJobDataMap();
		@SuppressWarnings("unchecked")
		HashSet<Server> listaServidores = (HashSet<Server>) data.get("LISTA_SERVIDORES");

		
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
	public void sincronizarListaServidores (HashSet<Server> pListaServidores){
		AgentRestClient agenteRestclientSvc = new AgentRestClient();
		int exitoso = 0;
		int fallidos = 0;
		
		AgentDto objAgenteDtoAux = null;
		for (Server servidor : pListaServidores) {
			objAgenteDtoAux= agenteRestclientSvc.obtainAgenteSrv(servidor.getAgentDto().getPath());
			
			if(objAgenteDtoAux.getStatus().equalsIgnoreCase("ok")){
				exitoso ++;
				//procedemos a actualizar datos del sevidor
				servidor.getAgentDto().setStatus(objAgenteDtoAux.getStatus());
				servidor.getAgentDto().setHostname(objAgenteDtoAux.getHostname());
				servidor.getAgentDto().setVersionAgent(objAgenteDtoAux.getVersionAgent());
				System.out.println("Succes sync with server:  "+ servidor.getAgentDto().getHostname());
			}else {
				fallidos++;
				servidor.getAgentDto().setStatus("offline");
				System.out.println("Error trying sync with the server <" +objAgenteDtoAux.getStatus() + ">");
			}
			System.out.println(" Synchronizations successful: "+exitoso);
			System.out.println(" Synchronizations faild: "+fallidos);

		}
		
		
		
		
	}

}
