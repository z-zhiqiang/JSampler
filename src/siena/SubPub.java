
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

public class SubPub implements Notifiable{

    public void notify(Event e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Event [] s) {}

    public static void main(String[] args) {

        HierarchicalDispatcher siena;
	OpCov opcov;
        Dejavu d = new Dejavu();

	try {
	    BufferedReader in
		= new BufferedReader(new InputStreamReader(System.in));
	    String sexpect;
	    String[] sf1 = new String[5];
	    String[] sf2 = new String[5];
	    SENPPacket[] f1 = new SENPPacket[5];
	    SENPPacket[] f2 = new SENPPacket[5];
	    int i, f_cnt, e_cnt, cnt;
	    int j, k;
	    boolean expected;
	    boolean nexpected;

            siena = new HierarchicalDispatcher();
            opcov = new OpCov();
            siena.setMaster(args[0]);

	    SubPub party = new SubPub();

	    while ((sexpect = in.readLine()) != null) {
	      expected = sexpect.equals("filter");
		f_cnt = Integer.decode(in.readLine()).intValue();
		for(i=0;i<f_cnt;i++){
		sf1[i] = in.readLine(); 
		if (sf1[i] == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f1[i] = SENP.decode(sf1[i].getBytes());
                System.out.println("subscribing for " + f1[i].filter.toString());
		opcov.add_con(f1[i].filter);
		siena.subscribe(f1[i].filter, party);
		}

	      if((sexpect = in.readLine()) != null){
		nexpected = sexpect.equals("event");
	      if(nexpected){
		e_cnt = Integer.decode(in.readLine()).intValue();
		for(i=0;i<e_cnt;i++){
		sf2[i] = in.readLine(); 
		if (sf2[i] == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f2[i] = SENP.decode(sf2[i].getBytes());
		}

		if(f_cnt >= e_cnt)
		    cnt = f_cnt;
		else
		    cnt = e_cnt;
		for(i=0;i<cnt;i++){
		if(f_cnt < e_cnt){
		   j=0;
		   k=i;
		}
		else{
		   j=i; 
		   k=0;
		}
		if (f1[j].filter == null || f2[k].event == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		if (expected != opcov.app(f1[j].filter, f2[k].event)) {
		    System.err.println(sf1[j] + " " + sf2[k]);
		    System.err.println("Error: expecting " + sexpect);
//		    System.exit(1);
		}
		}

		for(i=0;i<e_cnt;i++){
                System.out.println("publishing for " + f2[i].event.toString());
		siena.publish(f2[i].event);
		}
	      }
	      else{
		f_cnt = Integer.decode(in.readLine()).intValue();
		for(i=0;i<f_cnt;i++){
		sf1[i] = in.readLine(); 
		if (sf1[i] == null) {
		    System.err.println("bad input format");
//		    System.exit(1);
		}
		f1[i] = SENP.decode(sf1[i].getBytes());
                System.out.println("subscribing for " + f1[i].filter.toString());
		opcov.add_con(f1[i].filter);
		siena.subscribe(f1[i].filter, party);
		}
	      }
	     }
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
//	    System.exit(1);
	}
    }
}
