 package ext.enersys.builder.containerEndItemReport;

import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.meta.common.OperationIdentifier;
import com.ptc.windchill.csm.common.CsmConstants;
import com.ptc.windchill.enterprise.change2.commands.RelatedChangesQueryCommands;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.change2.WTChangeOrder2;
import wt.facade.classification.ClassificationFacade;
import wt.fc.ObjectIdentifier;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.httpgw.GatewayAuthenticator;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlHelper;

public class ExportEndItemBOM implements RemoteAccess {
   private static XSSFWorkbook workbook;
   private static final int RANDOM_FOLDER_NAME_LENGTH = 9;
   private static final String tmpLocation;
   private static final String FOLDER_CONSTANT = "ExpEndItemReport_Utility";
   private static final Logger LOGGER = LogR.getLogger(ExportEndItemBOM.class.getName());

   static {
      String tempStr = null;

      try {
         tempStr = WTProperties.getLocalProperties().getProperty("wt.temp");
      } catch (IOException var2) {
         var2.printStackTrace();
      }

      tmpLocation = tempStr;
   }

   public static void main(String[] argv) throws WTException, Exception {
      if (argv.length == 0) {
         System.exit(0);
      }

      String partNo = argv[0];
      RemoteMethodServer rms = RemoteMethodServer.getDefault();
      rms.setUserName("wcadmin");
      rms.setPassword("wcadmin");
      GatewayAuthenticator gwa = new GatewayAuthenticator();
      gwa.setRemoteUser("");
      Class[] argTypes = new Class[]{String.class};
      Object[] argValues = new Object[]{partNo};
      rms.invoke("searchpart", ExportEndItemBOM.class.getName(), (Object)null, argTypes, argValues);
   }

   public static void searchpart(String strPartNo) throws Exception {
      ArrayList<WTPart> selectedParts = new ArrayList();
      String containerName = null;
      QuerySpec querySpec = new QuerySpec(WTPart.class);
      int[] fromIndicies = new int[]{0, -1};
      querySpec.appendWhere(new SearchCondition(WTPart.class, "master>number", "LIKE", strPartNo), fromIndicies);
      querySpec.appendAnd();
      querySpec.appendWhere(new SearchCondition(WTPart.class, "master>endItem", "TRUE"), new int[]{0, 1});
      querySpec.appendAnd();
      querySpec.appendWhere(new SearchCondition(WTPart.class, "iterationInfo.latest", "TRUE"), new int[]{0, 1});
      QueryResult qr = PersistenceHelper.manager.find(querySpec);
      LOGGER.info("Selected ENd Items " + qr.size());
      if (qr.size() == 0) {
         throw new WTException("Cannot find part: " + strPartNo);
      } else {
         WTPart foundPrt = null;

         while(qr.hasMoreElements()) {
            foundPrt = (WTPart)qr.nextElement();
            LOGGER.info("Found part Details:" + foundPrt.getNumber() + "\nVersion:" + foundPrt.getVersionIdentifier().getValue() + "." + foundPrt.getIterationIdentifier().getValue() + "  isLatest:" + foundPrt.isLatestIteration());
            containerName = foundPrt.getContainerName();
            selectedParts.add(foundPrt);
         }

      }
   }

