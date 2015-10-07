// -*- Java -*-
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/siena
//
//  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-1999 University of Colorado
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
// $Id: InterestedParty.java,v 1.1 2000/10/31 01:21:32 carzanig Exp $
//

//
// this is an example of an interested party, that is, a consumer of
// notifications
//
package siena;

import siena.*;

public class InterestedParty implements Notifiable {
    public void notify(Event e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Event [] s) { 
    }

    public static void main(String args[]) {
	if(args.length < 1) {
	    System.err.println("Usage: InterestedParty <server-uri>");
//	    System.exit(1);
	}
	
	HierarchicalDispatcher siena;
	Dejavu d = new Dejavu();
	short relop = 0;

	try {
	    siena = new HierarchicalDispatcher();
	    siena.setMaster(args[0]);
	    String nullarg;

            if(args.length == 1){
                Filter f = new Filter();
                nullarg = null;
	    	f.addConstraint(nullarg, AttributeConstraint.EQ, nullarg);
	    	System.out.println("subscribing for " + f.toString());
            }
	
	    Filter f[] = new Filter[(args.length)/3];
	    	System.out.println("arg_length " + args.length);
	    int k = 1; 
	    for(int i = 0; i< (args.length)/3; ++i){
		f[i] = new Filter();
		if(args[k].equals("true"))
	    	f[i].addConstraint(args[k+2], AttributeConstraint.EQ, true);
		else if(args[k].equals("false"))
	    	f[i].addConstraint(args[k+2], AttributeConstraint.EQ, false);
		else{
//	    	f[i].addConstraint(args[k], AttributeConstraint.EQ, args[k+1]);
		if(args[k+1].equals("1")) // EQ
		   relop = 1;
		if(args[k+1].equals("2")) // LT
		   relop = 2;
		if(args[k+1].equals("3")) // GT
		   relop = 3;
		if(args[k+1].equals("4")) // GE
		   relop = 4;
		if(args[k+1].equals("5")) // LE
		   relop = 5;
		if(args[k+1].equals("6")) // PF(prefix)
		   relop = 6;
		if(args[k+1].equals("7")) // SF(suffix)
		   relop = 7;
		if(args[k+1].equals("8")) // XX 
		   relop = 8;
		if(args[k+1].equals("9")) // NE 
		   relop = 9;
		if(args[k+1].equals("10")) // SS(substring) 
		   relop = 10;
	    	f[i].addConstraint(args[k], relop, args[k+2]);
		}
		k=k+3;
	    }
	
	    InterestedParty party = new InterestedParty();
	    
	    try{
	    	for(int i = 0; i< (args.length)/3; ++i){
	      		System.out.println("subscribing for " + f[i].toString());
	  		siena.subscribe(f[i], party);
	      	}
	      	if(args[k].equals("unsub")){
	  		System.out.println("unsubscribing with filter");
	      		for(int i = 0; i< (args.length)/3; ++i)
	  		    siena.unsubscribe(f[i], party);
	      	}
	      	if(args[k].equals("unsub_nofilter")){
	  		System.out.println("unsubscribing without filter");
	  		siena.unsubscribe(party);
		}
                try {
                    Thread.sleep(5000);       // sleeps for five seconds
                } catch (java.lang.InterruptedException ex) {
                    System.out.println("interrupted");
                }
                System.out.println("unsubscribing");
            for(int i = 0; i< (args.length)/3; ++i)
                  siena.unsubscribe(f[i], party);

	    } catch (SienaException ex) {
                System.err.println("Siena error:" + ex.toString());
            }
            System.out.println("shutting down.");
            siena.shutdown();
	} catch (Exception ex) {
            ex.printStackTrace();
//            System.exit(1);
        }
    }
}
