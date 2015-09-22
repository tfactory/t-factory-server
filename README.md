# t-factory-server

t-factory-server is a software aimed to manage Tomcat instances in a simple, standardized  and easy way via a Web Graphic User Interface that interact with agents (t-Factory-Agent) installed on remote servers.

tFactory usually fit well in scenarios when you hate to ask questions like:
* Which ports are available in a remote server?
* Which ports does the current instances have configured on remote server?
* Can you create a new instance in less than 1 minutes with custom settigns?
* Can you give me a report of all the instances in our environment with his respective ports configuration now?
* Can you create a image template and be distributed on remote servers in less than 2 minute?
* What are we going to do if operations guy don't want to use Chef, Puppet or other cool tools for tomcat management?


## Table of contents
* [Creator](#creator)
* [Installation](#installation)
* [Features](#features)
* [Contributing](#contributing)
* [Documentation](#documentation)
* [Bugs and feature requests](#bugs-and-feature-requests)
* [Roadmap](#roadmap)
* [Copyright and license](#copyright-and-license)


##Creator

**Cesar Hernandez**

* <https://twitter.com/CesarHgt>
* <https://cesarhernandezgt.blogspot.com> 

##Installation
* Download the [latest release] (https://github.com/tfactory/t-factory-server/releases) (.war) file and deploy it on any Tomcat 7 instance. JRE 7 or latest should be used the the Tomcat instance.
* Start the tomcat instance.
* Personalize the file global-configuration.properties according to your needs.
* Paste the Tomcat intance(s) template(s) you want to distribute with the tFactory server
* Restart the tomcat instance.

For agente configuration see: [t-factory-agent] (https://github.com/tfactory/t-factory-agent/) 


##Features

* Add/Remove remote servers to manage.
* Monitor remote t-factory-agent status.
* Register/Deregister instances already created on remote servers.
* Creation of new instances on remote servers with auto and manual selection of available ports.
* Instance template catalog capability.
* Monitor changes on configuration (currently server.xml file) of remote instances.
* Internationalized GUI (currently only EN_US and ES_GT).

## Contributing
You are wellcome to improve the software. Be sure to check: opening issues, coding standards, and notes on development code.

## Documentation
You can find enough documentation by the generation of the javaDoc of this project.

## Bugs and feature requests
You can check: [open and closed issues.](https://github.com/tfactory/t-factory-server/issues/new)

## Roadmap
This is the list of the upcoming features. (Looking forward to have your coding contribution):
* Add Persistences layer (currently all is stored in memory)
* Authentication and Authorization 
* Log4j incorporation
* Configure memory parameters to instances when they are created from the tFactory server.
* Configure jmx port parameter to instances when they are created from the tFactory server.
* Datasource managements.
* GUI edition of global configurations.


## Copyright and license
Code released under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


