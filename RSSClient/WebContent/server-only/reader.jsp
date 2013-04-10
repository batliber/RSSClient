<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="entities.*" %>
<%@ page import="dao.*" %>
<%@ page import="importer.*" %>
<%@ page import="reader.*" %>
<%@ page import="global.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.net.*"%>
<%@ page import="org.bson.types.ObjectId"%>
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
	
	Folder requestedFolder = requestedFolderId != null ? FolderDAO.getInstance().getById(new ObjectId(requestedFolderId)) : null;
	Subscription requestedSubscription = requestedSubscriptionId != null ? SubscriptionDAO.getInstance().getById(new ObjectId(requestedSubscriptionId)) : null;
	boolean keepUnread = requestedKeepUnread != null ? requestedKeepUnread.equals("true") : false;
	Long limit = requestedLimit != null && !requestedLimit.equals("") ? new Long(requestedLimit) : new Long(Constants.DEFAULT_ITEMS_LIMIT);
	
	if (keepUnread) {
		Item item = new Item();
		item.setGUID(requestedGUID);
		
		ItemDAO.getInstance().markRead(item, requestedSubscription, !keepUnread);
	}
	
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
	<div id="divFolders" class="divFolders">
<%
	for (String folderName : folders.keySet()) {
		Folder folder = folders.get(folderName);
		
		if (requestedFolder != null && requestedFolder.getName().equals(folderName)) {
			Collection<Subscription> folderSubscriptions = requestedFolder.getSubscriptions();
%>
		<div class="divFolderIcon">&nbsp;</div><div class="divFolder"><a href="reader.jsp"><%=folderName%></a></div><div class="divUnreadCount">(<%=folder.getUnread()%>)</div>
<%
			for (Subscription subscription : folderSubscriptions) {
				String subscriptionText = subscription.getTitle().length() < 32 ? subscription.getTitle() : (subscription.getTitle().substring(0, 29) + "...");
		
				if (subscription.getUnread() > 0) {
%>
		<div class="divSubscription"><a href="reader.jsp?folderId=<%=folder.getId()%>&subscriptionId=<%=subscription.getId()%>"><%=subscriptionText%></a></div><div class="divUnreadCount">(<%=subscription.getUnread()%>)</div>
<%
				}
			}
		} else {
			if (folder.getUnread() > 0) {
%>
		<div class="divFolderIcon">&nbsp;</div><div class="divFolder"><a href="reader.jsp?folderId=<%=folder.getId()%>"><%=folderName%></a></div><div class="divUnreadCount">(<%=folder.getUnread()%>)</div>
<%
			}
	
		}
	}
%>
	</div>
	<div id="divItems" class="divItems">
