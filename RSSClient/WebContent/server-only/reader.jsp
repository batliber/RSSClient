<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="entities.*" %>
<%@ page import="dao.*" %>
<%@ page import="importer.*" %>
<%@ page import="reader.*" %>
<%@ page import="global.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.net.*" %>
<%@ page import="org.bson.types.ObjectId" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>YARSS-Client</title>
	<script type="text/javascript" src="./reader.js"></script>
	<link rel="stylesheet" type="text/css" href="./reader.css"></link>
</head>
<body>
<%
	String requestedFolderId = request.getParameter("folderId");
	String requestedSubscriptionId = request.getParameter("subscriptionId");
	String requestedGUID = request.getParameter("itemGUID");
	String requestedKeepUnread = request.getParameter("keepUnread");
	String requestedLimit = request.getParameter("limit");
	
	Map<String, Folder> folders = FolderDAO.getInstance().listFolders();
	
	Folder requestedFolder = 
		requestedFolderId != null ? 
			FolderDAO.getInstance().getById(new ObjectId(requestedFolderId)) : 
			null;
	Subscription requestedSubscription = 
		requestedSubscriptionId != null ? 
			SubscriptionDAO.getInstance().getById(new ObjectId(requestedSubscriptionId)) : 
			null;
	boolean keepUnread = 
		requestedKeepUnread != null ? 
			requestedKeepUnread.equals("true") : 
			false;
	Long limit = 
		requestedLimit != null && !requestedLimit.equals("") ? 
			new Long(requestedLimit) : 
			new Long(Constants.DEFAULT_ITEMS_LIMIT);
	Long newLimit = limit + Constants.DEFAULT_ITEMS_LIMIT;
	
	if (keepUnread) {
		Item item = new Item();
		item.setGUID(requestedGUID);
		
		ItemDAO.getInstance().markRead(item, requestedSubscription, !keepUnread);
	}
	
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
	<div class="divFolders">
<%
	// Para cada Folder
	for (String folderName : folders.keySet()) {
		Folder folder = folders.get(folderName);
		
		ObjectId folderId = folder.getId();
		Long folderUnread = folder.getUnread();
		
		if (requestedFolder != null && requestedFolder.getName().equals(folderName)) {
			// Si es el Folder solicitado
			Collection<Subscription> folderSubscriptions = requestedFolder.getSubscriptions();
			String closeFolderURL = "reader.jsp";
%>
		<div class="divFolderStatusOpen">&nbsp;</div>
		<div class="divFolderIcon">&nbsp;</div>
		<div class="divFolder">
			<a href="<%= closeFolderURL %>"><span class="spanFolderName"><%= folderName %></span></a>
		</div>
		<div class="divUnreadCount">(<%= folderUnread %>)</div>
<%
			// Para cada Subscription
			for (Subscription subscription : folderSubscriptions) {
				ObjectId subscriptionId = subscription.getId();
				String subscriptionTitle = 
					subscription.getTitle().length() < 32 ? 
						subscription.getTitle() : 
						(subscription.getTitle().substring(0, 29) + "...");
				Long subscriptionUnread = subscription.getUnread();
		
				if (subscription.getUnread() > 0) {
					// Si hay Items no leídos
					String openSubscriptionURL = "reader.jsp?" + 
						"folderId=" + folderId + 
						"&subscriptionId=" + subscriptionId;
%>					
		<div class="divSubscription">
			<a href="<%= openSubscriptionURL %>"><%= subscriptionTitle %></a>
		</div>
		<div class="divUnreadCount">(<%= subscriptionUnread %>)</div>
<%
				}
			}
		} else {
			// Si no es el Folder solicitado
			if (folderUnread > 0) {
				
				// Si hay items no leídos
				String openFolderURL = "reader.jsp?" + 
					"folderId=" + folder.getId();
%>
		<div class="divFolderStatusClosed">&nbsp;</div>
		<div class="divFolderIcon">&nbsp;</div>
		<div class="divFolder">
			<a href="<%= openFolderURL %>"><span class="spanFolderName"><%= folderName %></span></a>
		</div>
		<div class="divUnreadCount">(<%= folderUnread %>)</div>
<%
			}
		}
	}
%>
	</div>
	<div class="divItems">
