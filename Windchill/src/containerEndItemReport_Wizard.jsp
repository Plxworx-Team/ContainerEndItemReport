
<%@include file = "/netmarkets/jsp/components/beginWizard.jspf" %>

<%@page import= "java.io.FileOutputStream"%>

<%@page import="java.lang.*"%>

<%@page import="com.ptc.netmarkets.util.beans.NmCommandBean"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
 
<c:set var="buttonList" value="ENDITemReportWizardButtons" scope="page" />

<c:set var="title" value="EndItem Report" scope="page" />
 
 
 
<jca:wizard buttonList="${buttonList}" title="${title}">

    <jca:wizardStep action="containerEndItemReport_WizardStep" type="createEndItemBOM" />

</jca:wizard>
 
 
<script>

window.onload=function(){

	window.resizeTo(800, 600);

	window.focus();

}
 
redirectToDownloadJSP = function (formAction){

	// URL Parameter --> aN & eN

	if(formAction.extraData){

		var aN = formAction.extraData.aN;

		var sFN = formAction.extraData.sFN;

		if(aN){

			window.opener.open("ext/enersys/containerEndItemReport/download?aN="+aN+"&sFN="+sFN, "_self");

		}else{

			//JCAAlert("ext.enersys.resourceBundle.EnerSysBOMUtilityRB.TR_ERROR_DOWNLOADING_CONSTANT");

		}

	}

	//return true;

};
 
//Register function with the ObjectsAffected event

PTC.action.on('objectsAffected', redirectToDownloadJSP);
 
</script>
 
<%@include file = "/netmarkets/jsp/util/end.jspf"%>
Oracle Java Technologies | Oracle
Java can help reduce costs, drive innovation, & improve application services; the #1 programming language for IoT, enterprise architecture, and cloud computing.