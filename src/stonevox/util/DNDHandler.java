package stonevox.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import stonevox.Program;

public class DNDHandler extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	private JList<File> list;

	public DNDHandler(JList<File> list)
	{
		this.list = list;
	}

	public int getSourceActions(JComponent c)
	{
		return COPY_OR_MOVE;
	}

	public boolean canImport(TransferSupport ts)
	{
		return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	public boolean importData(TransferSupport ts)
	{
		try
		{
			@SuppressWarnings("rawtypes")
			List data = (List) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			if (data.size() < 1)
			{
				return false;
			}

			DefaultListModel<File> listModel = new DefaultListModel<File>();
			for (Object item : data)
			{
				File file = (File) item;
				listModel.addElement(file);

				Program.filepath = file.getAbsolutePath();

				// break for now until somesort of tab system is put in place to
				// load and view
				// multiple qbs at once
				break;
			}

			list.setModel(listModel);
			return true;

		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}
}
