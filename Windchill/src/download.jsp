<%@ page
	import="javax.servlet.ServletOutputStream,
org.apache.commons.io.FileUtils,
java.io.File,
java.lang.String,
java.io.UnsupportedEncodingException,
java.net.URLDecoder,
java.io.IOException,
java.util.Base64,
ext.enersys.builder.containerEndItemReport.ExportEndItemBOM,
java.io.FileInputStream,
java.io.OutputStream,
org.apache.commons.io.IOUtils
"%>

<%
	/**
	
	* 
	
	* Downloads the file with file name "aN" in parameter & then deletes it.
	
	* During download the "eN" parameter is the name of the downloaded file.
	
	*
	
	*/

	String fileName = request.getParameter("aN");
	String sessionFolderName = request.getParameter("sFN");
	System.out.println("Inside download JSP");
	final int BUFFER_CONST = 8192;
	if (fileName != null && !fileName.isEmpty()) {
		try {
			fileName = new String(Base64.getDecoder().decode(URLDecoder.decode(fileName, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

		}
	}

	if (sessionFolderName != null && !sessionFolderName.isEmpty()) {
		try {
			sessionFolderName = new String(
					Base64.getDecoder().decode(URLDecoder.decode(sessionFolderName, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	String excelFileLocation = ExportEndItemBOM.getUtilityBaseFolderLocation() + File.separatorChar
			+ sessionFolderName;

	if (excelFileLocation != null && !excelFileLocation.isEmpty()) {
		File excelFile = new File(excelFileLocation, fileName);

		if (fileName != null && !fileName.isEmpty() && excelFile != null && excelFile.isFile()) {
			// WRITE INTO RESPONSE in CHUNKS
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			OutputStream sos = response.getOutputStream();

			FileInputStream in = null;
			try {
				in = new FileInputStream(excelFile);
				byte[] buffer = new byte[BUFFER_CONST];
				int length;
				while ((length = in.read(buffer)) > 0) {
					sos.write(buffer, 0, length);
					sos.flush();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
			}
			sos.close();
			response.flushBuffer();
			out.clear();

			// DELETION OF FILE

			excelFile.delete();
			excelFile = null;

		} else {
			// NO DOWNLOAD
			// TODO: Custom Error Message

		}
	}
%>