This program was written for an upper level computer science course in February 2012. There are two main classes, Discovery.java and Router.java.
Exactly one discovery node in the system is responsible for allowing routers to register and deregister themselves, constructing a CDN by orchestrating connections between routers, and assigning and publishing weights to the links connecting any two routers. Commands supported while running Discovery.java:
list-routersl
ist-weights
setup-cdn number-of-connections

Multiple router nodes exist in the system. They are responsible for routing data packets iwthin the CDN and updating routes based on changes to link weights between nodes in the CDN. When a router sends a message, every router in the CDN receives the packet exactly once. Commands supported while running Router.java:
print-MST
send-data
exit-cdn

Running from the command line:
java cdn.Discovery portnum refresh\_interval
java cdn.Router portnum assigned-id discovery-host discovery-port