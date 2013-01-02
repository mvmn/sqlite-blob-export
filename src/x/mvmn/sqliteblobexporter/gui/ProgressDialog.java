package x.mvmn.sqliteblobexporter.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import x.mvmn.sqliteblobexporter.db.ExportWorker;

/**
 * @author Mykola Makhin
 *
 */
public class ProgressDialog extends JDialog {

	private static final long serialVersionUID = -786404721276991292L;
	private JLabel statusLabel = new JLabel("Working...");
	private ExportWorker exportWorker;
	private JButton okButton = new JButton("Ok");

	public ProgressDialog(Window parent) {
		super(parent, "Export progress", ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		this.setPreferredSize(new Dimension(500, 200));

		this.getContentPane().setLayout(new BorderLayout());

		statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.getContentPane().add(statusLabel, BorderLayout.CENTER);

		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ProgressDialog.this.setVisible(false);
			}
		});
		this.getContentPane().add(okButton, BorderLayout.SOUTH);
	}
	
	public Runnable createProgressCallback() {
		return new Runnable() {

			public void run() {
				ExportWorker exportWorker = ProgressDialog.this.exportWorker;
				if(exportWorker!=null) {
					String status = exportWorker.getStatus();
					int progress = exportWorker.getProgress();
					Exception error = exportWorker.getError();
					boolean finished = exportWorker.isFinished();
					
					if(!finished) {
						status += "("+progress+")";
					} else {
						if(error == null) status = "Finished.";
						else status = "Error occurred: "+error.getClass().getName()+": "+error.getMessage();
					}
					statusLabel.setText(status);
				}
			}
			
		};
	}
	public Runnable createFinishCallback() {
		return new Runnable() {

			public void run() {
				okButton.setEnabled(true);
				okButton.invalidate();
			}
		};
	}
	
	public void setExportWorker(ExportWorker exportWorker) {
		this.exportWorker = exportWorker;
	}	
}
