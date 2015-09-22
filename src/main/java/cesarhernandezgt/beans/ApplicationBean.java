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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import cesarhernandezgt.dto.Server;
import cesarhernandezgt.jobs.SchedulerJob;


@ManagedBean(eager=true)
@ApplicationScoped
public class ApplicationBean {
	
	@ManagedProperty("#{globalConf.IntancesHomeDirectory}") 
	private String intancesHomeDirectories;
	
	@ManagedProperty("#{globalConf.InstancesTemplateHomeDirectory}")
	private String instancesTemplateHomeDirectory;
	
	@ManagedProperty("#{globalConf.JavaInstallationsPaths}")
	private String javaInstallationsPaths;
	
	@ManagedProperty("#{globalConf.SynchInterval}")
	private String synchInterval;
	
	
	/**
	 * Master list of registered Servers.
	 * This is going to be replaced by a persistence store in future releases.
	 */
	private HashSet<Server>  listaServidores ;
	
	
	/**
	 * Nandera para activar/desactivar el auto refresh de las tablas cada n tiempo. 
	 */
	private boolean autoRefreshOff=false; 
	private List<SelectItem>  listaUbicEstandarDeInstancias;
	private List<SelectItem>  listaPlantillasInstancias;
	private List<SelectItem>  listaVersionJavaInstancias;
	private List<SelectItem>  listaMemDeInstancias;
	
	//Port range to be used by the t-factory-server at instance creation to test remote available ports
    //More information on Port range: http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml
	@ManagedProperty("#{globalConf.InitialPortRange}")
	private int puertosRangoInical;
	@ManagedProperty("#{globalConf.FinalPortRange}")
	private int puertosRangoFinal;
	
	@PostConstruct
    public void init() {
		System.out.println("Loading global-configuration properties.");
		listaServidores = new HashSet<Server> ();
		autoRefreshOff = true;
		listaUbicEstandarDeInstancias = new ArrayList<SelectItem>();
		listaPlantillasInstancias = new ArrayList<SelectItem>();
		listaVersionJavaInstancias = new ArrayList<SelectItem>();
		listaMemDeInstancias = new ArrayList<SelectItem>();
		
		
		//Fill the list containing home directory(ies) of Tomcat Instance(s) On Remote Servers
		List<String> listAux1 = new ArrayList<String>(Arrays.asList(intancesHomeDirectories.split(",")));
		for (String instHomePath : listAux1) {
			listaUbicEstandarDeInstancias.add(new SelectItem(instHomePath,instHomePath));
		}


		//Fill the list of Intances Template to be used in the GUI.
		List<String> listAux2 = new ArrayList<String>(Arrays.asList(instancesTemplateHomeDirectory.split(",")));
		for (String instTemplateHomePath : listAux2) {
			listaPlantillasInstancias.add(new SelectItem(instTemplateHomePath,instTemplateHomePath));
		}

		//Fill the list  to be used in the GUI for selection Java Installations on remotes servers.
		List<String> listAux3 = new ArrayList<String>(Arrays.asList(javaInstallationsPaths.split(",")));
		for (String instTemplateHomePath : listAux3) {
			listaVersionJavaInstancias.add(new SelectItem(instTemplateHomePath,instTemplateHomePath));
		}
		
		//Initialize Heap and Perm space ranges for GUI selection.  
		 Double dAux;
	     String strAux;
	     for (int i = 6; i < 16; i++) { //from 64MB to 32768 MB.
	    	  dAux = Math.pow(2, i);
	    	  strAux = String.valueOf(dAux.intValue());
	    	  listaMemDeInstancias.add(new SelectItem(strAux,strAux+" MB (-Xms"+strAux+"m -Xmx"+strAux+"m)"));
	      }
		
	     
        //We create the CHRONE JOB to synchronize and verify the remote serer status and their instances.
		JobKey jobKeyA = new JobKey("jobA", "group1");
    	JobDetail jobA = JobBuilder.newJob(SchedulerJob.class)
		.withIdentity(jobKeyA).build();
		Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity("dummyTriggerName1", "group1").withSchedule(CronScheduleBuilder.cronSchedule(synchInterval)).build();
		// we send as reference the Master List of Remote Servers objects
    	jobA.getJobDataMap().put("LISTA_SERVIDORES", listaServidores);
    	//System.out.println("se persistio en jobA la lisata de Servidores ...");
    	
    	try {
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			
			scheduler.start();
			scheduler.scheduleJob(jobA, trigger1);
			System.out.println("Synch job created sucesfully. ");
			
		} catch (SchedulerException e) {
			System.out.println(" ERROR ON SCHEDLERS FACTORY!!!!!!!!!!!");
			e.printStackTrace();
		}
        
    }
	
	
	public String applyConfigurationChanges(){
		System.out.println("Global configuration changed successfully.");
		return "configuration.xhtml";
	}
	
