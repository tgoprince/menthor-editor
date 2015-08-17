package net.menthor.editor.transformation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import RefOntoUML.parser.OntoUMLParser;
import net.menthor.editor.v2.OntoumlDiagram;

public class TransformationDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = -4770351584655675698L;
	
	protected JFrame frame;
	protected List<OntoumlDiagram> diagrams;	
	protected OntoUMLParser refparser;		
	protected JTabbedPane tabbedPane = new JTabbedPane();
	protected JPanel principalPane;
	
	protected FilterPane filterPane = new FilterPane();
		
	protected JButton btnOk;
	protected JButton btnCancel;
	
	protected FilterPane getFilter() { return filterPane; } 
			
	protected JButton getOkButton() { return btnOk; }
	protected JFrame getFrame() { return frame; }
	
	protected TransformationDialog(JFrame owner, OntoUMLParser refparser, List<OntoumlDiagram> diagrams, boolean modal) 
	{
		super(owner, modal);
		
		this.frame = owner;				
		this.refparser = refparser;
		this.diagrams=diagrams;
		
		filterPane.fillContent(refparser, diagrams);
				
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	
		JPanel footerPane = new JPanel();
		getContentPane().add(footerPane, BorderLayout.SOUTH);
		
		btnOk = new JButton("Generate");
		footerPane.add(btnOk);
		
		btnCancel = new JButton("Close");
		footerPane.add(btnCancel);
		
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);		
		setTitle("Transformation");	
		setSize(new java.awt.Dimension(550, 420));
			
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
	}

	/** Add Non Closable Tab */
	protected Component addNonClosable(String text, Component component)
	{
		if (component==null) component = new JPanel();
		tabbedPane.addTab(text, component);		
		tabbedPane.setSelectedComponent(component);
		return component;
	}
	
	protected JButton getCancelButton() { return btnCancel; }
}

