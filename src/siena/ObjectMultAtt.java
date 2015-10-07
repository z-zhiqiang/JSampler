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
// $Id: Object.java,v 1.1 2000/10/31 01:21:29 carzanig Exp $
//

//
// this is an example of an object of interest, that is, a producer of
// notifications
//
package siena;
import siena.*;

class SimpleNotMul implements Notifiable {
    Siena siena;

    public SimpleNotMul(Siena s) {
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

public class ObjectMultAtt {
    public static void main(String[] args) {
	    HierarchicalDispatcher siena;
	    Dejavu d = new Dejavu();
	try {
	    siena = new HierarchicalDispatcher();
	    int count = Integer.decode(args[1]).intValue();
	   
	    switch(args.length) {
	    case 2: siena.setMaster(args[0]); 
	    case 0: break;
	    default:
		System.err.println("Usage: Object [server-uri]");
//		System.exit(1);
	    }
	    
//	    Filter f = new Filter();
//	    f.addConstraint("name", AttributeConstraint.SF, "");
//	    if(count >=3){
//	      f.addConstraint("age", 30);
//	      f.addConstraint("state", "Oregon");
//	    }
//	    
//	    siena.subscribe(f, new SimpleNotMul(siena));
	    
	    Event e = new Event();
	    e.putAttribute("name", "Antonio");
	    if(count >= 2)
	    e.putAttribute("age", 30);
	    if(count >= 3)
	    e.putAttribute("state", "italian");
	    if(count >= 4)
	    e.putAttribute("att_4", "val_4");
	    if(count >= 5)
	    e.putAttribute("att_5", "val_5");
	    if(count >= 6)
	    e.putAttribute("att_6", "val_6");
	    if(count >= 7)
	    e.putAttribute("att_7", "val_7");
	    if(count >= 8)
	    e.putAttribute("att_8", "val_8");
	    if(count >= 9)
	    e.putAttribute("att_9", "val_9");
	    if(count >= 10)
	    e.putAttribute("att_10", "val_10");
	    if(count >= 100){
	    e.putAttribute("att_11", "val_11");
	    e.putAttribute("att_12", "val_12");
	    e.putAttribute("att_13", "val_13");
	    e.putAttribute("att_14", "val_14");
	    e.putAttribute("att_15", "val_15");
	    e.putAttribute("att_16", "val_16");
	    e.putAttribute("att_17", "val_17");
	    e.putAttribute("att_18", "val_18");
	    e.putAttribute("att_19", "val_19");
	    e.putAttribute("att_20", "val_20");
	    e.putAttribute("att_21", "val_21");
	    e.putAttribute("att_22", "val_22");
	    e.putAttribute("att_23", "val_23");
	    e.putAttribute("att_24", "val_24");
	    e.putAttribute("att_25", "val_25");
	    e.putAttribute("att_26", "val_26");
	    e.putAttribute("att_27", "val_27");
	    e.putAttribute("att_28", "val_28");
	    e.putAttribute("att_29", "val_29");
	    e.putAttribute("att_30", "val_30");
	    e.putAttribute("att_31", "val_31");
	    e.putAttribute("att_32", "val_32");
	    e.putAttribute("att_33", "val_33");
	    e.putAttribute("att_34", "val_34");
	    e.putAttribute("att_35", "val_35");
	    e.putAttribute("att_36", "val_36");
	    e.putAttribute("att_37", "val_37");
	    e.putAttribute("att_38", "val_38");
	    e.putAttribute("att_39", "val_39");
	    e.putAttribute("att_40", "val_40");
	    e.putAttribute("att_41", "val_41");
	    e.putAttribute("att_42", "val_42");
	    e.putAttribute("att_43", "val_43");
	    e.putAttribute("att_44", "val_44");
	    e.putAttribute("att_45", "val_45");
	    e.putAttribute("att_46", "val_46");
	    e.putAttribute("att_47", "val_47");
	    e.putAttribute("att_48", "val_48");
	    e.putAttribute("att_49", "val_49");
	    e.putAttribute("att_50", "val_50");
	    e.putAttribute("att_51", "val_51");
	    e.putAttribute("att_52", "val_52");
	    e.putAttribute("att_53", "val_53");
	    e.putAttribute("att_54", "val_54");
	    e.putAttribute("att_55", "val_55");
	    e.putAttribute("att_56", "val_56");
	    e.putAttribute("att_57", "val_57");
	    e.putAttribute("att_58", "val_58");
	    e.putAttribute("att_59", "val_59");
	    e.putAttribute("att_60", "val_60");
	    e.putAttribute("att_61", "val_61");
	    e.putAttribute("att_62", "val_62");
	    e.putAttribute("att_63", "val_63");
	    e.putAttribute("att_64", "val_64");
	    e.putAttribute("att_65", "val_65");
	    e.putAttribute("att_66", "val_66");
	    e.putAttribute("att_67", "val_67");
	    e.putAttribute("att_68", "val_68");
	    e.putAttribute("att_69", "val_69");
	    e.putAttribute("att_70", "val_70");
	    e.putAttribute("att_71", "val_71");
	    e.putAttribute("att_72", "val_72");
	    e.putAttribute("att_73", "val_73");
	    e.putAttribute("att_74", "val_74");
	    e.putAttribute("att_75", "val_75");
	    e.putAttribute("att_76", "val_76");
	    e.putAttribute("att_77", "val_77");
	    e.putAttribute("att_78", "val_78");
	    e.putAttribute("att_79", "val_79");
	    e.putAttribute("att_80", "val_80");
	    e.putAttribute("att_81", "val_81");
	    e.putAttribute("att_82", "val_82");
	    e.putAttribute("att_83", "val_83");
	    e.putAttribute("att_84", "val_84");
	    e.putAttribute("att_85", "val_85");
	    e.putAttribute("att_86", "val_86");
	    e.putAttribute("att_87", "val_87");
	    e.putAttribute("att_88", "val_88");
	    e.putAttribute("att_89", "val_89");
	    e.putAttribute("att_90", "val_90");
	    e.putAttribute("att_91", "val_91");
	    e.putAttribute("att_92", "val_92");
	    e.putAttribute("att_93", "val_93");
	    e.putAttribute("att_94", "val_94");
	    e.putAttribute("att_95", "val_95");
	    e.putAttribute("att_96", "val_96");
	    e.putAttribute("att_97", "val_97");
	    e.putAttribute("att_98", "val_98");
	    e.putAttribute("att_99", "val_99");
	    e.putAttribute("att_100", "val_100");
	    }

	    System.out.println("publishing " + e.toString());
	    try {
		Thread.sleep(5000);
		siena.publish(e);
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
