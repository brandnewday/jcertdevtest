jcertdevtest
============

A java exercise based on the Sun/Oracle Certified Java Developer assignment. 
Although I have not yet taken the assignment myself, and that there are many 
variations of the requirements details, the general requirements and similar 
examples are available in various exam guide books, online discussion forums 
etc.

The assignment touches on aspects of IO, networking, concurrency, and designing
layered architecture. Swing GUI, and non-coding tasks such as user guide, design
discussion etc, are also part of the requirement, but not attempted in this 
exercise.



Requirements
------------

A room booking system using a custom file datastore.

- GUI client which can:
    1. show all records
    2. search by name/location
    3. book a record with a given customer id (input by user)
- Server to use a provided binary format file datastore
- Server to implement a mandatory interface for CRUD on the file 
    - NB: client does NOT make use of all methods in this interface (e.g. client
        does not need to create/delete/unbook record), but the interface methods
        must still all be implemented according to the interface descriptions.
- The system can start from a single entry point (same jar file) in 3 
  different modes (by command line arg):
    1. GUI client (communicate with server over network). Can start multiple.
    2. Server (communicte with clients over network)
    3. Stand-alone (client and server in the same process and NOT going over
        network. i.e. client calls server logic directly by normal method calls)
- Use either Java RMI or Socket with serialized object for networking.
- Configs are passed in using some GUI on startup (no command arg, config file)



My Changes To Requirements
--------------------------

- Implement an extra mode to run as webapp
    1. instead of Java Swing GUI client, access as a webpage
    2. instead of network server, implement as webapp on Tomcat
- For the network client mode, do not bother with Java Swing, implement a simple
    CLI client instead.
- Use socket instead of RMI for the network modes. It is quite obvious from 
    exam guides and forum dicussions that RMI should be more appropriate for 
    the given requirements, which also mention it is better to be simple then 
    doing anything complicated for a junior developer to maintain. But socket 
    is more interesting as an exercise.
- For simplicity, takes config from command arg java properties.



Screenshots
-----------

![Webapp mode](/resources/screenshots/webapp.png)

![Network CLI Client mode](/resources/screenshots/network_client.png)




Build / Installation
--------------------

1. Unpack files, go to project root dir.
2. The db file is "db-1x1.db". Take note of its file path which is needed for startup and config.
3. For webapp mode, db file path is set in web.xml. Either first build the .war and 
    then deploy as exploded and modify the xml, or go directly to:
    /jcertdevtest-webapp/src/main/webapp/WEB-INF/web.xml
    and change the context param filePath value, before build.
4. From root dir, do `mvn clean package`
5. For webapp mode, copy created jcertdevtest-webapp.war file to Tomcat webapp dir
6. For other modes, the jar is created in /jcertdevtest-app/target/jcertdevtest-app-1.0-SNAPSHOT.jar



Run
---

Webapp mode (browser):

    http://localhost:{port}/jcertdevtest-webapp/

Network mode server:

    java -DfilePath={file path to db file} -Dport={port} jcertdevtest-app-1.0-SNAPSHOT.jar server

Network mode client:

    java -Dhost=localhost -Dport={port} jcertdevtest-app-1.0-SNAPSHOT.jar client

Standalone:

    java -DfilePath={file path to db file} jcertdevtest-app-1.0-SNAPSHOT.jar standalone




Design
------

The implemented system has the following modes/layers

1. Webapp mode  
Browser --> HttpServer -> Business -> Database

2. Network mode  
UI --> NetworkClient -> NetworkServer -> Business -> Database

3. Standalone  
UI -> Business -> Database


Main classes:

Common:

- `TestApp` startup entry point for the network/standalone modes
- `CLIClient` interactive command line client
- `DB` mandatory interface
- `Data` implementation of the mandatory interface
- `BookingService` interface for the required client functionalities book/search
- `BookingServiceImpl` implementation with the business logic

Network:

- `RemoteBookingServiceClient` implementation of BookingService for use on client
    to call server over network for the actual business implementation
- `NetworkClient` used by above to help provide a normal blocking method call 
interface
- `NetworkServer` Listens for network client connection
- `RemoteBookingServiceServerSessionHandler` Server side handling per session 
    events talking to the actual BookingService
- `NetworkSession` used in common on both client and server side to handle a
    single accepted socket connection, and IO in a non-blocking way (by 
    threads, not NIO).

Web:

- `MainServlet` handles search and display of the main page
- `BookingServlet` handles book request from main page
