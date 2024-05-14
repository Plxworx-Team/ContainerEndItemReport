package ext.enersys.builder.containerEndItemReport;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import wt.fc.ObjectIdentifier;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;

@ComponentBuilder({"ext.enersys.builder.containerEndItemReport.ContainerEndItemTableBuilder"})
public class ContainerEndItemTableBuilder extends AbstractComponentBuilder {
   public Object buildComponentData(ComponentConfig arg0, ComponentParams componentParams) throws Exception {
      System.out.println("Component Config" + arg0);
      System.out.println("Component Config" + componentParams);
      NmCommandBean localNmCommandBean = ((JcaComponentParams)componentParams).getHelperBean().getNmCommandBean();
      NmOid primaryOid = localNmCommandBean.getActionOid();
      ObjectIdentifier primaryObjIdentifier = primaryOid.getOidObject();
      System.out.println(primaryObjIdentifier + " " + primaryObjIdentifier.toString());
      String obid = primaryObjIdentifier.toString();
      obid = obid.substring(obid.lastIndexOf(":") + 1);
      long longId = Long.parseLong(obid);
      QuerySpec qspec = new QuerySpec(WTPart.class);
      qspec.appendWhere(new SearchCondition(WTPart.class, WTPart.CONTAINER_ID, "LIKE", longId), new int[]{0, 1});
      qspec.appendAnd();
      qspec.appendWhere(new SearchCondition(WTPart.class, "master>endItem", "TRUE"), new int[]{0, 1});
      qspec.appendAnd();
      qspec.appendWhere(new SearchCondition(WTPart.class, "iterationInfo.latest", "TRUE"), new int[]{0, 1});
      QueryResult qr = PersistenceHelper.manager.find(qspec);
      System.out.println("ENd-Item Query Size All " + qr.size());
      LatestConfigSpec configSpec = new LatestConfigSpec();
      qr = configSpec.process(qr);
      System.out.println("ENd-Item Query Size Latest" + qr.size());
      return qr;
   }

   public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
      ComponentConfigFactory factory = this.getComponentConfigFactory();
      TableConfig table = factory.newTableConfig();
      table.setLabel("Select End Items for single Level BOM");
      table.setSelectable(true);
      table.addComponent(factory.newColumnConfig("type", true));
      table.addComponent(factory.newColumnConfig("number", true));
      table.addComponent(factory.newColumnConfig("name", true));
      table.addComponent(factory.newColumnConfig("version", true));
      table.addComponent(factory.newColumnConfig("state", true));
      table.addComponent(factory.newColumnConfig("thePersistInfo.modifyStamp", true));
      table.addComponent(factory.newColumnConfig("ENERSYS_CLASSIFICATION_BINDING_ATTR", true));
      return table;
   }
}