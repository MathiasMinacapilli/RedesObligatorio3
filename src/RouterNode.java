import javax.swing.*;        
import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private HashMap<Integer, Integer> myVecinos; // <idVecino, costoMinimo> ------------------CONTIENE LOS COSTOS MINIMOS A LOS VECINOS
  private HashMap<Integer, Integer> myDistanciasMinimas; // <idNodoDestino, costoMinimo> ---------------CONTIENE LOS COSTOS MINIMOS PARA CADA DESTINO
  private HashMap<Integer, Integer> myForwardingTable; // <idNodoDestino, idVecino> --------CONTIENE LAS INTERFACES DE SALIDA PARA CADA DESTINO
  private HashMap<Integer, HashMap<Integer, Integer>> DistanceVectorDeVecinos; //<idVecino, distanceVector>  -CONTIENE LOS VECTORES DE DISTANCIA DE LOS VECINOS
  private boolean reversaEnvenenada = true;
  private Integer cantidadUpdates = 0;
  
  /*
  =============================================================================================
							CONSTRUCTOR DEL NODO ROUTER
  =============================================================================================
  */
  
  public RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> vecinos) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    this.myVecinos = vecinos;
    if(this.myVecinos == null) {
    	this.myVecinos = new HashMap<Integer, Integer>();
    }
    this.myDistanciasMinimas = new HashMap<Integer, Integer>();
    this.myForwardingTable = new HashMap<Integer, Integer>();
    this.myVecinos.forEach((idRouter, costo) -> {
    	this.myDistanciasMinimas.put(idRouter, costo);
    	this.myForwardingTable.put(idRouter, idRouter);
    });
    this.DistanceVectorDeVecinos = new HashMap<Integer, HashMap<Integer,Integer>>();
    
	//!!!!!!!!!!!!!!! setear el costo a mi mismo en 0
	this.myDistanciasMinimas.put(this.myID, 0);
	//!!!!!!!!!!!!!!! setear la interfaz de salida a mi mismo a myID
	this.myForwardingTable.put(this.myID, this.myID);
	  
    	
    
    // Enviar mi vector a mis vecinos
    vecinos.forEach((idRouter, costo) -> {
    	//RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, this.vecinos);  !!!!!!!!!!!!!!!!!o this.costs? Que en realidad seria lo mismo porque se inicializan iguales, pero semanticamente?
    	RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, this.myDistanciasMinimas);
    	this.sendUpdate(routerPacket);
    });
    
  }

  

  
  
  /*
  =============================================================================================
									RECEIVE UPDATE
  =============================================================================================
  */
  public void recvUpdate(RouterPacket pkt) {
	  cantidadUpdates++;
	  // Manejar el recibimiento de un paquete
	  Integer sourceId = pkt.sourceid;
	  
	  // Ver si cambió mi costo del link y si actualizo mi vector de distancias
	  // entonces reenviarlo a mis vecinos
	  HashMap<Integer, Integer> oldVector = new HashMap<Integer, Integer>();
	  this.myDistanciasMinimas.forEach((idRouterDestino, costo) -> {
	    	oldVector.put(idRouterDestino, costo);
	  });
	  
	  //Seteo el vector de distancia del vecino que me mando el pkt
	  this.DistanceVectorDeVecinos.put(sourceId, pkt.mincost);
	  
	  boolean vectorCambiado = false;
	  // Recalculo mis costos
	  
	  this.myDistanciasMinimas.forEach((idRouterDestino, costo)->{
		  if(idRouterDestino != this.myID) {
			  this.myDistanciasMinimas.put(idRouterDestino, sim.INFINITY);
		  }
	  });
	   
	  pkt.mincost.forEach((idDestino, costo)->{
		  if(!this.myDistanciasMinimas.containsKey(idDestino)) {
			  this.myDistanciasMinimas.put(idDestino, sim.INFINITY);
		  }
		  this.myVecinos.forEach((vecino, costoVecino)->{
			  if((this.DistanceVectorDeVecinos.get(vecino)!= null) && this.DistanceVectorDeVecinos.get(vecino).containsKey(idDestino)){
				  Integer costoMedianteVecino = this.myVecinos.get(vecino) + this.DistanceVectorDeVecinos.get(vecino).get(idDestino);
				  if(this.myDistanciasMinimas.get(idDestino) > costoMedianteVecino) {
					  this.myDistanciasMinimas.put(idDestino, costoMedianteVecino);
					  this.myForwardingTable.put(idDestino, vecino);
				  }
			  }
		  });
	  });
	  
	  if(!oldVector.equals(this.myDistanciasMinimas)) {
		  vectorCambiado = true;
	  }
	  
	  if(reversaEnvenenada) {
		  reversaEnvenenada(vectorCambiado);
	  }else {
		  
		  if(vectorCambiado) {
			  //mando actualización a vecinos
			  this.myVecinos.forEach((idRouter, costo)->{
				  RouterPacket packetActualizado = new RouterPacket(this.myID, idRouter, this.myDistanciasMinimas);
				  this.sendUpdate(packetActualizado);
			  });
		  }
	  }
	  

	  //if(vectorCambiado) {
	  printDistanceTable();
	  
	  myGUI.print("\n\n\n");

	  //}
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
							UPDATE LINK COST
  =============================================================================================
  */
  
  
  public void updateLinkCost(int dest, int newcost) {
	  // Ver si cambió mi costo del link y si actualizo mi vector de distancias
	  // entonces reenviarlo a mis vecinos
	  HashMap<Integer, Integer> oldVector = new HashMap<Integer, Integer>();
	  this.myDistanciasMinimas.forEach((idRouterDestino, costo) -> {
	    	oldVector.put(idRouterDestino, costo);
	  });
	  
	  /*//!!!!!!!!!!!!!!! setear el costo a mi mismo en 0 en oldVector, no se si es necesario
	  oldVector.put(this.myID, 0);*/ 
	  
	  boolean vectorCambiado = false;
	  // Recalculo mis costos
	  
	  System.out.println(dest);
	  System.out.println(newcost);
	  
	  this.myVecinos.put(dest, newcost);
	  if(!this.myDistanciasMinimas.containsKey(dest)) {
		  this.myDistanciasMinimas.put(dest, newcost);
		  this.myForwardingTable.put(dest,dest);
	  }
	  
	  if(newcost == sim.INFINITY) {
		  this.myVecinos.remove(dest);	
	  }
	  
	  
	  
	  //Reinicializo mi vector de distanciasMinimas
	  this.myDistanciasMinimas.forEach((idRouterDestino, costo)->{
		  if(idRouterDestino != this.myID) {
			  this.myDistanciasMinimas.put(idRouterDestino, sim.INFINITY);
		  }
	  });
	  
	  this.myDistanciasMinimas.forEach((idDestino, costo)->{
		  this.myVecinos.forEach((vecino, costoVecino)->{
			  if((this.DistanceVectorDeVecinos.get(vecino)!= null) && this.DistanceVectorDeVecinos.get(vecino).containsKey(idDestino)){
				  Integer costoMedianteVecino = this.myVecinos.get(vecino) + this.DistanceVectorDeVecinos.get(vecino).get(idDestino);
				  if(this.myDistanciasMinimas.get(idDestino) > costoMedianteVecino) {
					  this.myDistanciasMinimas.put(idDestino, costoMedianteVecino);
					  this.myForwardingTable.put(idDestino, vecino);
				  }
			  }
		  });
	  });
	  
	  
	  if(!oldVector.equals(this.myDistanciasMinimas)) {
		  vectorCambiado = true;
	  }


	  if(vectorCambiado) {
		  //mando actualización a vecinos
		  this.myVecinos.forEach((idRouter, costo)->{
			  RouterPacket packetActualizado = new RouterPacket(this.myID, idRouter, this.myDistanciasMinimas);
			  this.sendUpdate(packetActualizado);
		  });
	  }
	  
  }
   
  public void reversaEnvenenada(boolean vectorCambiado) {

	  if(vectorCambiado) {
	  HashMap<Integer, Integer> aMandar = new HashMap<Integer, Integer>();  
	  // Mando actualizacion a mis vecinos
	  this.myVecinos.forEach((idRouter, costo) -> {
		  this.myDistanciasMinimas.forEach((k,v)->{				//Ejecutar esto antes de mandarlo cada vez, hace que si cambie algo en la iteracion pasada, se me actualice igual a this.costs sin tener que buscar uno por uno lo que cambie
			  aMandar.put(k, v);			//aMandar = this.costs
		  });

			 this.myForwardingTable.forEach((k,v)->{		//Si la ruta a un nodo, pasa por el vecino al que le voy a mandar mi tabla, le digo que mi valor hacia ese nodo es infinito
				 if(idRouter == v) {
					 aMandar.put(k,sim.INFINITY);
				 }
			 });
			  
	    	RouterPacket routerPacket = new RouterPacket(this.myID, idRouter, aMandar);
	    	this.sendUpdate(routerPacket);
	    });
	}
  }
  
  
  
  /*   OBS: HAY QUE ARREGLAR LAS TABLAS DE IMPRESION INTERMEDIAS QUE QUEDAN CORRIDAS AL PRINCIPIO
  =============================================================================================
							PRINT DISTANCE TABLE
  =============================================================================================
  */
  public void printDistanceTable() {
	  
	  
	  //---------------Título------------------------------
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime() + "\n");
	  
	  myGUI.print("Vector de distancias y rutas:" + "\n\n");

	  //---------------Fila Destino------------------------ 
	  myGUI.print(F.format("Destino       |", 23));
	  
	  this.myDistanciasMinimas.forEach((router, costo) -> {
		  String imprimir = F.format(router, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  myGUI.print("\n");
	  
	  Integer i;
	  for(i=0; i<100; i++) {
		  myGUI.print("¯");
	  }
	  
	  myGUI.print("\n");
	  
	  //---------------Fila Costo------------------------ 
	  
	  myGUI.print(F.format("Costo        |", 24));
	  
	  this.myDistanciasMinimas.forEach((router, costo) -> {
		  String imprimir = F.format(costo, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  
	  myGUI.print("\n\n");
	  
	  //---------------Fila Ruta-------------------------
	  
	  myGUI.print(F.format("Ruta        |", 25));
	  
	  this.myForwardingTable.forEach((idDestino, interfazSalida) -> {
                  if(myDistanciasMinimas.get(idDestino) == 999){
                    String imprimir = "                  -    ";
                    myGUI.print(imprimir + "   ");
                  } else{
                    String imprimir = F.format(interfazSalida, 20);
                    myGUI.print(imprimir + "   ");
		  }
	  });
	  
	  myGUI.println("\n\n\n");
	  
	  //--------------Información sobre los vecinos-------
	  
	  //TABLA COSTOS ENLACES FISICOS A VECINOS
	  
	  myGUI.print("Tabla costos enlaces físicos a vecinos:" + "\n\n");
	  
	  //---------------Fila Vecino------------------------ 
	  myGUI.print(F.format("Vecino       |", 25));
	  
	  this.myVecinos.forEach((nghbr, costoFisico) -> {
		  String imprimir = F.format(nghbr, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  myGUI.print("\n");
	  
	  for(i=0; i<100; i++) {
		  myGUI.print("-");
	  }
	  
	  myGUI.print("\n");
	  
	  //---------------Fila Costo------------------------ 
	  
	  myGUI.print(F.format("Costo        |", 25));
	  
	  this.myVecinos.forEach((nghbr, costoFisico) -> {
		  String imprimir = F.format(costoFisico, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  
	  myGUI.print("\n\n");
	  
	  //TABLA DISTANCE VECTOR VECINOS
	  
	  myGUI.print("Vectores de Distancia de vecinos:" + "\n\n");
	  
	  //---------------Fila Destino------------------------ 
	  myGUI.print(F.format("Destino       |", 25));
	  
	  this.myDistanciasMinimas.forEach((router, costo) -> {
		  String imprimir = F.format(router, 20);
		  myGUI.print(imprimir + "   ");		  
	  });
	  
	  myGUI.print("\n");
	  
	  for(i=0; i<100; i++) {
		  myGUI.print("¯");
	  }
	  
	  myGUI.print("\n");
	  
	  //---------------Filas Vectores------------------------ 
	  
	  this.DistanceVectorDeVecinos.forEach((idVecino, vector)-> {
			  
		  myGUI.print(F.format("vecino " + idVecino + "     |", 25));
		  
		  this.DistanceVectorDeVecinos.get(idVecino).forEach((router, costo) -> {
			  String imprimir = F.format(costo, 20);
			  myGUI.print(imprimir + "   ");		  
		  });
		  
		  myGUI.print("\n");
	  });	  
	  
	  myGUI.print("\n\n");
	  
	  myGUI.print("CANTIDAD UPDATES RECIBIDOS: " + this.cantidadUpdates);
	  myGUI.print("\n\n");
	  
	  
  }

}
