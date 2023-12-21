<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/table.css" />

<title>Display</title>
</head>
<body>

<h3>Select Product Table Result : ${fn:length(select)} row(s) selected</h3>
<table>
	<thead>
	<tr>
		<th>ID</th>
		<th>Name</th>
		<th>Price</th>
		<th>Make</th>
		<th>Expire</th>
	</tr>
	</thead>
	
	<c:if test="${not empty select}">
	<tbody>
		<c:forEach var="row" items="${select}">
			<c:url value="/pages/product" var="link">
				<c:param name="id" value="${row.id}"></c:param>
				<c:param name="name" value="${row.name}"></c:param>
				<c:param name="price" value="${row.price}"></c:param>
				<c:param name="make" value="${row.make}"></c:param>
				<c:param name="expire" value="${row.expire}"></c:param>
			</c:url>
		<tr>
			<td>${row.id}</td>
			<td><a href="${link}">${row.name}</a></td>
			<td>${row.price}</td>
			<td>${row.make}</td>
			<td>${row.expire}</td>
		</tr>
		</c:forEach>
	</tbody>
	</c:if>	
</table>

<h3><a href="<c:url value="/pages/product"></c:url>">Product Table</a></h3>

</body>
</html>