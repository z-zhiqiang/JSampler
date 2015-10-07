// Usage: OpTest port_no #ofevents unsub_opt
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/dot/siena.html
//
//  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2000 University of Colorado
//
//  This program is free software; you can redistribute it and/or
//  modify it under the terms of the GNU General Public License
//  as published by the Free Software Foundation; either version 2
//  of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
//  USA, or send email to serl@cs.colorado.edu.
//
//
// $Id: TestFilter.java,v 1.1 2000/04/13 16:49:48 carzanig Exp $
//
package siena;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OpTest implements Notifiable{

    public void notify(Event e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Event [] s) {}

    public static void main(String[] args) {

        HierarchicalDispatcher siena;
	OpCov opcov;
        Dejavu d = new Dejavu();
	int count_evt = Integer.decode(args[1]).intValue();

	try {
	    BufferedReader in
		= new BufferedReader(new InputStreamReader(System.in));
	    String sexpect;
	    String sf1;
	    String sf2;
	    SENPPacket f1, f2;
	    boolean expected;
	    boolean nexpected;

            siena = new HierarchicalDispatcher();
            opcov = new OpCov();
            siena.setMaster(args[0]);

	    OpTest party = new OpTest();

	    while ((sexpect = in.readLine()) != null) {
	      expected = sexpect.equals("filter");
		sf1 = in.readLine(); 
		if (sf1 == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f1 = SENP.decode(sf1.getBytes());
                  System.out.println("subscribing for " + f1.filter.toString());
		  opcov.add_con(f1.filter);
		  siena.subscribe(f1.filter, party);
	      
	      if((sexpect = in.readLine()) != null){
		nexpected = sexpect.equals("event");
	      for(int i=0;i< count_evt;i++){
	      if(nexpected){
		sf2 = in.readLine(); 
		if (sf2 == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f2 = SENP.decode(sf2.getBytes());

		if (f1.filter == null || f2.event == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		// opcov.app performs f2.putAttribute
		if (expected != opcov.app(f1.filter, f2.event)) {
		    System.err.println(sf1 + " " + sf2);
		    System.err.println("Error: expecting " + sexpect);
//		    System.exit(1);
		}
                System.out.println("publishing for " + f2.event.toString());
		siena.publish(f2.event);
	      }
	      else{
		sf1 = in.readLine(); 
		if (sf1 == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f1 = SENP.decode(sf1.getBytes());
                System.out.println("subscribing for " + f1.filter.toString());
		opcov.add_con(f1.filter);
		siena.subscribe(f1.filter, party);
	      }
	     }
	    }
            if(args[2].equals("unsub")){
                System.out.println("unsubscribing with filter");
                siena.unsubscribe(f1.filter, party);
            }
            if(args[2].equals("unsub_nofilter")){
                System.out.println("unsubscribing without filter");
                siena.unsubscribe(party);
            }
	    }
 	
	} catch (Exception ex) {
	    ex.printStackTrace();
//	    System.exit(1);
	}
    }
}
