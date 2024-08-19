<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>View Alarms</title>
<meta http-equiv="refresh" content="30">
<style>
table, th, td {
	border: 1px solid;
}
</style>
</head>
<body>
	<p>Number of alarms = ${alarmsSize}</p>
	<p>Alarm List</p>
	<table>
		<thead>
			<tr>
				<th>Reduction Key (Ktable key)</th>
				<th>Id</th>
				<td>last Event Time</td>
				<td>last Update Time</td>
				<td>Severity</td>
				<td>Log Message</td>
				<td>Ip Address</td>
				<td>Node Label</td>
				<td>Uei</td>
				<td>Last Event</td>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="entry" items="${alarms}">
				<tr>
					<td>${entry.key}</td>
					<td>${entry.value.id}</td>
					<td>${entry.value.lastEventTime}</td>
					<td>${entry.value.lastUpdateTime}</td>
					<td>${entry.value.severity}</td>
					<td>${entry.value.logMessage}</td>
					<td>${entry.value.ipAddress}</td>
					<td>${entry.value.nodeCriteria.nodeLabel}</td>
					<td>${entry.value.uei}</td>
					<td>
						<table>
							<c:if test="${not empty entry.value.lastEvent}">
								<tr>
									<td>last event id</td>
									<td>${entry.value.lastEvent.id}</td>
								</tr>
								<tr>
									<td>last event time</td>
									<td>${entry.value.lastEvent.time}</td>
								</tr>
								<c:forEach var="parameter"
									items="${entry.value.lastEvent.parameterList}">
									<tr>
										<td>parameter</td>
										<td>${parameter}</td>
									</tr>
								</c:forEach>

							</c:if>
						</table>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</body>
</html>

