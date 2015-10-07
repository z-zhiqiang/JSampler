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

public class IntMult implements Notifiable {
    public void notify(Event e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Event [] s) { 
    }

    public static void main(String args[]) {
	if(args.length < 1) {
	    System.err.println("Usage: IntMult <server-uri>");
//	    System.exit(1);
	}
	
	HierarchicalDispatcher siena;
	Dejavu d = new Dejavu();

	try {
	    siena = new HierarchicalDispatcher();
	    siena.setMaster(args[0]);
           int count = Integer.decode(args[1]).intValue();

	    Filter f = new Filter();
		f = new Filter();
	    	f.addConstraint("name", AttributeConstraint.EQ, "Ann");
		if(count >= 2)
	    	f.addConstraint("age", AttributeConstraint.LT, 20);
		if(count >= 3)
	    	f.addConstraint("height", AttributeConstraint.NE, 6);
		if(count >= 4)
	    	f.addConstraint("att_4", AttributeConstraint.LE, 4);
		if(count >= 5)
	    	f.addConstraint("att_5", AttributeConstraint.PF, "any");
		if(count >= 6)
	    	f.addConstraint("att_6", AttributeConstraint.GT, 70.8);
		if(count >= 7)
	    	f.addConstraint("att_7", AttributeConstraint.EQ, false);
		if(count >= 8)
	    	f.addConstraint("att_8", AttributeConstraint.GE, 8);
		if(count >= 9)
	    	f.addConstraint("att_9", AttributeConstraint.SS, 9);
		if(count >= 10)
	    	f.addConstraint("att_10", AttributeConstraint.SF, 10);
		if(count >= 100){
	    	f.addConstraint("att_11", AttributeConstraint.EQ, 11);
	    	f.addConstraint("att_12", AttributeConstraint.GE, 12);
	    	f.addConstraint("att_13", AttributeConstraint.LE, 13);
	    	f.addConstraint("att_14", AttributeConstraint.GT, 14);
	    	f.addConstraint("att_15", AttributeConstraint.LT, 15);
	    	f.addConstraint("att_16", AttributeConstraint.NE, 16);
	    	f.addConstraint("att_17", AttributeConstraint.XX, 17);
	    	f.addConstraint("att_18", AttributeConstraint.SS, 18);
	    	f.addConstraint("att_19", AttributeConstraint.PF, 19);
	    	f.addConstraint("att_20", AttributeConstraint.SF, 20);
	    	f.addConstraint("att_31", AttributeConstraint.EQ, 31);
	    	f.addConstraint("att_32", AttributeConstraint.GE, 32);
	    	f.addConstraint("att_33", AttributeConstraint.LE, 33);
	    	f.addConstraint("att_34", AttributeConstraint.GT, 34);
	    	f.addConstraint("att_35", AttributeConstraint.LT, 35);
	    	f.addConstraint("att_36", AttributeConstraint.NE, 36);
	    	f.addConstraint("att_37", AttributeConstraint.XX, 37);
	    	f.addConstraint("att_38", AttributeConstraint.SS, 38);
	    	f.addConstraint("att_39", AttributeConstraint.PF, 39);
	    	f.addConstraint("att_40", AttributeConstraint.SF, 40);
	    	f.addConstraint("att_41", AttributeConstraint.EQ, 41);
	    	f.addConstraint("att_42", AttributeConstraint.GE, 42);
	    	f.addConstraint("att_43", AttributeConstraint.LE, 43);
	    	f.addConstraint("att_44", AttributeConstraint.GT, 44);
	    	f.addConstraint("att_45", AttributeConstraint.LT, 45);
	    	f.addConstraint("att_46", AttributeConstraint.NE, 46);
	    	f.addConstraint("att_47", AttributeConstraint.XX, 47);
	    	f.addConstraint("att_48", AttributeConstraint.SS, 48);
	    	f.addConstraint("att_49", AttributeConstraint.PF, 49);
	    	f.addConstraint("att_50", AttributeConstraint.SF, 50);
	    	f.addConstraint("att_51", AttributeConstraint.EQ, 51);
	    	f.addConstraint("att_52", AttributeConstraint.GE, 52);
	    	f.addConstraint("att_53", AttributeConstraint.LE, 53);
	    	f.addConstraint("att_54", AttributeConstraint.GT, 54);
	    	f.addConstraint("att_55", AttributeConstraint.LT, 55);
	    	f.addConstraint("att_56", AttributeConstraint.NE, 56);
	    	f.addConstraint("att_57", AttributeConstraint.XX, 57);
	    	f.addConstraint("att_58", AttributeConstraint.SS, 58);
	    	f.addConstraint("att_59", AttributeConstraint.PF, 59);
	    	f.addConstraint("att_60", AttributeConstraint.SF, 60);
	    	f.addConstraint("att_61", AttributeConstraint.EQ, 61);
	    	f.addConstraint("att_62", AttributeConstraint.GE, 62);
	    	f.addConstraint("att_63", AttributeConstraint.LE, 63);
	    	f.addConstraint("att_64", AttributeConstraint.GT, 64);
	    	f.addConstraint("att_65", AttributeConstraint.LT, 65);
	    	f.addConstraint("att_66", AttributeConstraint.NE, 66);
	    	f.addConstraint("att_67", AttributeConstraint.XX, 67);
	    	f.addConstraint("att_68", AttributeConstraint.SS, 68);
	    	f.addConstraint("att_69", AttributeConstraint.PF, 69);
	    	f.addConstraint("att_70", AttributeConstraint.SF, 70);
	    	f.addConstraint("att_71", AttributeConstraint.EQ, 71);
	    	f.addConstraint("att_72", AttributeConstraint.GE, 72);
	    	f.addConstraint("att_73", AttributeConstraint.LE, 73);
	    	f.addConstraint("att_74", AttributeConstraint.GT, 74);
	    	f.addConstraint("att_75", AttributeConstraint.LT, 75);
	    	f.addConstraint("att_76", AttributeConstraint.NE, 76);
	    	f.addConstraint("att_77", AttributeConstraint.XX, 77);
	    	f.addConstraint("att_78", AttributeConstraint.SS, 78);
	    	f.addConstraint("att_79", AttributeConstraint.PF, 79);
	    	f.addConstraint("att_80", AttributeConstraint.SF, 80);
	    	f.addConstraint("att_81", AttributeConstraint.EQ, 81);
	    	f.addConstraint("att_82", AttributeConstraint.GE, 82);
	    	f.addConstraint("att_83", AttributeConstraint.LE, 83);
	    	f.addConstraint("att_84", AttributeConstraint.GT, 84);
	    	f.addConstraint("att_85", AttributeConstraint.LT, 85);
	    	f.addConstraint("att_86", AttributeConstraint.NE, 86);
	    	f.addConstraint("att_87", AttributeConstraint.XX, 87);
	    	f.addConstraint("att_88", AttributeConstraint.SS, 88);
	    	f.addConstraint("att_89", AttributeConstraint.PF, 89);
	    	f.addConstraint("att_90", AttributeConstraint.SF, 90);
	    	f.addConstraint("att_91", AttributeConstraint.EQ, 91);
	    	f.addConstraint("att_92", AttributeConstraint.GE, 92);
	    	f.addConstraint("att_93", AttributeConstraint.LE, 93);
	    	f.addConstraint("att_94", AttributeConstraint.GT, 94);
	    	f.addConstraint("att_95", AttributeConstraint.LT, 95);
	    	f.addConstraint("att_96", AttributeConstraint.NE, 96);
	    	f.addConstraint("att_97", AttributeConstraint.XX, 97);
	    	f.addConstraint("att_98", AttributeConstraint.SS, 98);
	    	f.addConstraint("att_99", AttributeConstraint.PF, 99);
	    	f.addConstraint("att_100", AttributeConstraint.SF, 100);
		}
	
	    IntMult party = new IntMult();
	    
	    	System.out.println("subscribing for " + f.toString());
	    try {
		  siena.subscribe(f, party);
		try {
		    Thread.sleep(5000);	// sleeps for five seconds
		} catch (java.lang.InterruptedException ex) {
		    System.out.println("interrupted"); 
		}
		System.out.println("unsubscribing");
		  siena.unsubscribe(f, party);
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
    };
}
