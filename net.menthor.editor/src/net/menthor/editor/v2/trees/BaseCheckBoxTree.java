package net.menthor.editor.v2.trees;

/*
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

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.emf.ecore.EObject;

import RefOntoUML.NamedElement;

public class BaseCheckBoxTree extends CheckboxTree {

	private static final long serialVersionUID = 1L;

	protected DefaultMutableTreeNode modelRootNode;		
	protected DefaultTreeModel treeModel;
	protected TreeCheckingModel checkingModel;
	
	/**Constructor */
	protected BaseCheckBoxTree(DefaultMutableTreeNode rootNode){
		super(rootNode);
		this.modelRootNode = rootNode;
		this.treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
		getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);			
		checkingModel = getCheckingModel();		
	}
	
	/** Remove all nodes except the root node. */
	public void clear(){
    	modelRootNode.removeAllChildren();
        treeModel.reload();
    }    
    
    public void resetSelection() { }
    
    /** Get the selected node from the tree */
    public DefaultMutableTreeNode getSelectedNode(){
    	DefaultMutableTreeNode selectedNode;
		TreePath parentPath = getSelectionPath(); 
	    if (parentPath == null){
	        selectedNode = modelRootNode;
	    }else{
	      selectedNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
	    }
	    return selectedNode;
    }
    
    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addElement(EObject child){    		
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath(); 
        if (parentPath == null){
            parentNode = modelRootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
        } 
        return addElement(parentNode, child, true);
    }    
      
    public DefaultMutableTreeNode addElement(DefaultMutableTreeNode parent, EObject child){
    	return addElement(parent, child, false);
    }
    
    /** Add element to the tree */
    public DefaultMutableTreeNode addElement(DefaultMutableTreeNode parent, EObject child, boolean shouldBeVisible){    	
		DefaultMutableTreeNode node = getNode(child);
		if(node!=null) return node;    	    	
		DefaultMutableTreeNode childNode = createNode(child);		
		if (parent == null) parent = modelRootNode;				
		//It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());		
		//Make sure the user can see the lovely new node.
		if (shouldBeVisible) scrollPathToVisible(new TreePath(childNode.getPath()));		
		return childNode;
    }
    
    /** Get the root node from the tree */
    public DefaultMutableTreeNode getRootNode(){
		return modelRootNode;
	}
			
    public void colapseAll(){
    	collapsePath(new TreePath(modelRootNode.getPath()));
    }
    
    /** Get the node of this user object */
    @SuppressWarnings("rawtypes")
    public DefaultMutableTreeNode getNode(EObject userObject){	
		Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()){	    	
    		Object obj = node.getUserObject();
    		if (obj.equals(userObject)) return node;	    		    		
	    	node = (DefaultMutableTreeNode)e.nextElement();
	    }
	    //last element	    
	    Object obj = node.getUserObject();
	    if (obj.equals(userObject)) return node;	    	  
	    return null;
    }
    
    /** Create a node to the tree*/
    public DefaultMutableTreeNode createNode(EObject object){
    	return new DefaultMutableTreeNode(object);    
    }
    
	
	/** Get checked elements */
    public List<EObject> getCheckedElements () {		
		List<EObject> checkedNodes = new ArrayList<EObject>();
	    TreePath[] treepathList = getCheckingPaths();	    	
	    for (TreePath treepath : treepathList){	    	
	    	DefaultMutableTreeNode node = ((DefaultMutableTreeNode)treepath.getLastPathComponent());	    	
	    	EObject elem = (EObject)node.getUserObject();
	    	if(!checkedNodes.contains(elem)) checkedNodes.add(elem);	    		    		    	
	    }		    	
	    return checkedNodes;
	}
    
	/** Get Unchecked Elements. */
    public List<EObject> getUncheckedElements (){
		List<EObject> uncheckedNodes = new ArrayList<EObject>();
		List<EObject> checkedNodes = new ArrayList<EObject>();
	    TreePath[] treepathList = getCheckingPaths();	    	
	    for (TreePath treepath : treepathList){	    	
	    	DefaultMutableTreeNode node = ((DefaultMutableTreeNode)treepath.getLastPathComponent());	    	
	    	EObject elem = (EObject)node.getUserObject();
	    	if(!checkedNodes.contains(elem)) checkedNodes.add(elem);	    	
	    }		    
		EObject rootObject = (EObject)modelRootNode.getUserObject();	    	    
		initUncheckeNodes(rootObject, checkedNodes, uncheckedNodes);    	    	
	    return uncheckedNodes;
	}

    /** Initialize Unchecked Nodes. */
	protected void initUncheckeNodes(EObject element, List<EObject> checkedElements,List<EObject> uncheckedElements){    	
    	if (element == null) return;    	
    	Object[] elemArray = element.eContents().toArray();
		for (Object obj : elemArray){
			initUncheckeNodes((EObject)obj, checkedElements, uncheckedElements);
		}    	
    	if(!checkedElements.contains(element)){
    		if (element instanceof Package){    			
    			// nothing to do in this case...
    		}else{    			
    			uncheckedElements.add(element);    			
    		}
    	}	
    }
		
	/** Check Node. */
	@SuppressWarnings({ "rawtypes", "unused" })
	protected void checkNode(DefaultMutableTreeNode node, boolean uncheckChildren){		
		EObject childObject;		
		addCheckingPath(new TreePath(node.getPath()));				
		Object userobj = node.getUserObject();
		//unselected children only if was different than Association
    	if(uncheckChildren && userobj!=null && node.getChildCount()>0){
			Enumeration e = node.breadthFirstEnumeration();
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)e.nextElement();				
			while (e.hasMoreElements()){
				childNode = (DefaultMutableTreeNode)e.nextElement();				
				childObject = (EObject)childNode.getUserObject();		    		
				getCheckingModel().removeCheckingPath(new TreePath(childNode.getPath()));				
			}
    	}
	}
	
	/** Uncheck Node. */
	@SuppressWarnings({ "rawtypes", "unused" })
	protected void uncheckNode(DefaultMutableTreeNode node, boolean checkChildren){		
		EObject childObject;		
		removeCheckingPath(new TreePath(node.getPath()));		
		Object userobj = node.getUserObject();				
		//unselected children only if was different than Association
    	if(checkChildren && userobj!=null && node.getChildCount()>0){
			Enumeration e = node.breadthFirstEnumeration();
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)e.nextElement();					
			while (e.hasMoreElements()) {
				childNode = (DefaultMutableTreeNode)e.nextElement();				
				childObject = (EObject)childNode.getUserObject();		    		
				getCheckingModel().addCheckingPath(new TreePath(childNode.getPath()));				
			}
    	}
	}
	
	/** Select node */
	public void select (DefaultMutableTreeNode  node){
		this.setSelectionPath(new TreePath(node.getPath()));
		this.scrollPathToVisible(new TreePath(node.getPath()));		
	}
	
	/** Check these elements. We do not concern with other elements*/
	@SuppressWarnings("rawtypes")
	public void check(List<EObject> elements){			   
		List<EObject> alreadyChecked = getCheckedElements();	    
		alreadyChecked.removeAll(elements);
		alreadyChecked.addAll(elements);				
	    Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) {	    	
		    EObject obj = (EObject)node.getUserObject();
		    if (alreadyChecked.contains(obj)) { checkNode(node,true); }	    		    		    		
	    	node = (DefaultMutableTreeNode)e.nextElement();	    
	    }
	    //last element	    
		EObject obj = (EObject)node.getUserObject();
		if (alreadyChecked.contains(obj)) { checkNode(node,true); }	    
	}	
	
	/** Check these elements in the tree uncheking all other leafs */
	@SuppressWarnings("rawtypes")
	protected void checkStrictly(List<EObject> elements){			   
	    Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) {	    	
	    	EObject obj = (EObject)node.getUserObject();
	    	if(node.isLeaf()){
	    		if (elements.contains(obj)) { checkNode(node,true); }	    			
	    		else { uncheckNode(node,true); }
	    	}	    		    	
	    	node = (DefaultMutableTreeNode)e.nextElement();	    
	    }
	    //last element	    
		EObject obj = (EObject)node.getUserObject();
		if(node.isLeaf()){
		    if (elements.contains(obj)) { checkNode(node,true); }  
		    else { uncheckNode(node,true); }
	    }	    
	}	
	
	/** Uncheck this elements */
	@SuppressWarnings("rawtypes")
	public void uncheck(List<EObject> elements){			   
		List<EObject> alreadyUnchecked = getUncheckedElements();	    
		alreadyUnchecked.removeAll(elements);
		alreadyUnchecked.addAll(elements);				
	    Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()){	    	
		    EObject obj = (EObject)node.getUserObject();
		    if (alreadyUnchecked.contains(obj)) { uncheckNode(node,true); }	    		    		    		
	    	node = (DefaultMutableTreeNode)e.nextElement();	    
	    }
	    //last element	    
		EObject obj = (EObject)node.getUserObject();
		if (alreadyUnchecked.contains(obj)) { uncheckNode(node,true); }	    
	}	
	
	/** Check Element */
	@SuppressWarnings("rawtypes")
	protected boolean checkElement(EObject element){	
		boolean result = false;
		EObject rootEObj = (EObject)modelRootNode.getUserObject();
		if (rootEObj.equals(element)) { result=true; select(modelRootNode); return result; }		
		Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) {	    	
	    	EObject obj = (EObject)node.getUserObject();
	    	if (obj.equals(element)) { 
	    		result =true;		    		
	    		this.setSelectionPath(new TreePath(node.getPath()));
	    		this.scrollPathToVisible(new TreePath(node.getPath()));			    		
	    		return result;
	    	}	    		    		
	    	node = (DefaultMutableTreeNode)e.nextElement();
	    }
	    //last element	    
	    EObject obj = (EObject)node.getUserObject();
	    if (obj.equals(element)){ 
	    	result =true;		    	
	    	this.setSelectionPath(new TreePath(node.getPath()));
	    	this.scrollPathToVisible(new TreePath(node.getPath()));		    	
	    	return result;
	    }	    	    
	    return result;	    
	}
	
	/** Find Node */
	@SuppressWarnings("rawtypes")
	public ArrayList<DefaultMutableTreeNode> findName(String name)
	{		
		ArrayList<DefaultMutableTreeNode> list = new ArrayList<DefaultMutableTreeNode>();
		Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) {	    	
    		EObject obj = ((EObject)node.getUserObject());	
    		if(obj instanceof NamedElement){
    			NamedElement namedElem = (NamedElement)obj;
		    	if(namedElem.getName()!=null && !namedElem.getName().isEmpty()){
				   	if (namedElem.getName().contains(name) || namedElem.getName().equalsIgnoreCase(name)) {			    		
				   		list.add(node);			    		
				   	}
		    	}	    	
    		}
	    	node = (DefaultMutableTreeNode)e.nextElement();
	    }
	    //last element
	    EObject obj = ((EObject)node.getUserObject());	
		if(obj instanceof NamedElement){
			NamedElement namedElem = (NamedElement)obj;        		
    		if(namedElem.getName()!=null && !namedElem.getName().isEmpty()){
		    	if (namedElem.getName().contains(name) || namedElem.getName().equalsIgnoreCase(name)) {		    		
		    		list.add(node);		    		
		    	}
    		}	    	
	    }
	    return list;
	}
}