   public static File writeExcel(ArrayList<WTPart> selectedParts, String containerName, String SESSION_FOLDER_NAME) throws WTException, IOException {
      LOGGER.info("Selected End Items " + selectedParts.size());
      workbook = new XSSFWorkbook();
      XSSFCellStyle cellStyle = createCellStyle(true, false, false);
      XSSFCellStyle endItemCellStyle = createCellStyle(false, true, false);
      XSSFCellStyle onlyBorder = createCellStyle(false, false, true);
      XSSFSheet spreadsheet = workbook.createSheet("EndItem BOM ");
      LinkedHashSet<Object[]> BOMData = new LinkedHashSet();
      LOGGER.info("Selected Part Size" + selectedParts.size());
      XSSFRow row = spreadsheet.createRow(0);
      XSSFCell cell1 = row.createCell(0);
      cell1.setCellStyle(cellStyle);
      cell1.setCellValue(containerName);
      cell1 = row.createCell(1);
      int rowid = 1;
      BOMData.add(new Object[]{"Level", "Title", "Part-Number", "Container Name", "Current State", "Current Rev", "Last Released Rev", "Impacting CN", "Quantity", "Unit"});
      Iterator itr1 = BOMData.iterator();

      while(itr1.hasNext()) {
         row = spreadsheet.createRow(rowid++);
         Object[] objectArr1 = (Object[])itr1.next();
         int cellid1 = 1;
         Object[] var17 = objectArr1;
         int var16 = objectArr1.length;

         for(int var15 = 0; var15 < var16; ++var15) {
            Object obj1 = var17[var15];
            XSSFCell cell = row.createCell(cellid1++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((String)obj1);
         }
      }

      for(Iterator var25 = selectedParts.iterator(); var25.hasNext(); row = spreadsheet.createRow(rowid++)) {
         WTPart foundPrt = (WTPart)var25.next();
         LOGGER.info("found Part " + foundPrt.getNumber() + " " + foundPrt.getType() + VersionControlHelper.service.predecessorOf(foundPrt));
         BOMData = getDetails(foundPrt);
         Iterator<Object[]> itr = BOMData.iterator();

         for(boolean endItemrow = true; itr.hasNext(); endItemrow = false) {
            row = spreadsheet.createRow(rowid++);
            Object[] objectArr = (Object[])itr.next();
            int cellid = 1;
            Object[] var21 = objectArr;
            int var20 = objectArr.length;

            for(int var19 = 0; var19 < var20; ++var19) {
               Object obj1 = var21[var19];
               XSSFCell cell = row.createCell(cellid++);
               if (endItemrow) {
                  cell.setCellStyle(endItemCellStyle);
               } else {
                  cell.setCellStyle(onlyBorder);
               }

               cell.setCellValue((String)obj1);
            }
         }

         row = spreadsheet.createRow(rowid++);
      }

      File excelFile = new File(tmpLocation + File.separatorChar + "ExpEndItemReport_Utility" + File.separatorChar + SESSION_FOLDER_NAME + File.separatorChar + containerName + ".xlsx");
      FileUtils.forceMkdirParent(excelFile);
      FileUtils.touch(excelFile);
      FileOutputStream out = new FileOutputStream(excelFile);
      workbook.write(out);
      out.close();
      workbook.close();
      return excelFile;
   }

   public static String getUtilityBaseFolderLocation() {
      String ret = tmpLocation + File.separatorChar + "ExpEndItemReport_Utility";
      return ret;
   }

   public static LinkedHashSet<Object[]> getDetails(WTPart part) throws WTException, IOException {
      LOGGER.info("part" + part.getNumber() + " " + part.getType());
      LinkedHashSet<Object[]> BOMData = new LinkedHashSet();
      Locale locale = SessionHelper.getLocale();
      new PersistableAdapter(part, (String)null, locale, (OperationIdentifier)null);
      String ver = part.getVersionIdentifier().getValue() + "." + part.getIterationIdentifier().getValue();
      String Lastver = findLastPrtRev(part);
      BOMData.add(new Object[]{"0", part.getName(), part.getNumber(), part.getContainerName(), part.getLifeCycleState().getDisplay(), ver, Lastver, impactingCN(part), "", ""});
      findPartUsage(part, BOMData);
      return BOMData;
   }

   public static String getClassificationDisplayName(WTPart part, String internalName) throws WTException {
      String nodeDisplayName = null;
      ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
      new TreeMap();
      nodeDisplayName = facadeInstance.getLocalizedDisplayNameForClassificationNode(internalName, CsmConstants.NAMESPACE, Locale.US);
      return nodeDisplayName;
   }

   public static String findLastPrtRev(WTPart part) throws WTException {
      String previousRev = "";
      WTPart part1 = null;
      QueryResult resultSet = VersionControlHelper.service.allVersionsOf(part.getMaster());
      if (resultSet.hasMoreElements()) {
         part1 = (WTPart)resultSet.nextElement();
         if (resultSet.hasMoreElements()) {
            part1 = (WTPart)resultSet.nextElement();
            previousRev = part1.getVersionIdentifier().getValue() + "." + part1.getIterationIdentifier().getValue();
         }
      }

      return previousRev;
   }

   public static String impactingCN(WTPart part) throws WTException {
      WTCollection coll = RelatedChangesQueryCommands.getRelatedResultingChangeNotices(part);
      Iterator itr = coll.iterator();
      String ECNNumber = "";

      for(boolean first = true; itr.hasNext(); LOGGER.info(" *** Inside GETECNNumber Expression ObjectReference  :: " + ECNNumber)) {
         ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(itr.next().toString());
         WTChangeOrder2 ecn = (WTChangeOrder2)PersistenceHelper.manager.refresh(oid);
         ECNNumber = ECNNumber.concat("  ").concat(ecn.getNumber());
         if (first) {
            ECNNumber = ECNNumber;
         } else {
            ECNNumber = "," + ECNNumber;
         }
      }

      return ECNNumber;
   }

   public static void findPartUsage(WTPart part, LinkedHashSet<Object[]> BOMData) throws WTException, IOException {
      WTPartMaster masterChildPrt = null;
      QueryResult qr1 = PersistenceHelper.manager.navigate(part, "roleBObject", WTPartUsageLink.class, false);
      WTPartUsageLink ulink = null;
      WTPart latestPrt = null;
      LOGGER.info("BOM Data for parent:..." + part.getNumber() + part.getName());

      while(qr1.hasMoreElements()) {
         ulink = (WTPartUsageLink)qr1.nextElement();
         Quantity qty = ulink.getQuantity();
         Locale locale = SessionHelper.getLocale();
         masterChildPrt = (WTPartMaster)ulink.getRoleBObject();
         QueryResult qr2 = VersionControlHelper.service.allVersionsOf(masterChildPrt);
         if (qr2.hasMoreElements()) {
            latestPrt = (WTPart)qr2.nextElement();
         }

         String ver = latestPrt.getVersionIdentifier().getValue() + "." + latestPrt.getIterationIdentifier().getValue();
         String Lastver = findLastPrtRev(latestPrt);
         LOGGER.info("CHild Part Number " + latestPrt.getNumber());
         Locale locale1 = SessionHelper.getLocale();
         new PersistableAdapter(latestPrt, (String)null, locale1, (OperationIdentifier)null);
         BOMData.add(new Object[]{"1", latestPrt.getName(), latestPrt.getNumber(), latestPrt.getContainerName(), latestPrt.getLifeCycleState().getDisplay(), ver, Lastver, impactingCN(latestPrt), String.valueOf(qty.getAmount()), qty.getUnit().toString()});
         LOGGER.info("BOM Data for child:..." + latestPrt.getNumber() + latestPrt.getName());
      }

   }

   private static XSSFCellStyle createCellStyle(boolean addGreyBG, boolean addLighterGreyBG, boolean onlyBorder) {
      XSSFCellStyle cellStyle = workbook.createCellStyle();
      XSSFColor color;
      XSSFFont defaultFont;
      if (addGreyBG) {
         color = new XSSFColor(Color.LIGHT_GRAY);
         cellStyle.setFillForegroundColor(color);
         cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         defaultFont = workbook.createFont();
         defaultFont.setBold(false);
         defaultFont.setFontName("Times New Roman");
         defaultFont.setColor(IndexedColors.BLACK.getIndex());
         defaultFont.setItalic(false);
         cellStyle.setFont(defaultFont);
         cellStyle.setWrapText(false);
         cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
         cellStyle.setAlignment(HorizontalAlignment.LEFT);
         cellStyle.setBorderBottom(BorderStyle.THICK);
         cellStyle.setBorderLeft(BorderStyle.THICK);
         cellStyle.setBorderTop(BorderStyle.THICK);
         cellStyle.setBorderRight(BorderStyle.THICK);
      } else if (addLighterGreyBG) {
         color = new XSSFColor(new Color(241, 241, 241));
         cellStyle.setFillForegroundColor(color);
         cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         defaultFont = workbook.createFont();
         defaultFont.setBold(false);
         defaultFont.setFontName("Calibri");
         defaultFont.setColor(IndexedColors.BLACK.getIndex());
         defaultFont.setItalic(false);
         cellStyle.setFont(defaultFont);
         cellStyle.setWrapText(false);
         cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
         cellStyle.setAlignment(HorizontalAlignment.LEFT);
         cellStyle.setBorderBottom(BorderStyle.THIN);
         cellStyle.setBorderLeft(BorderStyle.THIN);
         cellStyle.setBorderTop(BorderStyle.THIN);
         cellStyle.setBorderRight(BorderStyle.THIN);
      } else if (onlyBorder) {
         cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
         cellStyle.setAlignment(HorizontalAlignment.LEFT);
         cellStyle.setBorderBottom(BorderStyle.THIN);
         cellStyle.setBorderLeft(BorderStyle.THIN);
         cellStyle.setBorderTop(BorderStyle.THIN);
         cellStyle.setBorderRight(BorderStyle.THIN);
      }

      return cellStyle;
   }
}