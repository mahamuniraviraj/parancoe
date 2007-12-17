<%@ include file="../common.jspf" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%@ include file="../head.jspf" %>
    <script src="${cp}/dwr/interface/participantBo.js" type="text/javascript"></script>
</head>
<body>
<div id="nonFooter">    
    <jsp:include page="../header.jsp"/>
    <div id="content"> 
        <div id="content_main">
            
            <h1><spring:message code='Event'/></h1>
            <%@ include file="show.jspf" %>
            
            <h2><spring:message code='ParticipantList'/></h2>
            <a href="" onclick="participantBo.sendCertificateToAllParticipants(${event.id}, 'http://www.jugevents.org'); return false;"><spring:message code="SendAllCertificates"/></a>
            <div id="sentCertificatesMessage"></div>
            <div class="displaytag">
                <display:table name="participants" id="participantList" sort="list" pagesize="20" defaultsort="5" defaultorder="ascending" requestURI="participants.html" export="true">
                    <display:column title="#">${participantList_rowNum}</display:column>
                    <display:column property="firstName" titleKey="first_name" sortable="true" headerClass="sortable"/>
                    <display:column property="lastName" titleKey="last_name" sortable="true" headerClass="sortable"/>
                    <display:column property="email" titleKey="Email" sortable="true" headerClass="sortable"/>
                    <display:column property="creationDate" titleKey="JoinedAt" sortable="true" headerClass="sortable"/>
                    <display:column media="html" titleKey="Attended" sortable="true" headerClass="sortable" style="text-align: center;">
                        <input onclick="participantBo.setAttended(${participantList.id}, this.checked)" type="checkbox" <c:if test="${participantList.attended}">checked="${participantList.attended}"</c:if> />
                           </display:column>
                           <display:column media="csv xml excel pdf" property="attended" titleKey="Attended" sortable="true" headerClass="sortable" style="text-align: center;" />
                           <display:column media="html" titleKey="Certificate" sortable="false" style="text-align: center;">
                               <a href="#" onclick="participantBo.sendCertificateToParticipant(${participantList.id}, 'http://www.jugevents.org'); return false"><spring:message code="SendCertificate"/></a>
                        <div id="certificateMsg${participantList.id}"><fmt:formatDate value="${participantList.lastCertificateSentDate}" type="date" dateStyle="short" /></div>
                    </display:column>
                    <display:column media="csv xml excel pdf" property="lastCertificateSentDate" titleKey="Certificate" sortable="false" style="text-align: center;" />
                </display:table>
            </div>
            <br/>
        </div>
        <jsp:include page="../menu.jsp"/>
    </div>            
</div>
<jsp:include page="../footer.jsp"/>        
</body>
</html>