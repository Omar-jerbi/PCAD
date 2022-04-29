package main;


import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



/*	COMMENTI SUL LABORATORIO
 *  la parte che ho trovato piu' complicata da implementare e' stata
 *  il metodo chiudi() , dopo svariati tentativi mi sembra che questa soluzione 
 *  con mappa  nome_evento -> [posti, flag chiuso] funzioni.
 */


public class MainV6 {
	public static final int NUMERO_EVENTI = 2; //1 --> 5
	
	static enum EVNAMES{
		EVENTOESTATE, EVENTOPRIMAVERA, EVENTOAUTUNNO, EVENTOINVERNO,  EVENTONATALE 
	}
	
	
	
	public static class Eventi{
		
		//key = nome evento : value = [ postiEvento ; flag chiuso SI/NO (=1/0) ]
		private ConcurrentMap<EVNAMES, Integer[]> eventi;
		

		public Eventi() {
			eventi = new ConcurrentHashMap<EVNAMES, Integer[]>();
		}
	
	
		public synchronized void listaEventi(String s) {
			System.out.println("\nSTATO EVENTI =="+s+"==:");
			var keys = eventi.keySet();
			if(keys.size() == 0) System.out.println("\tNO EVENTI");
			for(var k : keys) {
				if(eventi.get(k)[1] == 0) System.out.println("\t"+k +" POSTI: "+ eventi.get(k)[0]);
				if(eventi.get(k)[1] == 1) System.out.println("\t"+k +" CHIUSO !");
			}

			System.out.println();
		}


		public synchronized void crea(EVNAMES eve, int i){
			if(eventi.putIfAbsent(eve, new Integer[] {i, 0}) == null) {
				System.out.println(eve.toString() +"  --CREATO con posti: "+i);
				notify();
			}
		}


		public synchronized void aggiungi(EVNAMES eve, int i) {
			if(eventi.get(eve)[1] == 1) {//aggiunta posti ad un evento rimosso in iterazione precedente
				eventi.remove(eve);
				crea(eve, i);//ri-creazione, con posti = i (si poteva anche ricreare con posti = 0)
			}else {
			eventi.replace(eve,  new Integer[] {eventi.get(eve)[0] + i, 0});
			System.out.println(eve.toString() +"  --AGGIUNTI posti: "+i+"  --TOT= "+eventi.get(eve)[0]);

			notify();
			}

		}


		public synchronized void chiudi(EVNAMES eve){
			eventi.replace(eve,  new Integer[] {0, 1}); // posti 0 ; flag 1 = chiuso
			notify();
		}


		public synchronized void prenota(EVNAMES eve, int postiRichiesti) throws InterruptedException {
			System.out.println(eve+ " --> RICHIESTA POSTI: "+postiRichiesti);
			while(eventi.get(eve) == null || eventi.get(eve)[0] < postiRichiesti) {
				wait();
				if(eventi.get(eve) != null)
					if(eventi.get(eve)[1] == 1)//quando arriva la notify() da chiudi() 
						break;	
			}
			
			if(eventi.get(eve)[1] == 1) {
				System.out.println(eve + "  RIMOSSO :NO PRENOT");	
			}else {
				eventi.replace(eve,  new Integer[] {eventi.get(eve)[0] - postiRichiesti, 0});
				System.out.println(eve.toString()+" --PRENOTATI posti: "+postiRichiesti+" --TOT= "+eventi.get(eve)[0]);								
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
		
		
		System.out.println("NUM EVENTI INIZIALE: "+NUMERO_EVENTI);
		
		ADMIN.start();
		UTENTE.start();
		

	}

}