<%
	if (requestedSubscription == null) {
		if (requestedFolder != null) {
			for (Item item : ItemDAO.getInstance().listItemsByFolder(requestedFolder, false, limit)) {
				Subscription subscription = item.getSubscription();
				String subscriptionTitle = 
					subscription.getTitle().length() < 32 ? subscription.getTitle() : (subscription.getTitle().substring(0, 29) + "...");
				String itemTitle = (item.getTitle() != null ? item.getTitle().substring(0, Math.min(72, item.getTitle().length())) : "title"); 
				String itemText = " - " + (item.getText() != null ? item.getText() : "description");
				
				if (requestedGUID != null && requestedGUID.equals(item.getGUID()) && !keepUnread) {
%>
		<a href="reader.jsp?folderId=<%=requestedFolder.getId()%>">
			<div class="openSubscriptionTitle"><%=subscriptionTitle%></div>
			<div class="openItemPubDate"><%= item.getPubDate() != null ? simpleDateFormat.format(item.getPubDate()) : "" %></div>
			<div class="openItemTitle"><%=itemTitle%></div>
			<div class="openItemText"><%=itemText%></div>
		</a>
		<div class="openItemLink">
			<a class="aOpenItemLink" href="<%=item.getLink() != null ? item.getLink() : "#"%>"><%=itemTitle%></a>
		</div>
		<div class="openItemHTML"><%=item.getHTML()%></div>
		<div class="openItemTools">
			<a class="aOpenItemToolsKeepUnread" href="reader.jsp?folderId=<%=requestedFolder.getId()%>&subscriptionId=<%=subscription.getId()%>&itemGUID=<%=URLEncoder.encode(item.getGUID(), "UTF-8")%>&keepUnread=true">Keep unread</a>
		</div>
<%
				} else {
%>
		<a href="reader.jsp?folderId=<%=requestedFolder.getId()%>&itemGUID=<%=URLEncoder.encode(item.getGUID(), "UTF-8")%>">
			<div class="subscriptionTitle"><%=subscriptionTitle%></div>
			<div class="itemPubDate"><%= item.getPubDate() != null ? simpleDateFormat.format(item.getPubDate()) : "" %></div>
			<div class="itemTitle"><%=itemTitle%></div>
			<div class="itemText"><%=itemText%></div>
		</a>
<%
				}
			}
%>
		<div><a href="reader.jsp?folderId=<%= requestedFolder.getId() %>&limit=<%= limit != null ? limit + Constants.DEFAULT_ITEMS_LIMIT : "" %>">More items</a></div>
<%
		}
	} else {
		String subscriptionTitle = 
			requestedSubscription.getTitle().length() < 32 ? requestedSubscription.getTitle() : (requestedSubscription.getTitle().substring(0, 29) + "...");
		
		for (Item item : ItemDAO.getInstance().listItemsBySubscription(requestedSubscription, false, limit)) {
			String itemTitle = (item.getTitle() != null ? item.getTitle().substring(0, Math.min(72, item.getTitle().length())) : "title");
			String itemText = " - " + (item.getText() != null ? item.getText() : "description");
	
			if (requestedGUID != null && requestedGUID.equals(item.getGUID()) && !keepUnread) {
%>
		<a href="reader.jsp?folderId=<%=requestedFolder.getId()%>&subscriptionId=<%=requestedSubscription.getId()%>">
			<div class="openSubscriptionTitle"><%=subscriptionTitle%></div>
			<div class="openItemPubDate"><%= item.getPubDate() != null ? simpleDateFormat.format(item.getPubDate()) : "" %></div>
			<div class="openItemTitle"><%=itemTitle%></div>
			<div class="openItemText"><%=itemText%></div>
		</a>
		<div class="openItemLink"><a class="aOpenItemLink" href="<%=item.getLink() != null ? item.getLink() : "#"%>"><%=itemTitle%></a></div>
		<div class="openItemHTML"><%=item.getHTML()%></div>
		<div class="openItemTools">
			<a class="aOpenItemToolsKeepUnread" href="reader.jsp?folderId=<%=requestedFolder.getId()%>&subscriptionId=<%=requestedSubscription.getId()%>&itemGUID=<%=URLEncoder.encode(item.getGUID(), "UTF-8")%>&keepUnread=true">Keep unread</a>
		</div>
<%
			} else {
%>
		<a href="reader.jsp?folderId=<%=requestedFolder.getId()%>&subscriptionId=<%=requestedSubscription.getId()%>&itemGUID=<%=URLEncoder.encode(item.getGUID(), "UTF-8")%>">
			<div class="subscriptionTitle"><%=subscriptionTitle%></div>
			<div class="itemPubDate"><%= item.getPubDate() != null ? simpleDateFormat.format(item.getPubDate()) : "" %></div>
			<div class="itemTitle"><%=itemTitle%></div>
			<div class="itemText"><%=itemText%></div>
		</a>
<%
			}
		}
%>
		<div><a href="reader.jsp?folderId=<%= requestedFolder.getId() %>&subscriptionId=<%= requestedSubscription.getId() %>&limit=<%= limit != null ? limit + Constants.DEFAULT_ITEMS_LIMIT : "" %>">More items</a></div>
<%
	}

	if (requestedGUID != null && !keepUnread) {
		Item item = new Item();
		item.setGUID(requestedGUID);
		
		ItemDAO.getInstance().markRead(item, requestedSubscription, true);
	}
%>
	</div>
</body>
</html>