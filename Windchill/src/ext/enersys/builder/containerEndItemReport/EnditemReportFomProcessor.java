  package ext.enersys.builder.containerEndItemReport;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.util.WTException;

public class EnditemReportFomProcessor extends DefaultObjectFormProcessor {
   private static final String CLASSNAME = EnditemReportFomProcessor.class.getName();
   private static final int RANDOM_FOLDER_NAME_LENGTH = 9;

   public FormResult doOperation(NmCommandBean commandBean, List<ObjectBean> listObj) throws WTException {
      System.out.println("Inside EditemReportFomProcessor class");
      FormResult formResult = super.doOperation(commandBean, listObj);
      formResult.setStatus(FormProcessingStatus.SUCCESS);
      String SESSION_FOLDER_NAME = RandomStringUtils.randomAlphanumeric(9);
      ArrayList<Object> selectedObjects = commandBean.getSelected();
      System.out.println("Selected Objetcs " + selectedObjects.size());
      WTPart part = null;
      ArrayList<WTPart> WTParts = new ArrayList();
      Iterator var9 = selectedObjects.iterator();

      while(var9.hasNext()) {
         Object obj = var9.next();
         System.out.println("###  " + obj.toString() + " " + obj);
         String total = obj.toString();
         String vr = total.substring(total.lastIndexOf("VR"), total.indexOf("!"));
         ReferenceFactory rf = new ReferenceFactory();
         WTReference reference = rf.getReference(vr);
         part = (WTPart)reference.getObject();
         WTParts.add(part);
      }

      String containerName = part.getContainerName();
      File excelFile = null;
      System.out.println("Calling WriteExcel");

      try {
         excelFile = ExportEndItemBOM.writeExcel(WTParts, containerName, SESSION_FOLDER_NAME);
      } catch (WTException var15) {
         var15.printStackTrace();
      } catch (IOException var16) {
         var16.printStackTrace();
      }

      System.out.println("Excel File name " + excelFile.getName());
      if (excelFile != null && excelFile.isFile()) {
         HashMap extraData = new HashMap();

         try {
            System.out.println("@@@ Setting formresult");
            extraData.put("aN", URLEncoder.encode(Base64.getEncoder().encodeToString(excelFile.getName().getBytes()), "UTF-8"));
            extraData.put("sFN", URLEncoder.encode(Base64.getEncoder().encodeToString(SESSION_FOLDER_NAME.getBytes()), "UTF-8"));
         } catch (UnsupportedEncodingException var14) {
            var14.printStackTrace();
         }

         formResult.setExtraData(extraData);
      }

      return formResult;
   }

   public static Persistable getPersistable(String vr) throws Exception {
      ReferenceFactory rf = new ReferenceFactory();
      WTReference reference = rf.getReference(vr);
      return reference.getObject();
   }
}