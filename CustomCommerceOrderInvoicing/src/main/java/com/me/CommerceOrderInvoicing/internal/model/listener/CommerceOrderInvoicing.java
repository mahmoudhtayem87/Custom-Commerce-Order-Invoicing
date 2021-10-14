package com.me.CommerceOrderInvoicing.internal.model.listener;


		import com.liferay.commerce.model.CommerceOrder;
		import com.liferay.counter.kernel.service.CounterLocalService;
		import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
		import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
		import com.liferay.dynamic.data.mapping.model.LocalizedValue;
		import com.liferay.dynamic.data.mapping.model.Value;
		import com.liferay.dynamic.data.mapping.service.*;
		import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
		import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
		import com.liferay.portal.kernel.exception.ModelListenerException;
		import com.liferay.portal.kernel.exception.PortalException;
		import com.liferay.portal.kernel.model.BaseModelListener;
		import com.liferay.portal.kernel.model.ModelListener;
		import com.liferay.portal.kernel.service.*;

		import java.math.BigDecimal;
		import java.util.*;

		import org.osgi.service.component.annotations.Activate;
		import org.osgi.service.component.annotations.Component;
		import org.osgi.service.component.annotations.Modified;
		import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true,
		service = ModelListener.class
)
public class CommerceOrderInvoicing extends BaseModelListener<CommerceOrder> {

	@Override
	public void onAfterCreate(CommerceOrder commerceOrder) throws ModelListenerException {

	}

	@Override
	public void onAfterUpdate(CommerceOrder commerceOrder) throws ModelListenerException {
		if(commerceOrder.getOrderStatus() != 0  || commerceOrder.isDraft())
		{
			return;
		}
		ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
		try {
			List<VendorTotalAmount> vendors = new ArrayList<>();
			for( int index = 0 ; index < commerceOrder.getCommerceOrderItems().size()  ; index++ )
			{
				addItemToVendor(vendors,commerceOrder.getCommerceOrderItems().get(index).getCPDefinition().getUserId(),
						commerceOrder.getCommerceOrderItems().get(index).getFinalPrice());
			}
			for(int index = 0 ; index < vendors.size() ; index++)
			{
				DDMFormInstance form =
						DDMFormInstanceLocalServiceUtil.fetchDDMFormInstance(47263);
				DDMFormValues ddmFormValues = new DDMFormValues(form.getStructure().getDDMForm());
				Set<Locale> localeSet = new HashSet<Locale>();
				localeSet.add(Locale.forLanguageTag("en-US"));
				ddmFormValues .setAvailableLocales(localeSet);
				ddmFormValues .setDefaultLocale(Locale.forLanguageTag("en-US"));
				List<DDMFormFieldValue> ddmFormFieldValues = new ArrayList<DDMFormFieldValue>();
				DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();
				ddmFormFieldValue.setName("Field49362113");
				Value value = new LocalizedValue();
				value.addString(Locale.forLanguageTag("en-US"), commerceOrder.getCommerceOrderId()+"");
				ddmFormFieldValue.setValue(value);
				ddmFormFieldValues.add(ddmFormFieldValue);
				DDMFormFieldValue ddmFormFieldValue2 = new DDMFormFieldValue();
				ddmFormFieldValue2.setName("Field00112534");
				Value value2 = new LocalizedValue();
				value2.addString(Locale.forLanguageTag("en-US"), vendors.get(index).TotalAmount+"");
				ddmFormFieldValue2.setValue(value2);
				ddmFormFieldValues.add(ddmFormFieldValue2);
				ddmFormValues.setDDMFormFieldValues(ddmFormFieldValues);
				DDMFormInstanceRecord record =
						_ddmFormInstanceRecordLocalService.addFormInstanceRecord(vendors.get(index).VendorID,
								43500,47263,ddmFormValues,serviceContext);
			}
		} catch (PortalException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	void addItemToVendor(List<VendorTotalAmount> vendors,long vendorId,BigDecimal totalAmount)
	{
		boolean vendorFound = false;
		for(int index = 0 ; index < vendors.size();index++)
		{
			if(vendors.get(index).VendorID == vendorId)
			{
				vendors.get(index).TotalAmount= vendors.get(index).TotalAmount.add(totalAmount) ;
			}
		}
		if (!vendorFound)
		{
			VendorTotalAmount vendorTotalAmount = new VendorTotalAmount();
			vendorTotalAmount.TotalAmount = totalAmount;
			vendorTotalAmount.VendorID = vendorId;
			vendors.add(vendorTotalAmount);
		}
	}
	@Activate
	@Modified
	public void activate(Map<String, String> properties) {
		try {

		} catch (Exception e) {

		}
	}



	@Reference
	private DDMFormInstanceRecordLocalService _ddmFormInstanceRecordLocalService;
	@Reference
	private DDMFormInstanceLocalService _ddmFormInstanceLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private CounterLocalService _counterLocalService;

}

class VendorTotalAmount
{
	public long VendorID;
	public BigDecimal TotalAmount;
}