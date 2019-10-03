import javax.swing.*;        
import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private F myFormat;
  private RouterSimulator sim;
  private HashMap<Integer, Integer> vecinos; // <idVecino, costoMinimo>
  private HashMap<Integer, Integer> costs; // <idNodoDestino, costoMinimo>
  private HashMap<Integer, Integer> forwardingTable; // <idNodoDestino, idVecino>

  
  /*
  =============================================================================================
							CONSTRUCTOR DEL NODO ROUTER
  =============================================================================================
  */
  
  public RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> vecinos) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    this.vecinos = vecinos;
    this.costs = new HashMap<Integer, Integer>();
    this.forwardingTable = new HashMap<Integer, Integer>();
    this.vecinos.forEach((idRouter, costo) -> {
    	this.costs.put(idRouter, costo);
    	this.forwardingTable.put(idRouter, idRouter);
    });
    
	//!!!!!!!!!!!!!!! setear el costo a mi mismo en 0
	this.costs.put(this.myID, 0);
	//!!!!!!!!!!!!!!! setearla interfaz de salida a mi mismo a myID
	this.forwardingTable.put(this.myID, this.myID);
	  
    	
    
    // Enviar mi vector a mis vecinos
    vecinos.forEach((idRouter, costo) -> {
    	RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, this.vecinos);  //!!!!!!!!!!!!!!!!!o this.costs? Que en realidad seria lo mismo porque se inicializan iguales, pero semanticamente?
    	this.sendUpdate(routerPacket);
    });
    
    //DEBUG
    myGUI.print(F.format(costs, 70));
    myGUI.print("\n\n");
    
  }

  

  
  
  /*
  =============================================================================================
									RECEIVE UPDATE
  =============================================================================================
  */
  public void recvUpdate(RouterPacket pkt) {
	  // Manejar el recibimiento de un paquete
	  Integer sourceId = pkt.sourceid;
	  HashMap<Integer, Integer> oldVector = new HashMap<Integer, Integer>();
	  this.costs.forEach((idRouterDestino, costo) -> {
	    	oldVector.put(idRouterDestino, costo);
	  });
	  
	  /*//!!!!!!!!!!!!!!! setear el costo a mi mismo en 0 en oldVector, no se si es necesario
	  oldVector.put(this.myID, 0);*/ 
	  
	  boolean vectorCambiado = false;
	  // Recalculo mis costos
	  
	  pkt.mincost.forEach((idRouterDeLaTablaDeMiVecino, costoRouterIntermedio) -> {
		  
		  if(idRouterDeLaTablaDeMiVecino!=this.myID){
			  if (!this.costs.containsKey(idRouterDeLaTablaDeMiVecino)) {
				  this.costs.put(idRouterDeLaTablaDeMiVecino, sim.INFINITY);  //Si reconozco un nuevo router que no era mi vecino, le seteo distancia infinito
			  }
			  
			  //Integer costoDelRouterIntermedioAlDestino = this.costs.get(idRouterDeLaTablaDeMiVecino) + costoRouterIntermedio;
			  Integer costoPasandoPorRouterIntermedioAlDestino = this.costs.get(sourceId) + costoRouterIntermedio;  //!!!!!!!!!!no seria eso, o sea, mi costo al vecino + lo del vecino al destino?
			  if (this.costs.get(idRouterDeLaTablaDeMiVecino) > costoPasandoPorRouterIntermedioAlDestino) {
				  // Nos sale mas rentable ir al router y que el vaya a donde queremos
				  this.costs.put(idRouterDeLaTablaDeMiVecino, costoPasandoPorRouterIntermedioAlDestino);
				  
				  this.forwardingTable.put(idRouterDeLaTablaDeMiVecino, this.forwardingTable.get(sourceId)); //!!!!!!!! antes estaba solamente (...,sourceID) pero ver contraejemplo destino 3 del router 0
				  //si era un vecino actulizo tambien en esa tabla
				  

				    myGUI.print(F.format(forwardingTable, 70));
				  
				  //if (this.vecinos.containsKey(idRouterDeLaTablaDeMiVecino){     !!!!!!!!!!!!!!un nodo no es vecino de si mismo
				  if (this.vecinos.containsKey(idRouterDeLaTablaDeMiVecino)) {
					  this.vecinos.put(idRouterDeLaTablaDeMiVecino, costoPasandoPorRouterIntermedioAlDestino);
				  }	
			  }
		 }
	  });

	  if(!oldVector.equals(this.costs)) {
		  vectorCambiado = true;
	  }
	  
	  if(vectorCambiado) {
		  // Mando actualizacion a mis vecinos
		  this.vecinos.forEach((idRouter, costo) -> {
		    	RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, this.costs);
		    	this.sendUpdate(routerPacket);
		    });
	  }
	  printDistanceTable();
  }
  

  
  
  
  /*
  =============================================================================================
								SEND UPDATE
  =============================================================================================
  */
    
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);

  }
  
  
  
  
  /*
  =============================================================================================
							PRINT DISTANCE TABLE
  =============================================================================================
  */
  public void printDistanceTable() {
	  
	  
	  //---------------Título------------------------------
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime() + "\n");
	  
	  myGUI.print("Vector de distancias y rutas" + "\n\n");

	  //---------------Fila Destino------------------------ 
	  myGUI.print(myFormat.format("Destino       |", 25));
	  
	  this.costs.forEach((router, costo) -> {
		  String imprimir = myFormat.format(router, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  myGUI.print("\n");
	  
	  Integer i;
	  for(i=0; i<100; i++) {
		  myGUI.print("¯");
	  }
	  
	  myGUI.print("\n");
	  
	  //---------------Fila Costo------------------------ 
	  
	  myGUI.print(myFormat.format("Costo        |", 25));
	  
	  this.costs.forEach((router, costo) -> {
		  String imprimir = myFormat.format(costo, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  
	  myGUI.print("\n\n");
	  
	  //---------------Fila Ruta-------------------------
	  
	  myGUI.print(myFormat.format("Ruta        |", 25));
	  
	  this.forwardingTable.forEach((idDestino, interfazSalida) -> {
		  String imprimir = myFormat.format(interfazSalida, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  myGUI.println("\n\n\n");
	  
  }

  
  /*
  =============================================================================================
							UPDATE LINK COST
  =============================================================================================
  */
  
  
  public void updateLinkCost(int dest, int newcost) {
  }

}
