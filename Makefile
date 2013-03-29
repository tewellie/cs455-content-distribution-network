# Makefile for CDN

JFLAGS       = -g




default: 
	javac $(JFLAGS) cdn/Discovery.java
	javac $(JFLAGS) cdn/DiscoveryReceiverThread.java
	javac $(JFLAGS) cdn/Helper.java
	javac $(JFLAGS) cdn/LinkInfo.java
	javac $(JFLAGS) cdn/LinkReceiverThread.java
	javac $(JFLAGS) cdn/LinkWeightUpdate.java
	javac $(JFLAGS) cdn/Message.java
	javac $(JFLAGS) cdn/MessageType.java
	javac $(JFLAGS) cdn/MSTPrim.java
	javac $(JFLAGS) cdn/Pair.java
	javac $(JFLAGS) cdn/PeerID.java
	javac $(JFLAGS) cdn/PeerRouterList.java
	javac $(JFLAGS) cdn/RegisterRequest.java
	javac $(JFLAGS) cdn/RegisterResponse.java
	javac $(JFLAGS) cdn/Router.java
	javac $(JFLAGS) cdn/RouterInfo.java
	javac $(JFLAGS) cdn/RouterReceiverThread.java
	javac $(JFLAGS) cdn/Tree.java
	javac $(JFLAGS) cdn/TreeNode.java
	javac $(JFLAGS) cdn/UpdateLinks.java
	
all: 
	javac $(JFLAGS) cdn/Discovery.java
	javac $(JFLAGS) cdn/DiscoveryReceiverThread.java
	javac $(JFLAGS) cdn/Helper.java
	javac $(JFLAGS) cdn/LinkInfo.java
	javac $(JFLAGS) cdn/LinkReceiverThread.java
	javac $(JFLAGS) cdn/LinkWeightUpdate.java
	javac $(JFLAGS) cdn/Message.java
	javac $(JFLAGS) cdn/MessageType.java
	javac $(JFLAGS) cdn/MSTPrim.java
	javac $(JFLAGS) cdn/Pair.java
	javac $(JFLAGS) cdn/PeerID.java
	javac $(JFLAGS) cdn/PeerRouterList.java
	javac $(JFLAGS) cdn/RegisterRequest.java
	javac $(JFLAGS) cdn/RegisterResponse.java
	javac $(JFLAGS) cdn/Router.java
	javac $(JFLAGS) cdn/RouterInfo.java
	javac $(JFLAGS) cdn/RouterReceiverThread.java
	javac $(JFLAGS) cdn/Tree.java
	javac $(JFLAGS) cdn/TreeNode.java
	javac $(JFLAGS) cdn/UpdateLinks.java
	
clean: 
	rm -f *.class *~


