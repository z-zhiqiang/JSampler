
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
// $Id: IntMultSub.java,v 1.1 2000/04/13 16:49:48 carzanig Exp $
//
package siena;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;



public class IntMultSub implements Notifiable {
    public void notify(Event e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Event [] s) {
    }

    public static void main(String args[]) {

        HierarchicalDispatcher siena;
        Dejavu d = new Dejavu();


	try {

           if(args.length < 1) {
               System.err.println("Usage: IntMultSub <server-uri> <count>");
//               System.exit(1);
           }

           int count = Integer.decode(args[1]).intValue();

            siena = new HierarchicalDispatcher();
            siena.setMaster(args[0]);

	    BufferedReader in
		= new BufferedReader(new InputStreamReader(System.in));
	    String sexpect;
	    String sf1;
	    SENPPacket f1;
	    boolean expected = false;
	    boolean subtwo = false;
	    boolean submulti = false;
	    boolean subtype = false;

            Filter f[] = new Filter[count];
	    int k=0;
	    while ((sexpect = in.readLine()) != null) {
		expected = sexpect.equals("unsub");
		sexpect = in.readLine(); 
		if(sexpect.equals("two"))
		   subtwo = true;
	    	else if(sexpect.equals("multi"))
		   submulti = true;
		sexpect = in.readLine(); 
		subtype = sexpect.equals("same");
		sf1 = in.readLine(); 
		if (sf1 == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f1 = SENP.decode(sf1.getBytes());
		Iterator it = f1.filter.constraintNamesIterator();
		String name = (String)it.next();
		Iterator li = f1.filter.constraintsIterator(name);
		AttributeConstraint ac = (AttributeConstraint)li.next();
		switch(ac.op){
        	   case AttributeConstraint.XX:
			break;	
      	  	   case AttributeConstraint.EQ:
			break;	
        	   case AttributeConstraint.NE:
			break;	
        	   case AttributeConstraint.SS:
			break;	
        	   case AttributeConstraint.SF:
			break;	
        	   case AttributeConstraint.PF:
			break;	
        	   case AttributeConstraint.LT:
			break;	
        	   case AttributeConstraint.GT:
			break;	
        	   case AttributeConstraint.LE:
			break;	
        	   case AttributeConstraint.GE:
			break;	
        	   default:
//	    	   	System.exit(1);
		}
                f[k] = new Filter();
                f[k].addConstraint("age", ac);
	 	k++;
	    }

            IntMultSub party = new IntMultSub();
            IntMultSub party_2 = new IntMultSub();
            IntMultSub party_3 = new IntMultSub();
            IntMultSub party_4 = new IntMultSub();

            for(int i = 0; i< count; ++i){
                System.out.println("subscribing for " + f[i].toString());
		if(subtwo)
                   System.out.println("subscribing for " + f[i].toString());
		if(submulti){
                   System.out.println("subscribing for " + f[i].toString());
                   System.out.println("subscribing for " + f[i].toString());
                   System.out.println("subscribing for " + f[i].toString());
		}
	    }
            try {
                for(int i = 0; i< count; ++i){
                   siena.subscribe(f[i], party);
		   if(subtwo && subtype)
                     siena.subscribe(f[i], party);
		   else if(subtwo && !subtype)
                     siena.subscribe(f[i], party_2);
		   else if(submulti){
                     siena.subscribe(f[i], party_2);
                     siena.subscribe(f[i], party_3);
                     siena.subscribe(f[i], party_4);
		   }
			
		}
                try {
                    Thread.sleep(30);       // sleeps for five minutes

                } catch (java.lang.InterruptedException ex) {
                    System.out.println("interrupted");
                }
	      if(expected){
                System.out.println("unsubscribing 1");
		if(subtwo)
               	   System.out.println("unsubscribing 2");
		if(submulti){
               	   System.out.println("unsubscribing 2");
               	   System.out.println("unsubscribing 3");
               	   System.out.println("unsubscribing 4");
		}
                for(int i = 0; i< count; ++i){
                    siena.unsubscribe(f[i], party);
	      	    if(subtwo && subtype)
                       siena.unsubscribe(f[i], party);
		    else if(subtwo && !subtype)
                       siena.unsubscribe(f[i], party_2);
		    else if(submulti){
                       siena.unsubscribe(f[i], party_2);
                       siena.unsubscribe(f[i], party_3);
                       siena.unsubscribe(f[i], party_4);
		    }
	        }
	      }
            } catch (SienaException ex) {
                System.err.println("Siena error:" + ex.toString());
            }
//            System.out.println("shutting down.");
//            siena.shutdown();
//            System.exit(0);

	} catch (Exception ex) {
	    ex.printStackTrace();
//	    System.exit(1);
	}
    }
}
