<%--

    Copyright (C) 2006-2012 The Parancoe Team <info@parancoe.org>

    This file is part of Parancoe Plugin Configuration.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<h1><spring:message code="PluginConfiguration_ConfigurationProperties" text="?PluginConfiguration_ConfigurationProperties?"/></h1>
<c:forEach items="${pluginConfigurationCategories}" var="category">
    <h2><spring:message code="${category.name}" text="?${category.name}?" /></h2>
</c:forEach>
