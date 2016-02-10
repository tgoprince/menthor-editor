package org.tinyuml.ui.diagram;

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

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tinyuml.draw.DiagramElement;
import org.tinyuml.draw.DrawingContext;
import org.tinyuml.draw.Label;
import org.tinyuml.draw.MultiSelection;
import org.tinyuml.draw.NodeSelection;
import org.tinyuml.draw.NullElement;
import org.tinyuml.draw.NullSelection;
import org.tinyuml.draw.RubberbandSelector;
import org.tinyuml.draw.Selection;
import org.tinyuml.umldraw.StructureDiagram;
import org.tinyuml.umldraw.shared.UmlConnectionSelection;
import org.tinyuml.umldraw.shared.UmlDiagramElement;

import net.menthor.editor.v2.commands.AppCommandListener;
import net.menthor.editor.v2.commands.ICommandListener;
import net.menthor.editor.v2.managers.EditManager;
import net.menthor.editor.v2.ui.editor.EditorMouseEvent;
import net.menthor.editor.v2.ui.editor.IEditorMode;
import net.menthor.editor.v2.ui.popupmenu.MultiElementPopupMenu;
import net.menthor.editor.v2.ui.popupmenu.SingleElementPopupMenu;

/**
 * This class handles selections of diagram allElements, this includes all
 * non-line allElements and multiple selections. It also handles displaying
 * the context menu.
 *
 * @author Wei-ju Wu, John Guerson
 */
public class SelectionHandler implements IEditorMode {

	private DiagramEditor editor;
	private Selection selection = NullSelection.getInstance();
	private Set<SelectionListener> listeners = new HashSet<SelectionListener>();
	private Point2D startPoint = new Point2D.Double();
	
	/**
	 * The selector for rubber band selection.
	 */
	private RubberbandSelector selector = new RubberbandSelector();

	/**
	 * This should be done with MultiSelection instead of a RubberBand...
	 * FIXME
	 */
	public void selectAll() 
	{
		selection = selector;
		selection.updatePosition(0,0);
		selection.startDragging(0,0);
		selection.updatePosition(editor.getDiagramWidth(), editor.getDiagramHeight());
		selection.stopDragging(editor.getDiagramWidth(),  editor.getDiagramHeight());
		if (selection instanceof RubberbandSelector) setRubberbandSelection((RubberbandSelector) selection);
		editor.redraw();
		notifyListeners();
		editor.requestFocusInEditor();
	}
	 
	public void select(List<DiagramElement> elements){
		selection = new MultiSelection(editor, elements);
	}
	
	/**
	 * Constructor.
	 * @param anEditor the editor
	 */
	public SelectionHandler(DiagramEditor anEditor) {
		editor = anEditor;
		selector.setDiagram(editor.getDiagram());
	}

	public Selection getSelection() {
		return selection;
	}

	/**
	 * Deselects all allElements.
	 */
	public void deselectAll() { selection = NullSelection.getInstance(); }

