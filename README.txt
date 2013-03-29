Theresa Wellington
CS 455 - CDN

Classes:
	cdn/Discovery.java - Discovery Node, handles all required functionality and also allows routers to register and deregister after cdn is setup
	cdn/DiscoveryReceiverThread.java - receives messages from Routers - one instance per router
	cdn/Helper.java - contains methods to help convert ints to and from byte arrays
	cdn/LinkInfo.java - contains information about links, methods to marshal/unmarshal
	cdn/LinkReceiverThread.java - allows routers to wait for connections from other routers
	cdn/LinkWeightUpdate.java - marshal/unmarshal link weight update message
	cdn/Message.java - marshal/unmarshal all messages
	cdn/MessageType.java - defines different message types
	cdn/MSTPrim.java - builds a minimum spanning tree for the CDN
	cdn/Pair.java - data structure that holds two objects (mostly used as Pair<Sting,String> for link IDs)
	cdn/PeerID.java - used marshal/unmarshal a router ID;
	cdn/PeerRouterList.java - data structure, methods to marshal/unmarshal peer router lists
	cdn/RegisterRequest.java - data structure for registering *and* deregistering, methods to marshal/unmarshal peer router lists
	cdn/RegisterResponse.java - data structure for responding to registration/deregistration, methods for marshalling/unmarshalling
	cdn/Router.java - Router Node (handles all required functionality) *Except* print-MST does not print in depth first search order, simply prints out a hash table of the links in the MST
	cdn/RouterInfo.java - contains information about Router nodes, and methods to marshal/unmarshal RouterInfo objects
	cdn/RouterReceiverThread.java - receives messages from Discovery Node, other Routers
	cdn/Tree.java - data structure used in MSTPrim
	cdn/TreeNode.java - data structure used in Tree
	cdn/UpdateLinks.java - thread that waits for the specified refresh_interval then tells the Discovery Node to update links


Other information:
	The program is not very well commented right now and also needs packaging introduced.
	Print-MST does print the minimum spanning tree but not in the same format as the assignment example
	The Discovery Node allows routers to register/deregister while the CDN is setup (it waits to propogate changes until setup-cdn is called again)
	