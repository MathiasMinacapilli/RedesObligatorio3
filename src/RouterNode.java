import javax.swing.*;        
import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private HashMap<Integer, Integer> vecinos; // <idVecino, costoMinimo>
  private HashMap<Integer, Integer> costs; // <idNodoDestino, costoMinimo>
  private HashMap<Integer, Integer> forwardingTable; // <idNodoDestino, idVecino>

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> vecinos) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    this.vecinos = vecinos;
    this.costs = new HashMap();
    this.vecinos.forEach((idRouter, costo) -> {
    	this.costs.put(idRouter, costo);
    });
    
    // Enviar mi vector a mis vecinos
    vecinos.forEach((idRouter, costo) -> {
    	RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, this.vecinos);
    	this.sendUpdate(routerPacket);
    });
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
	  // Manejar el recibimiento de un paquete
	  Integer sourceId = pkt.sourceid;
	  // Recalculo mis costos
	  pkt.mincost.forEach((idRouterDeLaTablaDeMiVecino, costoRouterIntermedio) -> {
		  if (!this.costs.containsKey(idRouterDeLaTablaDeMiVecino)) {
			  this.costs.put(idRouterDeLaTablaDeMiVecino, sim.INFINITY);
		  }
		  Integer costoDelRouterIntermedioAlDestino = this.costs.get(idRouterDeLaTablaDeMiVecino) + costoRouterIntermedio;
		  if (this.costs.get(idRouterDeLaTablaDeMiVecino) > costoDelRouterIntermedioAlDestino) {
			  // Nos sale mas rentable ir al router y que el vaya a donde queremos
			  this.costs.put(idRouterDeLaTablaDeMiVecino, costoDelRouterIntermedioAlDestino);
			  this.forwardingTable.put(idRouterDeLaTablaDeMiVecino, sourceId);
			  if (this.vecinos.containsKey(idRouterDeLaTablaDeMiVecino)) {
				  this.vecinos.put(idRouterDeLaTablaDeMiVecino, costoDelRouterIntermedioAlDestino);
			  }
			  // Mando actualizacion a mis vecinos
			  this.vecinos.forEach((idRouter, costo) -> {
			    	RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, this.costs);
			    	this.sendUpdate(routerPacket);
			    });
		  }
	  });
	  printDistanceTable();
  }
  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);

  }
  

  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());
	  myGUI.println(F.format(forwardingTable, 28));
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
  }

}