<%
	if (requestedSubscription == null) {
		// Si estamos viendo los Items de un Folder
		if (requestedFolder != null) {
			// Si solicitamos un Folder
			String folderName = requestedFolder.getName();
%>
		<div class="divItemsHeader"><%= folderName %></div>
<%
			// Para cada Item del Folder
			for (Item item : ItemDAO.getInstance().listItemsByFolder(requestedFolder, false, limit)) {
				Subscription subscription = item.getSubscription();
				String subscriptionTitle = 
					subscription.getTitle().length() < 32 ? 
						subscription.getTitle() : 
						(subscription.getTitle().substring(0, 29) + "...");
				String itemTitle = 
					item.getTitle() != null ? 
						item.getTitle().substring(0, Math.min(72, item.getTitle().length())) : 
						"title";
				String itemText = " - " + (item.getText() != null ? item.getText() : "description");
				String itemPubDate = item.getPubDate() != null ? simpleDateFormat.format(item.getPubDate()) : "";
				String itemLink = item.getLink() != null ? item.getLink().toString() : "#";
				String itemHTML = item.getHTML();
				String itemGUID = URLEncoder.encode(item.getGUID(), "UTF-8");
				
				if (requestedGUID != null && requestedGUID.equals(item.getGUID()) && !keepUnread) {
					// Si solicitamos un Item en particular
					ObjectId subscriptionId = subscription.getId();
					String closeItemURL = "reader.jsp?" +
						"folderId=" + requestedFolderId;
					String keepUnreadURL = "reader.jsp?" + 
						"folderId=" + requestedFolderId + 
						"&subscriptionId=" + subscriptionId + 
						"&itemGUID=" + itemGUID + 
						"&keepUnread=true";
%>
		<div class="openSubscriptionTitle"><%= subscriptionTitle %></div>
		<div class="folderOpenItemPubDate"><%= itemPubDate %></div>
		<div class="openItemTitle"><a href="<%= closeItemURL %>"><%= itemTitle %></a></div>
		<div class="openItemText"><%= itemText %></div>
		<div class="openItemLink">
			<a class="aOpenItemLink" href="<%= itemLink %>"><%= itemTitle %></a>
		</div>
		<div class="openItemHTML"><%= itemHTML %></div>
		<div class="openItemTools">
			<a class="aOpenItemToolsKeepUnread" href="<%= keepUnreadURL %>">Keep unread</a>
		</div>
<%
				} else {
					// Si estamos viendo todos los Items colapsados
					String openItemURL = "reader.jsp?" + 
						"folderId=" + requestedFolderId + 
						"&itemGUID=" + itemGUID;
%>
		<div class="subscriptionTitle"><%= subscriptionTitle %></div>
		<div class="folderItemPubDate"><%= itemPubDate %></div>
		<div class="itemTitle"><a href="<%= openItemURL %>"><%= itemTitle %></a></div>
		<div class="itemText"><%= itemText %></div>
<%
				}
			}
		
		String moreItemsURL = "reader.jsp?" + 
			"folderId=" + requestedFolderId + 
			"&limit=" + newLimit;
%>
		<div class="divMoreItems"><a href="<%= moreItemsURL %>">::&nbsp;More items&nbsp;::</a></div>
<%
		}
	} else {
		// Si estamos viendo los Items de una Subscripción
		String subscriptionTitle = requestedSubscription.getTitle();
		String subscriptionURL = requestedSubscription.getSiteURL();
%>
		<div class="divItemsHeader"><a href="<%= subscriptionURL %>"><%= subscriptionTitle %></a></div>
<%		
		// Para cada Item de la Subscripción
		for (Item item : ItemDAO.getInstance().listItemsBySubscription(requestedSubscription, false, limit)) {
			String itemGUID = URLEncoder.encode(item.getGUID(), "UTF-8");
			String itemHTML = item.getHTML();
			String itemLink = item.getLink() != null ? item.getLink().toString() : "#";
			String itemPubDate = item.getPubDate() != null ? simpleDateFormat.format(item.getPubDate()) : "";
			String itemText = " - " + (item.getText() != null ? item.getText() : "description");
			String itemTitle = 
				item.getTitle() != null ? 
					item.getTitle().substring(0, Math.min(72, item.getTitle().length())) : 
					"title";
			String closeItemURL = "reader.jsp?" + 
				"folderId=" + requestedFolderId + 
				"&subscriptionId=" + requestedSubscriptionId;

			if (requestedGUID != null && requestedGUID.equals(item.getGUID()) && !keepUnread) {
				// Si solicitamos un Item en particular
				String keepUnreadURL = "reader.jsp?" + 
					"folderId=" + requestedFolderId + 
					"&subscriptionId=" + requestedSubscriptionId + 
					"&itemGUID=" + itemGUID + 
					"&keepUnread=true";
%>
		<div class="subscriptionOpenItemPubDate"><%= itemPubDate %></div>
		<div class="openItemTitle"><a href="<%= closeItemURL %>"><%= itemTitle %></a></div>
		<div class="openItemText"><%= itemText %></div>
		<div class="openItemLink">
			<a class="aOpenItemLink" href="<%= itemLink %>"><%= itemTitle %></a>
		</div>
		<div class="openItemHTML"><%= itemHTML %></div>
		<div class="openItemTools">
			<a class="aOpenItemToolsKeepUnread" href="<%= keepUnreadURL %>">Keep unread</a>
		</div>
<%
			} else {
				// Si estamos viendo todos los Items colapsados
				String openItemURL = "reader.jsp?" + 
					"folderId=" + requestedFolderId + 
					"&subscriptionId=" + requestedSubscriptionId + 
					"&itemGUID=" + itemGUID;
%>
		<div class="subscriptionItemPubDate"><%= itemPubDate %></div>
		<div class="itemTitle"><a href="<%= openItemURL %>"><%= itemTitle %></a></div>
		<div class="itemText"><%= itemText %></div>
<%
			}
		}
		
		String moreItemsURL = "reader.jsp?" + 
			"folderId=" + requestedFolderId + 
			"&subscriptionId=" + requestedSubscriptionId +
			"&limit=" + newLimit;
%>
		<div class="divMoreItems"><a href="<%= moreItemsURL %>">::&nbsp;More items&nbsp;::</a></div>
<%
	}

	if (requestedGUID != null && !keepUnread) {
		// Si estamos solicitando mantener el Item abierto como no leído
		Item item = new Item();
		item.setGUID(requestedGUID);
		
		ItemDAO.getInstance().markRead(item, requestedSubscription, true);
	}
%>
	</div>
</body>
</html>