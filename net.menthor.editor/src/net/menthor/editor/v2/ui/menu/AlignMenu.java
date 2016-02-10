package net.menthor.editor.v2.ui.menu;

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

import java.util.ArrayList;

import javax.swing.JPopupMenu;

import org.tinyuml.umldraw.shared.UmlDiagramElement;

import net.menthor.editor.v2.commands.ICommandListener;
import net.menthor.editor.v2.commands.CommandType;

public class AlignMenu extends MultiElementMenu {

	private static final long serialVersionUID = 3797953970276009760L;
	
	public AlignMenu(ICommandListener listener, String text, ArrayList<UmlDiagramElement> elements, JPopupMenu parent){
		super(listener, text,elements);			
		createMenuItem("Horizontal Center", CommandType.ALIGN_HORIZONTAL);
		createMenuItem("Vertical Center", CommandType.ALIGN_VERTICAL);
		createMenuItem("Top", CommandType.ALIGN_TOP);				
		createMenuItem("Bottom", CommandType.ALIGN_BOTTOM);
		createMenuItem("Left", CommandType.ALIGN_LEFT);
		createMenuItem("Right", CommandType.ALIGN_RIGHT);
		
		parent.add(this);
	}
	
	public AlignMenu(ICommandListener listener, ArrayList<UmlDiagramElement> elements, JPopupMenu parent){
		this(listener, "Align", elements, parent);		
  	}
}
