package org.tinyuml.ui.diagram.commands;

/**
 * Copyright 2007 Wei-ju Wu
 *
 * This file is part of TinyUML.
 *
 * TinyUML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * TinyUML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TinyUML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import net.menthor.editor.ui.ModelHelper;
import net.menthor.editor.ui.ProjectBrowser;
import net.menthor.editor.ui.UmlProject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.tinyuml.draw.CompositeElement;
import org.tinyuml.draw.DiagramElement;
import org.tinyuml.ui.diagram.DiagramEditor;
import org.tinyuml.ui.diagram.commands.DiagramNotification.ChangeType;
import org.tinyuml.ui.diagram.commands.DiagramNotification.NotificationType;
import org.tinyuml.umldraw.shared.BaseConnection;
import org.tinyuml.umldraw.shared.UmlConnection;

import RefOntoUML.Association;
import RefOntoUML.Classifier;
import RefOntoUML.Generalization;
import RefOntoUML.Property;
import RefOntoUML.Relationship;
import RefOntoUML.impl.AssociationImpl;
import RefOntoUML.impl.GeneralizationImpl;

/**
 * This is an undoable creation command for a Connection.
 *
 * @author Wei-ju Wu, John Guerson
 */
public class AddConnectionCommand extends BaseDiagramCommand {

	private static final long serialVersionUID = 2924451842640450250L;	
	private CompositeElement parent;
	private DiagramElement diagramElement;	
	private RefOntoUML.Element relationship;
	private Classifier source;
	private Classifier target;
	private EObject eContainer;	
	private boolean addToDiagram;

	public AddConnectionCommand(DiagramNotification editorNotification, CompositeElement parent, RefOntoUML.Element relationship, Classifier aSource, Classifier aTarget, UmlProject project, EObject eContainer) {
		this.parent = parent;
		this.project = project;
		this.notification = editorNotification;
		
		if (notification==null) this.addToDiagram = false; 
		else this.addToDiagram=true;
		
		this.relationship = relationship;		
		source = aSource;
		target = aTarget;
		
		this.eContainer = eContainer;
		
		diagramElement = ModelHelper.getDiagramElementByEditor(relationship,(DiagramEditor)notification);
	}

	@Override
	public void undo() 
	{
		super.undo();
						
		if (relationship!=null){
//			System.out.println("Undoing ="+relationship);
			ModelHelper.createAdapterEditingDomain().getCommandStack().undo();
			ProjectBrowser.frame.getDiagramManager().updateMenthorFromDeletion(relationship);
		}
		
		if(addToDiagram && diagramElement!=null){				
			parent.removeChild(diagramElement);
			ModelHelper.removeMapping(diagramElement);
			
			List<DiagramElement> elements = new ArrayList<DiagramElement>();
			elements.add(diagramElement);
			notification.notifyChange(elements, ChangeType.ELEMENTS_ADDED, NotificationType.UNDO);			
		}	
	}

	@Override
	public void redo() 
	{
		redo = true;
		super.redo();
		run();
	}

	public void run() {	    
					
		ArrayList<DiagramElement> list = new ArrayList<DiagramElement>();
		
		addToModel();		
		ProjectBrowser.frame.getDiagramManager().updateMenthorFromInclusion(relationship);
		
		if(addToDiagram && diagramElement != null)
		{			
			addToDiagram(redo);
			ModelHelper.addMapping(relationship, diagramElement);	
			list.add(diagramElement);
		}
		
		DiagramEditor d = ((DiagramEditor)notification);
		//notify
		if (d!=null) {
			d.notifyChange((List<DiagramElement>) list, ChangeType.ELEMENTS_ADDED, redo ? NotificationType.REDO : NotificationType.DO);			
			UndoableEditEvent event = new UndoableEditEvent(((DiagramEditor)d), this);
			for (UndoableEditListener l : ((DiagramEditor)d).editListeners)  l.undoableEditHappened(event);			
		}
	}
		
	@SuppressWarnings("unused")
	private void addToDiagram (boolean redo)
	{
		//set sides on the diagram element
		if (diagramElement instanceof BaseConnection) {                         
            BaseConnection connection = (BaseConnection) diagramElement;                            
            if (connection.getRelationship() instanceof GeneralizationImpl)
            {
            	GeneralizationImpl generalization  = (GeneralizationImpl) connection.getRelationship();
            	generalization.setSpecific(source);
            	generalization.setGeneral(target);
            }
            else if(connection.getRelationship() instanceof AssociationImpl)
            {
                AssociationImpl association  = (AssociationImpl) connection.getRelationship();
                association.getMemberEnd().get(0).setType(source);
                association.getMemberEnd().get(1).setType(target);                              
            }
		}
		
		//add to diagram
		parent.addChild(diagramElement);
//		diagramElement.setParent((CompositeNode)parent);
		
		// bug in designing. not best solution, but it works.
		Relationship relationship = ((UmlConnection)diagramElement).getRelationship();		
		if (source instanceof Relationship || target instanceof Relationship)  diagramElement.invalidate();		
	}
	
	private void addToModel()
	{			
//		System.out.println("Adding = "+relationship);
		if (relationship instanceof Association){
			
			//set sides on the element
			Property p1 = ((Association)relationship).getMemberEnd().get(0);
			Property p2 = ((Association)relationship).getMemberEnd().get(1);			
			if(source!=null) {
				p1.setType(source);
				p1.setName(source.getName().trim().toLowerCase());
			}			
			if(target!=null) {
				p2.setType(target);
				p2.setName(target.getName().trim().toLowerCase());
			}
			
			// add to model
			if(eContainer==null){
				AddCommand cmd = new AddCommand(ModelHelper.createAdapterEditingDomain(), project.getModel().getPackagedElement(), relationship);
				ModelHelper.createAdapterEditingDomain().getCommandStack().execute(cmd);
			}else{				
				AddCommand cmd = new AddCommand(ModelHelper.createAdapterEditingDomain(), ((RefOntoUML.Package)eContainer).getPackagedElement(), relationship);
				ModelHelper.createAdapterEditingDomain().getCommandStack().execute(cmd);
			}			
		}
		if (relationship instanceof Generalization)
		{	
			//set sides on the element
			((Generalization)relationship).setSpecific(source);
			((Generalization)relationship).setSpecific(target);
			
			//add to model
			if(source!=null){
				AddCommand cmd = new AddCommand(ModelHelper.createAdapterEditingDomain(), ((RefOntoUML.Classifier)source).getGeneralization(), (RefOntoUML.Generalization)relationship);
				ModelHelper.createAdapterEditingDomain().getCommandStack().execute(cmd);				
			}
						
		}		
	}	
}

