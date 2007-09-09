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
                    <c:choose>
                        <c:when test="${requestScope.lang eq 'it'}">     
                           
                            Ti &egrave; stata spedita una mail all'indirizzo <b>${jugger.email}</b> con
                            le istruzioni per inserire la nuova password.
                        </c:when>
                        <c:otherwise>
                            
                            
                            An e-mail has been sent to your address <b>${jugger.email}</b> with the
                            instructions to insert the new password.
                        </c:otherwise>
                    </c:choose>
                </div>
                <jsp:include page="../menu.jsp"/>
            </div>
        </div>
        <jsp:include page="../footer.jsp"/>        
    </body>
</html>