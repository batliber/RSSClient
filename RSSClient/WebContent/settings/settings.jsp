<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="entities.*" %>
<%@ page import="dao.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>YARSS-Client :: Settings</title>
	<script type="text/javascript" src="./settings.js"></script>
	<link rel="stylesheet" type="text/css" href="./settings.css"></link>
</head>
<body>
	<div>
<%
	Collection<Subscription> subscriptions = SubscriptionDAO.getInstance().listSubscriptions();
	Collection<Folder> folders = FolderDAO.getInstance().listFoldersShallow();
	
	for (Subscription subscription : subscriptions) {
		String subscriptionTitle = subscription.getTitle();
%>
		<div style="height: 28px;">
			<div style="float: left;width: 600px;overflow: hidden; white-space: nowrap;"><%= subscriptionTitle %></div>
			<div style="float: left;width: 150px;">
				<select>
<%
		for (Folder folder : folders) {
			String folderName = folder.getName();
%>
					<option><%= folderName %></option>
<%
		}
%>
				</select>
			</div>
<%
		String folderNames = "";
		for (Folder folder : subscription.getFolders()) {
			folderNames += folder.getName() + ", ";
		}
		folderNames = folderNames.substring(0, folderNames.length() - 2);
%>
			<div><%= folderNames %></div>
		</div>
<%
	}
%>
	</div>
</body>