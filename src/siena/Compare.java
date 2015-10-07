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
// $Id: Compare.java,v 1.1 2000/04/17 17:40:25 carzanig Exp $
//
package siena;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

public class Compare {

    public static List readPackets(String filename) 
	throws java.io.IOException, siena.SENPInvalidFormat {
	List l = new LinkedList();
	BufferedReader file = new BufferedReader(new FileReader(filename));
	String inl;
	while ((inl = file.readLine()) != null) {
	    byte[] pkt = inl.getBytes();
	    if (pkt.length > 0 && pkt[0] != 0x23 /* '#' */) {
		//		System.out.println(inl);
		// skip blank lines and comments
		l.add(SENP.decode(pkt));
	    }
	}
	return l;
    }

    public static boolean equals(Event a, Event b) {
	if (a.size() != b.size()) return false;

	Iterator ai, bi;
	ai = a.attributeNamesIterator();
	bi = b.attributeNamesIterator();
	while(ai.hasNext()) {
	    String aname = (String)ai.next();
	    String bname = (String)bi.next();
	    //
	    // I know that the set of attribute names is ordered!
	    //
	    if(!aname.equals(bname)) {
		return false;
	    } else {
		AttributeValue aav = a.getAttribute(aname);
		AttributeValue bav = b.getAttribute(aname);
		if (!Covering.apply_operator(AttributeConstraint.EQ,aav,bav)) 
		    return false;
	    }
	}
	return true;
    }

    public static boolean equals(Filter a, Filter b) {
	//
	// this is meant to be a syntactical equality, but I have no
	// time to implement it that way now, so I'm relying on the
	// covering relations (semantic equivalence)
	//
	return Covering.covers(a,b) && Covering.covers(b,a);
    }

    public static boolean equals(byte[] a, byte[] b) {
	if (a == null) return b == null;
	if (b == null || b.length != a.length) return false;
	for(int i=0; i<a.length; ++i) 
	    if (a[i] != b[i]) return false;
	return true;
    }

    public static boolean equals(SENPPacket a, SENPPacket b) {

	if (a.method != b.method) return false;
	if (!equals(a.id, b.id)) return false;
	if (!equals(a.to, b.to)) return false;

	if (a.filter != null || b.filter != null) {
	    if (b.filter == null || a.filter == null) return false;
	    return equals(a.filter, b.filter);
	} else if (a.event != null || b.event != null) {
	    if (b.event == null || a.event == null) return false;
	    return equals(a.event, b.event);
	}
	return true;
    }
    
    public static void main(String[] args) {
	boolean ok = true;
	try {
	    if (args.length != 2) {
		System.err.println("usage: Compare <actual> <expected>\n");
//		System.exit(1);
	    }
	    List actual, expected;
	    actual = readPackets(args[0]);
	    expected = readPackets(args[1]);

	    boolean found;
	    Iterator ei, ai;
	    SENPPacket ep, ap;

	    ei = expected.iterator();
	    while(ei.hasNext()) {
		ep = (SENPPacket)ei.next();
		ai = actual.iterator();
		found=false;
		while(ai.hasNext()) {
		    ap = (SENPPacket)ai.next();
		    if (equals(ap, ep)) {
			found=true;
			ai.remove();
			break;
		    }
		}
		if (!found) {
		    if (ok) {
			ok = false;
			System.out.println(">>> MISSING >>>");
		    }
		    System.out.println(new String(SENP.encode(ep)));
		}
	    }
	    if (!actual.isEmpty()) {
		ok = false;
		System.out.println("<<< EXTRA <<<");
		ai = actual.iterator();
		while(ai.hasNext()) {
		    ap = (SENPPacket)ai.next();
		    System.out.println(new String(SENP.encode(ap)));
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
//	    System.exit(1);
	}
//	System.exit(ok ? 0 : 1);
    }
}
