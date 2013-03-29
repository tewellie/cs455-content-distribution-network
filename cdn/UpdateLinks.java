package cdn;

public class UpdateLinks extends Thread {
	
	private Discovery discoveryNode;
	private long time;
	
	UpdateLinks(Discovery node, long waitTime){	
		discoveryNode = node;
		time = waitTime;
		
	}
	 public void run() {
		    while (!interrupted()) {
		      try {
		         //then sleep for a millisecond
		        sleep(time);
		        discoveryNode.updateLinkWeights();
		      } catch (InterruptedException e) {
		      }
		    }
		  }

}
