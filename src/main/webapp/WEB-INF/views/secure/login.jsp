<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" type="text/css" href="../css/main.css" />

<title><spring:message code="login.title" /></title>
</head>
<body>

<h3><spring:message code="login.header" /></h3>

<form action="<c:url value="/secure/login.controller"></c:url>" method="post">
<table>
	<tr>
		<td><spring:message code="login.form.username" /> : </td>
		<td><input type="text" name="username" value="${param.username}"></td>
		<td>${errors.xxx1}</td>
	</tr>
	<tr>
		<td><spring:message code="login.form.password" /> : </td>
		<td><input type="text" name="password" value="${param.password}"></td>
		<td>${errors.xxx2}</td>
	</tr>
	<tr>
		<td>
			<select name="locale">
				<option value="">請選擇</option>
				<option value="zh_TW">中文</option>
				<option value="en_US">英文</option>
				<option value="ja_JP">日文</option>
				<option value="de_DE">德文</option>
			</select>
		</td>
		<td align="right"><button type="submit"><spring:message code="login.form.button" /></button></td>
	</tr>
</table>
</form>

</body>
</html>