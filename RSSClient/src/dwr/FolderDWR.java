package dwr;

import java.util.Map;

import org.directwebremoting.annotations.RemoteProxy;

import dao.FolderDAO;

import entities.Folder;

import test.TestFeedReader;

@RemoteProxy
public class FolderDWR {

	public void runTestFeedReader() {
		new TestFeedReader();
	}
	
	public Map<String, Folder> listFolders() {
		return FolderDAO.getInstance().listFolders();
	}
	
	public void updateFolderSubscriptions() {
		FolderDAO.getInstance().updateFolderSubscriptions();
	}
}