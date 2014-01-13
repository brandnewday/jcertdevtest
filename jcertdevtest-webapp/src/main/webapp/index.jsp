<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Booking System</title>
	<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>

<div id="wrapper">

	<div id="searchPanel">
	<form action="Main" method="get">
		<input type="hidden" name="search" value="Y"/>
			<div id="searchDetails">
			<div>
				<div class="fieldLabel">Name:</div><input type="text" name="name"/><br/>
				<div class="fieldLabel">Location:</div><input type="text" name="location"/>
			</div>
			<div>
				<input type="radio" name="concat" value="AND" checked/>AND
				<input type="radio" name="concat" value="OR"/>OR
			</div>
			<input type="submit" name="exact" value="Search"/>
		</div>
		<div id="searchAll">
			<input type="submit" name="all" value="Show All"/>
		</div>
	</form>
	</div>


	<h2>${notice}</h2>

	<div id="resultPanel">
	<c:if test="${not empty rooms}">
		<form name="frmBooking" action="Booking" method="post">
		<input type="hidden" name="recNo"/>
		<input type="hidden" name="customer"/>
		<table>
			<thead>
				<tr>
					<c:forEach items="${colHeaders}" var="colHeader">
						<th>${colHeader}</th>
					</c:forEach>
					<th></th>
				</tr>
			</thead>
			<tbody>
			<c:forEach items="${rooms}" var="room">
				<tr>
					<c:forEach items="${room.data}" var="col">
						<td>${col}</td>
					</c:forEach>
					<c:if test="${!room.booked}">
						<td>
							<input type="button" name="${room.recNo}" value="Book" onClick="return doBook(${room.recNo});"/>
						</td>
					</c:if>
				</tr>
			</c:forEach>
			</tbody>
		</table>
		</form>
	</c:if>
	</div>
	
</div>
	
</body>
<script type="text/javascript">
function doBook(recNo) {
	var customer = prompt("Enter customer ID:", "");
	if(customer) {
		var form = document.forms["frmBooking"];
		form.elements["customer"].value = customer;
		form.elements["recNo"].value = recNo;
		form.submit();
	}
}	
</script>
</html>
