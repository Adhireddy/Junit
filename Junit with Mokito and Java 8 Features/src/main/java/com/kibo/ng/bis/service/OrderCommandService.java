package com.kibo.ng.bis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Order;
import com.kibo.ng.bis.jaxb.Order.Payments;
import com.kibo.ng.bis.jaxb.Order.Payments.Payment;
import com.kibo.ng.bis.jaxb.OrderPaymentCreditCard.Number;
import com.kibo.ng.bis.jaxb.OrderPaymentCreditCard;
import com.kibo.ng.bis.jaxb.Orders;
import com.kibo.ng.bis.jaxb.Output;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.ConfigModel;

@Service
public class OrderCommandService extends MozuApiService<Order> {

	@Override
	public boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config, CatalogModel catalogModel) {
		List<Order> orders = importCommand.getOrders().getOrder();
		for (Order order : orders) {
			importEntityRecord(importCommand, order, marketLive, config, catalogModel);
		}
		return true;
	}

	@Override
	public void setInputRecord(InputRecord record, Order order) {
		record.setOrder(order);
	}

	@Override
	public void insertEntity(Order order, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity(Order order, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateInsertEntity(Order order, Marketlive marketLive, CatalogModel catalogModel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception {
		CommandResult result = marketLive.getResults().getResult().get(0);
		result.setOutput(new Output());
		Orders ordersExport = new Orders();
		result.getOutput().setOrders(ordersExport);

		List<Order> orderList = ordersExport.getOrder();

		/*OrderResource orderResource = new OrderResource(apiContext);
		List<com.mozu.api.contracts.commerceruntime.orders.Order> ngorderList = null;
		Integer startIndex = 0;
		do {
			ngorderList = orderResource.getOrders(startIndex, PAGE_SIZE, orderByStr, filterStr, null, null, null)
					.getItems();

			for (com.mozu.api.contracts.commerceruntime.orders.Order ngOrder : ngorderList) {
				Order orderExport = new Order();

				orderExport.setCode(ngOrder.getParentOrderId());
				orderExport.setAdditionalAddressTotal(new BigDecimal(0.0));
				orderExport.setAdditionalChargesTotal(new BigDecimal(0.0));
				// TODO: Payment - not finding related mapping details
				Payments payments = new Payments();

				for (com.mozu.api.contracts.commerceruntime.payments.Payment ngPayment : ngOrder.getPayments()) {
					Payment payment = new Payment();

					payment.getOrderPaymentCardToken()
							.setCreditCardType(ngPayment.getBillingInfo().getCard().getPaymentOrCardType());
					payment.getOrderPaymentCardToken()
							.setExpMonth(ngPayment.getBillingInfo().getCard().getExpireMonth() + "");
					payment.getOrderPaymentCardToken()
							.setExpYear(ngPayment.getBillingInfo().getCard().getExpireYear() + "");
					payment.getOrderPaymentCardToken()
							.setMaskedNumber(ngPayment.getBillingInfo().getCard().getCardNumberPartOrMask());

					OrderPayment orderPayment = new OrderPayment();
					payment.getOrderPaymentCardToken().setOrderPayment(orderPayment);

					// payment.getOrderPaymentCardToken().setPk(ngPayment.getBillingInfo().getCard().getPaymentServiceCardId());
					payment.getOrderPaymentCardToken().setToken(null);

					payment.getOrderPaymentCreditCard()
							.setExpMonth(ngPayment.getBillingInfo().getCard().getExpireMonth() + "");
					payment.getOrderPaymentCreditCard()
							.setExpYear(ngPayment.getBillingInfo().getCard().getExpireYear() + "");
					payment.getOrderPaymentCreditCard()
							.setKeyCode(ngPayment.getBillingInfo().getCard().getPaymentServiceCardId());

					Number number = new Number();
					//number.setEncrypted(true);
					number.setValue(ngPayment.getBillingInfo().getCard().getCardNumberPartOrMask());
					payment.getOrderPaymentCreditCard().setNumber(number);

					OrderPayment orderPayment1 = new OrderPayment();
					// orderPayment1.getAttributePages().getAttributePage().get(0).getAttributes().se

					payment.getOrderPaymentCreditCard().setOrderPayment(orderPayment1);

					payment.getOrderPaymentCreditCard().setPk(null);
					payment.getOrderPaymentCreditCard()
							.setType(ngPayment.getBillingInfo().getCard().getPaymentOrCardType());

					// TODO : determine other fields
					payment.getOrderPaymentCheck().setAccountNumber(null);

					payment.getOrderPaymentGiftCertificate().setBalance(null);
					payment.getOrderPaymentGiftCertificate().setNumber(null);
					payment.getOrderPaymentGiftCertificate().setOrderPayment(null);
					payment.getOrderPaymentGiftCertificate().setPk(0);

					payment.getOrderPaymentOnCredit().setOrderPayment(null);
					payment.getOrderPaymentGiftCertificate().setOrderPayment(null);
					payment.getOrderPaymentGiftCertificate().setPk(0);

					payment.getOrderPaymentPayPal();

					payments.getPayment().add(payment);

				}
				orderExport.setPayments(payments);

				// TODO: AttributePages
				AttributePages attributePages = new AttributePages();
				AttributePage attributePage = new AttributePage();
				AttributePageData attributePageData = new AttributePageData();

				attributePageData.setFirstName(null);
				attributePageData.setLastName(null);
				attributePageData.setCity(null);

				attributePage.setAttributes(attributePageData);
				attributePages.getAttributePage().add(attributePage);

				orderExport.setAttributePages(attributePages);

				// TODO :AuthorizationsRequests

				AuthorizationsRequests authorizationsRequests = new AuthorizationsRequests();
				AuthorizationsRequest authorizationsRequest = new AuthorizationsRequest();

				authorizationsRequest.setAuthorizationsRequest(null);

				authorizationsRequests.setAuthorizationsRequest(authorizationsRequest);
				orderExport.setAuthorizationsRequests(authorizationsRequests);

				// B2BOrder

				B2BOrder b2BOrder = new B2BOrder();
				b2BOrder.getPurchaseOrderNumber().add(ngOrder.getOrderNumber() + "");

				orderExport.setB2BOrder(b2BOrder);

				// Shippment related
				ngOrder.getShipments();
				ngOrder.getShippingAdjustment().getAmount();
				ngOrder.getShippingAmountBeforeDiscountsAndAdjustments();
				ngOrder.getShippingDiscounts();
				ngOrder.getShippingSubTotal();
				ngOrder.getShippingTaxTotal();

				Shipments shipments = new Shipments();
				orderExport.setShipments(shipments);
				orderExport.setShippingCostTotal(new BigDecimal(0.0));
				orderExport.setShippingLocationTotal(new BigDecimal(0.0));
				orderExport.setShippingMethodTotal(new BigDecimal(0.0));
				orderExport.setShippingStateTotal(new BigDecimal(0.0));
				orderExport.setShippingTotal(new BigDecimal(ngOrder.getShippingTotal()));
				orderExport.setShippingWeightTotal(new BigDecimal(0.0));

				// OrderBillShipInfo

				OrderBillShipInfo orderBillShipInfo = new OrderBillShipInfo();

				orderBillShipInfo.setAttributePages(null);
				orderBillShipInfo.setCode(ngOrder.getBillingInfo().getPurchaseOrder().getPurchaseOrderNumber());
				orderBillShipInfo.setCompany(ngOrder.getBillingInfo().getBillingContact().getCompanyOrOrganization());
				orderBillShipInfo.setDateCreated(
						dateTimeToXMLGregorianCalendar(ngOrder.getBillingInfo().getAuditInfo().getCreateDate()));
				orderBillShipInfo.setDateModified(
						dateTimeToXMLGregorianCalendar(ngOrder.getBillingInfo().getAuditInfo().getUpdateDate()));
				orderBillShipInfo.setEmail(ngOrder.getBillingInfo().getBillingContact().getEmail());
				orderBillShipInfo.setExtension(null);
				orderBillShipInfo.setFax(null);
				orderBillShipInfo.setOriginalOrderBillShipInfoId(null);
				orderBillShipInfo.setPhone1(ngOrder.getBillingInfo().getBillingContact().getPhoneNumbers().getMobile());
				orderBillShipInfo.setPhone2(ngOrder.getBillingInfo().getBillingContact().getPhoneNumbers().getWork());
				orderBillShipInfo.setPk(null);

				orderExport.setBillToInfo(orderBillShipInfo);

				orderExport.setBorderFreeOrderConfAttempt(0);
				orderExport.setBorderFreeOrderFlag(0);
				orderExport.setCampaignSourceCode(null);
				orderExport.setChannel(null);
				orderExport.setComment1(ngOrder.getNotes().get(0).getText());
				// orderExport.setCustomerCode(ngOrder.getCustomerAccountId());
				orderExport.setDateBorderFreeOrderConfirmation(null);
				orderExport.setDateCreated(dateTimeToXMLGregorianCalendar(ngOrder.getAuditInfo().getCreateDate()));
				orderExport.setDateDeleted(null);
				orderExport.setDateModified(dateTimeToXMLGregorianCalendar(ngOrder.getAuditInfo().getUpdateDate()));
				orderExport.setDateOrdered(dateTimeToXMLGregorianCalendar(ngOrder.getSubmittedDate()));
				orderExport.setDeferred(false);

				// Discounts
				Discounts discounts = new Discounts();

				ngOrder.getShippingDiscounts();

				for (com.mozu.api.contracts.commerceruntime.discounts.AppliedDiscount sppliedDiscount : ngOrder
						.getOrderDiscounts()) {
					OrderDiscount orderDiscount = new OrderDiscount();

					ngOrder.getDiscountedSubtotal();
					ngOrder.getHandlingDiscounts();

					orderDiscount.setSingleUseCouponsAsString(sppliedDiscount.getCouponCode());
					orderDiscount.setAmount(new BigDecimal(ngOrder.getDiscountedTotal()));
					orderDiscount.setInitialTotal(new BigDecimal(ngOrder.getDiscountTotal()));
					orderDiscount.setMessage(null);
					orderDiscount.setPriceListCode(null);
					orderDiscount.setPriceListName(null);
					orderDiscount.setPricingCode(null);
					orderDiscount.setPricingName(null);
					orderDiscount.setSourceCodesAsString(null);

					discounts.getDiscount().add(orderDiscount);
				}

				orderExport.setDiscounts(discounts);

				orderExport.setRefundAmount(new BigDecimal(ngOrder.getAmountRefunded()));

				orderExport.setGiftWrapTotal(new BigDecimal(0.0));

				orderExport.setInvoiceNumber(ngOrder.getOrderNumber() + "");
				orderExport.setLocale(null);
				orderExport.setMerchandiseTotal(new BigDecimal(0.0));
				orderExport.setMultipleAddresses(false);
				orderExport.setDeleted(false);
				orderExport.setOrderProviderType(ngOrder.getType());

				orderExport.setPk(ngOrder.getId());
				orderExport.setRefund(ngOrder.getIsEligibleForReturns());
				orderExport.setRefundAmount(new BigDecimal(ngOrder.getAmountAvailableForRefund()));
				orderExport.setSingleUseCouponsAsString(ngOrder.getCouponCodes().get(0));
				orderExport.setSiteCode(ngOrder.getSiteId() + "");
				orderExport.setSourceCodesAsString(ngOrder.getSourceDevice());
				orderExport.setStatus(ngOrder.getStatus());
				orderExport.setSubTotal(new BigDecimal(ngOrder.getSubtotal()));
				orderExport.setTaxTotal(new BigDecimal(ngOrder.getTaxTotal()));
				orderExport.setTotal(new BigDecimal(ngOrder.getTotal()));

				// Trackings

				ngOrder.getPackages().get(0).getTrackingNumber();

				Trackings trackings = new Trackings();
				OrderTracking orderTracking = new OrderTracking();

				orderTracking.setAttributePages(null);
				orderTracking.setBorderFreePackagingSlipUrl(null);
				orderTracking.setBorderFreeParcelNotifAttempt(null);
				orderTracking.setCarrierName(ngOrder.getChannelCode());// ngOrder.getChannelCode()
				orderTracking.setCarrierURL(null);
				orderTracking.setCode(ngOrder.getChannelCode());
				orderTracking.setDateBorderFreeParcelNotification(null);
				orderTracking.setDateCreated(dateTimeToXMLGregorianCalendar(ngOrder.getSubmittedDate()));
				orderTracking.setDateModified(dateTimeToXMLGregorianCalendar(ngOrder.getLastValidationDate()));
				orderTracking.getOrder().setChannel(ngOrder.getChannelCode());
				// TODO : determine other field mapping
				orderTracking.getOrder();

				orderTracking.getOrderItem().setAttributePages(null);
				orderTracking.getOrderItem().setB2BOrderItem(null);
				orderTracking.getOrderItem().setCode(ngOrder.getItems().get(0).getId());
				orderTracking.getOrderItem().setProductCode(ngOrder.getItems().get(0).getProduct().getProductCode());
				orderTracking.getOrderItem().setDateCreated(
						dateTimeToXMLGregorianCalendar(ngOrder.getItems().get(0).getAuditInfo().getCreateDate()));
				orderTracking.getOrderItem().setDateModified(null);
				orderTracking.getOrderItem().setDiscounts(null);
				orderTracking.getOrderItem().setFreeGift(false);
				orderTracking.getOrderItem().setItemAdjustmentAmount(
						new BigDecimal(ngOrder.getItems().get(0).getAdjustedLineItemSubtotal()));
				orderTracking.getOrderItem().setItemAdjustmentAmountPerQty(new BigDecimal(0.0));
				orderTracking.getOrderItem().setItemAdjustmentValue(new BigDecimal(0.0));

				ngOrder.getItems().get(0).getAdjustedLineItemSubtotal();
				ngOrder.getItems().get(0).getAdjustedLineItemSubtotal();

				trackings.getOrderTracking().add(orderTracking);
				orderExport.setTrackings(trackings);

				orderExport.setWeightSurchargeTotal(null);
				orderExport.setShippingStateTotal(null);

				// ngOrder.getAcceptsMarketing();
				// ngOrder.getAdjustment().getAmount();
				// ngOrder.getAdjustment().getDescription();
				// ngOrder.getAdjustment().getInternalComment();
				// ngOrder.getAmountRemainingForPayment();

				orderList.add(orderExport);

			}
			startIndex = startIndex + PAGE_SIZE;
		} while (orderList.size() > 0);
		*/
		for(int i = 0; i < 10; i++) {
			Order orderExport = new Order();
			orderExport.setCode(i+"abcd");
			orderExport.setComment2("Hello " + i);
			Payments payments = new Payments();
			List<Payment> paymentsList = payments.getPayment();
			
			Payment payment = new Payment();
			OrderPaymentCreditCard card = new OrderPaymentCreditCard();
			String ccNo = "hQEMAw5smc5oRuL4AQgAkn2yr3YzjlvKbR3PYNWyBaC50Po9lth89HHC7raro41bk7+bRXk/DQH6"
					+ "ZbbPIJD6O6Ld3WVzrsorzuu0C+7gEFk+8tbHh3KBPBIwiGHEk9MHWup7MfhR5dfg8QM1U5Gx8kZH"
					+ "5fIZNd4hPV96Fh/UsNGv0NzMs6p8E0YainxYeqz8b4sUI1kkw5tDtm4YbEk/h2G1IuBX9P4xTsBU"
					+ "4tpIWGBkmj9UwG6EOlZOO3wFfumzeFJqM38ZgR6bd5MWzGjgMDOAwTclPX1bMXphUlljUS2QE8ez"
					+ "QKIyv96FNpV8DTyFGdpcXT3VI5L5eMORyOgjPg3sX0i3wrTWRu+avWa3ZckxgnvsAVj6h8+fKW3j"
					+ "56UpNjj1nqIfMSqQmPsd9sLUAbTQi9Ys5Wkl6VHk+M6PwHuq+g==";
			Number ccNoFormat = new Number();
			
			ccNoFormat.setEncrypted(true);
			ccNoFormat.setValue(ccNo);
			card.setNumber(ccNoFormat);
			payment.setOrderPaymentCreditCard(card);
			paymentsList.add(payment);
			orderExport.setPayments(payments);
			orderList.add(orderExport);
		}
	}

	@Override
	public String getDefaultOrderByString() {
		return "acceptedDate";
	}

	@Override
	void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception {

	}

	@Override
	public void decryptFields(List<CommandResult> results, ConfigModel config) {
		if(results != null) {
			for (CommandResult commandResult : results) {
				Output output = commandResult.getOutput();
				if(output != null) {
					Orders orders = output.getOrders();
					if(orders != null) {
						List<Order> orderList = orders.getOrder();
						for (Order order : orderList) {
							Payments payments = order.getPayments();
							List<Payment> list = payments.getPayment();
							for (Payment payment : list) {
								OrderPaymentCreditCard cc = payment.getOrderPaymentCreditCard();
								if(cc != null) {
									com.kibo.ng.bis.jaxb.OrderPaymentCreditCard.Number num = cc.getNumber();
									String ccNumber = num.getValue();
									if(ccNumber != null){
										num.setValue(decryptString(ccNumber, config));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void setInitialData(ConfigModel config) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
