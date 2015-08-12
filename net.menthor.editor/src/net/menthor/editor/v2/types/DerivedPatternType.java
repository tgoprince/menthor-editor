package net.menthor.editor.v2.types;

/**
 * ============================================================================================
 * Menthor Editor -- Copyright (c) 2015 
 *
 * This file is part of Menthor Editor. Menthor Editor is based on TinyUML and as so it is 
 * distributed under the same license terms.
 *
 * Menthor Editor is free software; you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation; either 
 * version 2 of the License, or (at your option) any later version.
 *
 * Menthor Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Menthor Editor; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA  02110-1301  USA
 * ============================================================================================
 */

public enum DerivedPatternType {

	UNION("Union"), 
	EXCLUSION("Exclusion"), 
	INTERSECTION("Intersection"), 
	SPECIALIZATION("Specialization"), 
	PASTSPECIALIZATION("Past Specialization"), 
	PARTICIPATION("Participation");

	private String name;

	DerivedPatternType(String name)
	{
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() { return name; }

	public static void main (String args[])
	{
		for(DerivedPatternType c: DerivedPatternType.values()){
			System.out.println(c.name);
		}
	}
}
