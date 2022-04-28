package main;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main_pe {

	public static class Evento{
		private String nome;
		private int posti;
		
		public Evento(String n, int p) {
			nome = n;
			setPosti(p);
		}

		public String getNome() {
			return nome;
		}

		public int getPosti() {
			return posti;
		}

		
		public synchronized void setPosti(int posti) {
			this.posti = posti;
		}
		
	
	}
	
	
	public static class Eventi{
		
		private LinkedList<Evento> eventi = null;
		
		
		public Eventi() {
			eventi = new LinkedList<Evento>();
		}
		
		public synchronized void crea(String n, int p) throws InterruptedException {
//			System.out.println("quo");
			
			var test = false;
			for(var e : eventi) {
				if(e.getNome().equals(n)) test = true;
			}
			
			
			if(!test) {
				eventi.add(new Evento(n, 0));
				System.out.println(n+" CREATO CON POSTI = 0");
				aggiungi(n, p);
			}
//			System.out.println("qua");

		}
		
	
		public synchronized void aggiungi(String n, int p) {
//			System.out.println("que");
			var aux = find(n);
			
			if(aux != null) {
				aux.setPosti(p + aux.getPosti());
				if(p>=0) System.out.println(n + " AGGIUNTI  "+ p + " ; TOT ATTUALE = "+aux.getPosti());
				else 	System.out.println("prenotati "+-p+" da "+n + " ; rimanenti= " + aux.getPosti());
				notifyAll();

			}

		}
		
		
		public Evento find(String n) {
			Evento aux = null;
			for(var e : eventi) {
				if(e.getNome().equals(n)) aux = e;
			}
			
			return aux;
		}
		
		public synchronized void prenota(String n, int p) throws InterruptedException {
			while(find(n) == null) {
				System.out.println(n+" NON DISPONIBILE");
				wait();
			}
			
			var aux = find(n);
			
			while(aux.getPosti() - p < 0) {
				System.out.println(n+" POSTI= "+ aux.getPosti() + "; POSTI NON DISPONIBILI RICHIESTI: "+p);
				wait();
			}
			
			aggiungi(n, -p);


		}
		
		public void listaEventi() {
			for(var e : eventi) {
				System.out.print("\t"+e.getNome() + " : ");
				System.out.println(e.getPosti());
			}
		}
		
		public synchronized void chiudi(String n) {
			Evento aux = null;
			for(var e : eventi) {
				if(e.getNome().equals(n)) aux = e;
			}
			
			if(aux != null) {
				eventi.remove(aux);
				notifyAll();
			}
			
		}
		
		
		

		public LinkedList<Evento> getEventi() {
			return eventi;
		}
		
		public int getPostiEvento(String n) {
			Evento aux = null;
			for(var e : eventi) {
				if(e.getNome().equals(n)) aux = e;
			}
			
			return aux.getPosti();
		}
	}
	
	
	static enum EVNAMES{
		EVENTOESTATE, EVENTOPRIMAVERA, EVENTOAUTUNNO, EVENTOINVERNO,  EVENTONATALE 
	}
	
	
	public static String RandEvento() {
		int rN = (int) Math.floor(Math.random()*(4-0+1)+0);
		
		return EVNAMES.values()[rN].toString();
	}
	
	
	
	
//FINISCI METODO "chiudi()"
//togli commenti	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws InterruptedException {
		Eventi ev = new Eventi();
				
		Thread ADMIN = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					
					System.out.println("\nSTATO ATTUALE EVENTI:");
					ev.listaEventi();
					System.out.println();
					
					
					for(int i = 0 ; i<3; i++) {
						try {
							String eve = EVNAMES.values()[i].toString();
							
//							System.out.println("qui");
							
							ev.crea(eve, i*10+50);
							Thread.sleep(2000);
							
							ev.aggiungi(eve, i*20+20);
							Thread.sleep(2000);
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				
			}
				
			
			}
		});
		
		
		
		
		Thread UTENTE = new Thread(new Runnable() {
			@Override
			public void run() {
				
				while(true) {
					Random randS = new Random();
		int r =randS.nextInt(3);//0 o 1 o 2
					String s = EVNAMES.values()[r].toString();
					
					int postiRichiesti = 60;
					System.out.println("\nNUOVA RICHIESTA PRENOTAZIONE DI POSTI= "+postiRichiesti+" da " + s);
					
					try {
						ev.prenota(s, postiRichiesti);			
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				
				}
			}
		});
		
		
		ADMIN.start();
		UTENTE.start();
		
		ADMIN.join();
		UTENTE.join();

		
		
//		
//		Thread tP = new Thread(new Runnable() {
//			@Override 
//			public void run() {
//				try {
//					ev.crea("e1", 10);
//					System.out.println("creato");
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//				ev.aggiungi("e1", 20);
//				System.out.println("aggiunti 20");
//			}
//		});
//	
//		
//		
//		Thread tC = new Thread(new Runnable() {
//			@Override 
//			public void run() {			
//				
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
//				
//				try {
//					System.out.println("provo prenota 40");
//					ev.prenota("e1", 40);
//					System.out.println("prenotati 40");
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		
//		
//		Thread tAGG = new Thread(new Runnable() {						
//			@Override 
//			public void run() {				
//				
//				
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//				
//				ev.aggiungi("e1", 30);
//				System.out.println("aggiunti 30");
//			}
//		});
//		
//		
//		tP.start();
//		tC.start();
//		tAGG.start();
//		
//		
//		
//		
//		tP.join();
//		tC.join();
//		tAGG.join();
		
	}

}
