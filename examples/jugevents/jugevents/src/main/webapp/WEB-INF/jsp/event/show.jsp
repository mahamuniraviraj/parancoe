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
                    
                    <h1>Event</h1>
                    <jsp:useBean id="today" class="java.util.Date"/>
                    <c:if test="${today lt event.startDate}">
                    <a href="${cp}/event/registration.form?event.id=${param.id}"><spring:message code="RegisterToTheEvent"/></a>
                    </c:if>
                    <%@ include file="show.jspf" %>
                </div>
                <jsp:include page="../menu.jsp"/>
            </div>
        </div>
        <jsp:include page="../footer.jsp"/>        
    </body>
</html>