	public ApplicationBean(){
		
	}

	public String getIntancesHomeDirectories() {
		return intancesHomeDirectories;
	}

	public String getSynchInterval() {
		return synchInterval;
	}

	public void setSynchInterval(String synchInterval) {
		this.synchInterval = synchInterval;
	}

	public String getJavaInstallationsPaths() {
		return javaInstallationsPaths;
	}

	public void setJavaInstallationsPaths(String javaInstallationsPaths) {
		this.javaInstallationsPaths = javaInstallationsPaths;
	}

	public String getInstancesTemplateHomeDirectory() {
		return instancesTemplateHomeDirectory;
	}

	public void setInstancesTemplateHomeDirectory(
			String instancesTemplateHomeDirectory) {
		this.instancesTemplateHomeDirectory = instancesTemplateHomeDirectory;
	}

	public void setIntancesHomeDirectories(String intancesHomeDirectories) {
		this.intancesHomeDirectories = intancesHomeDirectories;
	}

	public HashSet<Server> getListaServidores() {
		return listaServidores;
	}

	public void setListaServidores(HashSet<Server> listaServidores) {
		this.listaServidores = listaServidores;
	}

	public boolean isAutoRefreshOff() {
		return autoRefreshOff;
	}

	public void setAutoRefreshOff(boolean autoRefreshOff) {
		this.autoRefreshOff = autoRefreshOff;
	}

	public List<SelectItem> getListaUbicEstandarDeInstancias() {
		return listaUbicEstandarDeInstancias;
	}

	public void setListaUbicEstandarDeInstancias(
			List<SelectItem> listaUbicEstandarDeInstancias) {
		this.listaUbicEstandarDeInstancias = listaUbicEstandarDeInstancias;
	}

	public List<SelectItem> getListaPlantillasInstancias() {
		return listaPlantillasInstancias;
	}

	public void setListaPlantillasInstancias(
			List<SelectItem> listaPlantillasInstancias) {
		this.listaPlantillasInstancias = listaPlantillasInstancias;
	}

	public List<SelectItem> getListaVersionJavaInstancias() {
		return listaVersionJavaInstancias;
	}

	public void setListaVersionJavaInstancias(
			List<SelectItem> listaVersionJavaInstancias) {
		this.listaVersionJavaInstancias = listaVersionJavaInstancias;
	}

	public List<SelectItem> getListaMemDeInstancias() {
		return listaMemDeInstancias;
	}

	public void setListaMemDeInstancias(List<SelectItem> listaMemDeInstancias) {
		this.listaMemDeInstancias = listaMemDeInstancias;
	}

	public int getPuertosRangoInical() {
		return puertosRangoInical;
	}

	public void setPuertosRangoInical(int puertosRangoInical) {
		this.puertosRangoInical = puertosRangoInical;
	}

	public int getPuertosRangoFinal() {
		return puertosRangoFinal;
	}

	public void setPuertosRangoFinal(int puertosRangoFinal) {
		this.puertosRangoFinal = puertosRangoFinal;
	}


	
}
