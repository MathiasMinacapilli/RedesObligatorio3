import javax.swing.*;        
import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {

  }
  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);

  }
  

  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
  }

}
