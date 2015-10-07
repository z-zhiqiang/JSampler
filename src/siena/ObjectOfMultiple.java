// -*- Java -*-
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/siena/
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
// $Id: ObjectOfMultiple.java,v 1.1 2000/10/31 01:21:29 carzanig Exp $
//

//
// this is an example of an object of interest, that is, a producer of
// notifications
//
package siena;
import siena.*;

class SimpleMult implements Notifiable {
    Siena siena;

    public SimpleMult(Siena s) {
	siena = s;
    }

    public void notify(Event e) {
	System.out.println("local notifiable: " + e.toString());
	 try {
	     siena.unsubscribe(this);
	 } catch (SienaException ex) {
	     ex.printStackTrace();
	 }
    }

    public void notify(Event [] s) { 
    }
}

public class ObjectOfMultiple {
    public static void main(String args[]) {
	try {
	    Dejavu d = new Dejavu();
	    HierarchicalDispatcher siena;
	    siena = new HierarchicalDispatcher();
	    Compare comp = new Compare();
	    String nullarg;
	    String attr1;
	    byte[] battr1;
	    byte[] battr2;

	    if(args.length < 1){ 
		System.err.println("Usage: ObjectOfMultiple [server-uri]");
//		System.exit(1);
	    }

	    siena.setMaster(args[0]); 
	    
	    if(args.length == 1){
                Event e = new Event();
                nullarg = null;
                e.putAttribute(nullarg, nullarg);
                System.out.println("publishing " + e.toString());
	    }
	    
	    Event e[] = new Event[(args.length)/4];
	    int k = 1;
	    for(int i=0; i< (args.length)/4; ++i){
	   	e[i] = new Event();
		if(args[k].equals("true"))
	    		e[i].putAttribute(args[k+1], true);
		else if(args[k].equals("false"))
	    		e[i].putAttribute(args[k+1], false);
		else
	    		e[i].putAttribute(args[k], args[k+1]);
	    	e[i].putAttribute(args[k+2], args[k+3]);
		battr1 = args[k].getBytes();
		battr2 = args[k+2].getBytes();
		if(comp.equals(battr1, battr2)){ 
                    System.out.println("Error: same name attribute");
//                    System.exit(1);
                }
		k=k+4;
	    }


	    for(int i=0; i< (args.length)/4; ++i)
	    System.out.println("publishing " + e[i].toString());
	    try {
                Thread.sleep(5000);
	    for(int i=0; i< (args.length)/4; ++i)
		siena.publish(e[i]);
	    } catch (SienaException ex) {
		System.err.println("Siena error:" + ex.toString());
	    }
	    System.out.println("shutting down.");
	    siena.shutdown();
	} catch (Exception ex) {
	    ex.printStackTrace();
//	    System.exit(1);
	}
    }
}
