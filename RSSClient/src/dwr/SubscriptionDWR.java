package dwr;

import org.directwebremoting.annotations.RemoteProxy;

import dao.SubscriptionDAO;

@RemoteProxy
public class SubscriptionDWR {

	public void updateSubscriptionsFolders() {
		SubscriptionDAO.getInstance().updateSubscriptionsFolders();
	}
}