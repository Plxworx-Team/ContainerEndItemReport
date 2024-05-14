<%@include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.lang.*"%>
<%@page import="com.ptc.netmarkets.util.beans.NmCommandBean"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>

<jsp:include
	page="${mvc:getComponentURL('ext.enersys.builder.containerEndItemReport.ContainerEndItemTableBuilder')}" />

<%@include file="/netmarkets/jsp/util/end.jspf"%>