	/**
	 * Handles the selection on a mouseClicked event.
	 * @param e the EditorMouseEvent
	 */
	public void handleSelectionOnMouseClicked(EditorMouseEvent e) {
		
		boolean focusEditor = true;
		double mx = e.getX(), my = e.getY();
		
		if(e.getMouseEvent().isControlDown()) return;
		
		//System.out.println("Modifiers " + e.getMouseEvent().getMouseModifiersText(e.getMouseEvent().getModifiers()));
		
		// this is a pretty ugly cast, it is needed in order to use the getLabel()
		// method which is not a base DiagramElement method
		List<DiagramElement> previousSelected = selection.getElements();
				
		DiagramElement element = editor.getDiagram().getChildAt(mx, my);
			
		if (element instanceof UmlDiagramElement && previousSelected.contains(element)) {	
			Label label = element.getLabelAt(mx, my);
			if (label != null && label.isEditable()) {
				focusEditor = false;
				editor.editLabel(label);				
			} else if (e.getClickCount() >= 2) {
				EditManager.get().edit(element);
			}
		}
						
		else if (editor.getDiagram().getLabelAt(mx, my) != null) {
			// Edit the diagram name
			focusEditor = false;
			editor.editLabel(editor.getDiagram().getLabelAt(mx, my));
		} 
				
		else {
			if (element == NullElement.getInstance()) {
				element = editor.getDiagram();
			}			
			selection = element.getSelection(editor);			
		}
		editor.redraw();
		notifyListeners();
		if(focusEditor)
			editor.requestFocusInEditor();
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseClicked(EditorMouseEvent e) {		
		if (e.isMainButton()) {
			handleSelectionOnMouseClicked(e);
		}
		if (SwingUtilities.isRightMouseButton(e.getMouseEvent()))
        {
			handleSelectionOnMousePress(e);
			displayContextMenu(e);
			handleSelectionOnMouseReleased(e);	
        }
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void mousePressed(EditorMouseEvent e) {		
		handleSelectionOnMousePress(e);
		if (e.isPopupTrigger()) {
			//this was not working in Mac. I moved it to mouseClicked(e)
			displayContextMenu(e);
		}else{
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseReleased(EditorMouseEvent e) {						
		handleSelectionOnMouseReleased(e);		
		if (e.isPopupTrigger()) {
			//this was not working in Mac. I moved it to mouseClicked(e)
			displayContextMenu(e);
		}else{
			
		}
	}
	
	public void select (DiagramElement element)
	{
		selection = element.getSelection(editor);
	}
	
	/**
	 * Displays the context menu.
	 * @param e the EditorMouseEvent
	 */
	private void displayContextMenu(EditorMouseEvent e) {
		double mx = e.getX(), my = e.getY();
		Selection selection = getSelection(mx, my);
		if (!nothingSelected()) {
			if(selection.getElement() instanceof StructureDiagram == false)
			{
				final JPopupMenu menu = createDiagramPopupMenu(selection,mx,my);
				menu.show(e.getMouseEvent().getComponent(), e.getMouseEvent().getX(), e.getMouseEvent().getY());
			}
		}	
	}
	
	public JPopupMenu createDiagramPopupMenu(Selection selection, double x, double y) {
		
		ArrayList<UmlDiagramElement> filteredSelection = filterUmlDiagramElements(selection.getElements());
		
		if(filteredSelection.size()==1)
			return new SingleElementPopupMenu(AppCommandListener.get(), filteredSelection.get(0));
		
		if (selection.getElements().size() > 1) 			
			return new MultiElementPopupMenu(AppCommandListener.get(), filteredSelection);
		
		return new JPopupMenu("No Action Available");
	}
	
	private ArrayList<UmlDiagramElement> filterUmlDiagramElements(List<DiagramElement> original){
		ArrayList<UmlDiagramElement> filteredList = new ArrayList<UmlDiagramElement>();
		
		for (DiagramElement diagramElement : original) {
			if(diagramElement instanceof UmlDiagramElement)
				filteredList.add((UmlDiagramElement) diagramElement);
		}
		
		return filteredList;
	}
	

	/**
	 * Handle the selection on a mousePressed event.
	 * @param e the EditorMouseEvent
	 */
	public void handleSelectionOnMousePress(EditorMouseEvent e) {
		double mx = e.getX(), my = e.getY();
		
		Selection newSelection = getSelection(mx, my);

		if(e.getMouseEvent().isControlDown())
		{			
			if (newSelection instanceof NodeSelection || newSelection instanceof UmlConnectionSelection || newSelection instanceof MultiSelection || newSelection instanceof RubberbandSelector )
			{
				if(selection.getElements().size()>0){				
					ArrayList<DiagramElement> allElement = new ArrayList<DiagramElement>();
					List<DiagramElement> selectedElement = selection.getElements();	
					allElement.addAll(selectedElement);
					List<DiagramElement> newSelectedElement = newSelection.getElements();
					// select new elements...
					for(DiagramElement elem: newSelectedElement){
						if (!selectedElement.contains(elem)) allElement.add(elem);						
					}
					// in case of clicking in an already selected element: deselect it.					
					if(selectedElement.containsAll(newSelectedElement)){
						DiagramElement deselection = editor.getDiagram().getChildAt(mx, my);						
						allElement.remove(deselection);
					}										
					selection = new MultiSelection(editor, allElement);			
				}else{
					selection = newSelection;
				}
			}else{
				selection = newSelection;
			}
		}else{
			selection = newSelection;
		}
		
		// Dragging only if left mouse button was pressed
		if (e.isMainButton()){			
			if (nothingSelected() && editor.getDiagram().contains(mx, my)) {
				selection = selector;
				selection.updatePosition(mx, my);
			}			
			startPoint.setLocation(mx,my);
			selection.startDragging(mx, my);
		}
	}

	/**
	 * Returns true if no element was selected.
	 * @return true if no element was selected
	 */
	private boolean nothingSelected() {
		return selection == NullSelection.getInstance() ||
		selection.getElement() == editor.getDiagram();
	}

	/**
	 * Sets the current selection for the specified mouse coordinates. Returns
	 * true if an element was clicked, false otherwise
	 * @param mx the mapped x coordinate
	 * @param my the mapped y coordinate
	 * @return the selection object, a NullSelection instance otherwise
	 */
	private Selection getSelection(double mx, double my) {
		if (!nothingSelected() && selection.contains(mx, my)) {
			return selection;
		}
		DiagramElement element = editor.getDiagram().getChildAt(mx, my);			
		if (element != NullElement.getInstance()) {
			// select the element
			return element.getSelection(editor);
		}
		return editor.getDiagram().getSelection(editor);
	}

	/**
	 * Handles the current selection on a mouse released.
	 * @param e the EditorMouseEvent
	 */
	public void handleSelectionOnMouseReleased(EditorMouseEvent e) {
		
		double mx = e.getX(), my = e.getY();
				
		if (selection.isDragging()) {

			selection.updatePosition(mx, my);
			selection.stopDragging(mx, my);
			
			//TODO implement select/unselect holding shift
			/*Selection newSelection = getSelection(mx, my);
			
			if(e.getMouseEvent().isShiftDown())
			{
				if (selection instanceof NodeSelection)
				{
					ArrayList<DiagramElement> allElements = new ArrayList<DiagramElement>();
					DiagramElement selectedElement = selection.getElement();
					allElements.add(selectedElement);
					DiagramElement newSelectedElement = newSelection.getElement();
					allElements.add(newSelectedElement);
					selection = new MultiSelection(editor, allElements);
					
					//selection = selection;
				}
				
				else if(selection instanceof MultiSelection)
				{
					//TODO create the add and remove methods in multi selection
					if (selection.getElements().contains(newSelection.getElement()))
					{
						selection.getElements().remove(newSelection.getElement());
					}
					else
					{
						selection.getElements().add(newSelection.getElement());
					}
				}
			}*/
			
			if (selection instanceof RubberbandSelector) {
				
				setRubberbandSelection((RubberbandSelector) selection);
			}

			editor.redraw();
		}
		 
		// notify selection listeners
		notifyListeners();
		editor.requestFocusInEditor();
	}

	/**
	 * Sets the current selection to a rubber band selection if available.
	 * @param rubberband the RubberbandSelector
	 */
	private void setRubberbandSelection(RubberbandSelector rubberband) {
		if (rubberband.getSelectedElements().size() == 1) {
			selection = rubberband.getSelectedElements().get(0).getSelection(editor);
		} 
		else if (rubberband.getSelectedElements().size() > 1) {
			selection = new MultiSelection(editor, rubberband.getSelectedElements());
		} 
		else {
			selection = editor.getDiagram().getSelection(editor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseMoved(EditorMouseEvent e) {
		double mx = e.getX(), my = e.getY();
		if (selection.contains(mx, my)) {
			editor.setCursor(selection.getCursorForPosition(mx, my));
		} else {
			editor.setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseDragged(EditorMouseEvent e) {
		double mx = e.getX(), my = e.getY();		
		if (selection.isDragging()) {
			selection.updatePosition(mx, my);
			editor.repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void draw(DrawingContext drawingContext) {
		selection.draw(drawingContext);
		selector.draw(drawingContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateChanged() {
		selection.updateDimensions();
	}

	/**
	 * This method is called if the editor removed an element. If the current
	 * selection contains the removed element, that selection is removed.
	 * @param element the removed element
	 */
	public void elementRemoved(List<DiagramElement> elements) {
		for (DiagramElement element : elements) {
			if (selection.getElements().contains(element)) {
				selection = NullSelection.getInstance();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void cancel() {
		selection.cancelDragging();
	}

	/**
	 * Returns the currently selected element.
	 * @return the selected element
	 */
	public List<DiagramElement> getSelectedElements() {
		return selection.getElements();
	}

	/**
	 * Adds the specified SelectionListener.
	 * @param l the listener to add
	 */
	public void addSelectionListener(SelectionListener l) {
		listeners.add(l);
	}

	/**
	 * Removes the specified SelectionListener.
	 * @param l the listener to remove
	 */
	public void removeSelectionListener(SelectionListener l) {
		listeners.remove(l);
	}

	/**
	 * Notifies all listeners.
	 */
	private void notifyListeners() {
		for (SelectionListener l : listeners) {
			l.selectionStateChanged();
		}
	}

	/**
	 * Adds the specified AppCommandListener.
	 * @param l the AppCommandListener to add
	 */
	public void addAppCommandListener(ICommandListener l) {
		//contextMenuBuilder.addAppCommandListener(l);
	}
}
