<%@ include file="../common.jspf" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <%@ include file="../head.jspf" %>
    </head>
    <body>
        <div id="nonFooter">    
            <jsp:include page="../header.jsp"/>
            <div id="content"> 
                <div id="content_main">
                    
                    <h1>Events</h1>
                    
                    <h2>Registration</h2>

                    <strong>Title:</strong> ${registration.event.title}<br/>
                    <strong>Start date:</strong> <fmt:formatDate value="${registration.event.startDate}" />
                    <strong>Start time:</strong> ${registration.event.startTime}<br/>
                    <strong>Start beer:</strong> ${registration.event.endDate} (${registration.event.endTime})<br/>
                    <br/>
                    ${registration.event.description}<br/>
                    <br/>
                    <form:form commandName="registration" method="POST" action="${cp}/event/registration.form">
                        <form:hidden path="event.id"/>
                        <form:hidden path="a"/>
                        <form:hidden path="b"/>
                        <table>
                            <tr>
                                <td>First Name:</td>
                                <td><form:input path="participant.firstName"/></td>
                            </tr>
                            <tr>
                                <td>Last Name:</td>
                                <td><form:input path="participant.lastName"/></td>
                            </tr>
                            <tr>
                                <td>E-Mail:</td>
                                <td><form:input path="participant.email"/></td>
                            </tr>
                            <tr>
                                <td>${registration.a} + ${registration.b} = </td>
                                <td><form:input path="sum"/></td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td><input type="submit" value="Submit"/><br/><br/></td>
                            </tr>       
                        </table>
                        <form:errors path="*" cssClass="errorBox"/>
                    </form:form>
                </div>
                <jsp:include page="../menu.jsp"/>
            </div>
        </div>
        <jsp:include page="../footer.jsp"/>        
    </body>
</html>