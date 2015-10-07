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
// $Id: InterestedMultiple.java,v 1.1 2000/10/31 01:21:32 carzanig Exp $
//

//
// this is an example of an interested party, that is, a consumer of
// notifications
//
package siena;

import siena.*;

public class InterestedMultiple implements Notifiable {
    public void notify(Event e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Event [] s) { 
    }

    public static void main(String args[]) {
	if(args.length < 1) {
	    System.err.println("Usage: InterestedMultiple <server-uri>");
//	    System.exit(1);
	}
	
	HierarchicalDispatcher siena;
	Dejavu d = new Dejavu();

	try {
	    siena = new HierarchicalDispatcher();
	    siena.setMaster(args[0]);

	    Filter f[] = new Filter[(args.length)/4];
	    int k = 1; 
	    for(int i = 0; i< (args.length)/4; ++i){
		f[i] = new Filter();
		if(args[k].equals("true"))
	    	   f[i].addConstraint(args[k+1], AttributeConstraint.EQ, true);
		else if(args[k].equals("false"))
	    	   f[i].addConstraint(args[k+1], AttributeConstraint.EQ, false);
		else
	    	   f[i].addConstraint(args[k], AttributeConstraint.EQ, args[k+1]);
	    	f[i].addConstraint(args[k+2], AttributeConstraint.GT, args[k+3]);
		k=k+4;
	    }
	
	    InterestedMultiple party = new InterestedMultiple();
	    
	    for(int i = 0; i< (args.length)/4; ++i){
	    }
	    try {
		for(int i = 0; i< (args.length)/4; ++i){
	    	    System.out.println("subscribing for " + f[i].toString());
		    siena.subscribe(f[i], party);
	        }
                if(args[k].equals("unsub")){
                        System.out.println("unsubscribing");
                        for(int i = 0; i< (args.length)/4; ++i)
                            siena.unsubscribe(f[i], party);
                }

		try {
		    Thread.sleep(5000);	// sleeps for five seconds
		} catch (java.lang.InterruptedException ex) {
		    System.out.println("interrupted"); 
		}
		System.out.println("unsubscribing");
	    	   for(int i = 0; i< (args.length)/4; ++i)
		      siena.unsubscribe(f[i], party);
	    } catch (SienaException ex) {
		System.err.println("Siena error:" + ex.toString());
	    }
	    System.out.println("shutting down.");
	    siena.shutdown();
//	    System.exit(0);
	} catch (Exception ex) {
	    ex.printStackTrace();
//	    System.exit(1);
	} 
    }
}
