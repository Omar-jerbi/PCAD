package main;


import java.rmi.server.RMIClassLoader;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Main2 {
	public static final int NUMERO_EVENTI = 3;
	
	static enum EVNAMES{
		EVENTOESTATE, EVENTOPRIMAVERA, EVENTOAUTUNNO, EVENTOINVERNO,  EVENTONATALE 
	}
	
	
	
	public static class Eventi{
		 
		private ConcurrentMap<EVNAMES, Integer> eventi;
		boolean REMVD = false;


		public Eventi() {
			eventi = new ConcurrentHashMap<EVNAMES, Integer>();
		}
	
	
		public synchronized void listaEventi(String s) {
			System.out.println("\nSTATO EVENTI =="+s+"==:");
			var keys = eventi.keySet();
			if(keys.size() == 0) System.out.println("\tNO EVENTI");
			for(var k : keys) {
				System.out.println("\t"+k +" POSTI: "+ eventi.get(k));
			}

			System.out.println();
		}


		public synchronized void crea(EVNAMES eve, int i){
			if(eventi.putIfAbsent(eve, i) == null) {
				System.out.println(eve.toString() +"  --CREATO con posti: "+i);
				notify();
			}
		}


		public synchronized void aggiungi(EVNAMES eve, int i) {
			eventi.replace(eve, eventi.get(eve) + i);
			System.out.println(eve.toString() +"  --AGGIUNTI posti: "+i+"  --TOT= "+eventi.get(eve));

			notify();

		}


		public synchronized void chiudi(EVNAMES eve){
			eventi.remove(eve);
			REMVD = true;
			notify();
		}


		public synchronized void prenota(EVNAMES eve, int postiRichiesti) throws InterruptedException {
			System.out.println(eve+ " --> RICHIESTA POSTI: "+postiRichiesti);
			while((eventi.get(eve) == null || eventi.get(eve) < postiRichiesti)) {
				wait();
				if(REMVD) 
					break;	
			}
			
			if(REMVD) {
				REMVD = false;				
			}else {
				eventi.replace(eve, eventi.get(eve) - postiRichiesti);
				System.out.println(eve.toString()+" --PRENOTATI posti: "+postiRichiesti+" --TOT= "+eventi.get(eve));								
			}

		}
	
	
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		
		Eventi ev = new Eventi();
		
		Thread ADMIN = new Thread(new Runnable() {
			public void run() {
			while(true) {	
						ev.listaEventi("INIZIO");
				
				for(int i = 0 ; i<NUMERO_EVENTI; i++) {
					try {
						EVNAMES eve = EVNAMES.values()[i];
						
						ev.crea(eve, i*10+50);
						Thread.sleep(1000);
						
						ev.aggiungi(eve, i*20+20);
						Thread.sleep(1000);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
						ev.listaEventi("PRIMA CHIUSURA");
	
				//chiusura evento random
					var r = new Random();
					int randEvent = r.nextInt(NUMERO_EVENTI);
					ev.chiudi(EVNAMES.values()[randEvent]);
				
						ev.listaEventi("DOPO CHIUSURA");
			}	
			
			}
		});
		
		Thread UTENTE = new Thread(new Runnable() {
			public void run() {
				while(true) {	
					var rand = new Random();
					int postiRichiesti = rand.nextInt(100) + 10; 	//10 --> 109
					
					try {
						for(int i = 0; i<NUMERO_EVENTI; i++) {
							var r = new Random();
							int randEvent = r.nextInt(NUMERO_EVENTI);
							ev.prenota(EVNAMES.values()[randEvent] , postiRichiesti );						
						}		
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				
				}	
				
			}
		});
		
		
		System.out.println("NUM EVENTI: "+NUMERO_EVENTI);
		
		ADMIN.start();
		UTENTE.start();
		

	}

}
