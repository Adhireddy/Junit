package com.kibo.ng.bis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.Entity;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Inventories;
import com.kibo.ng.bis.jaxb.Inventory;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.Marketlive.Results;
import com.kibo.ng.bis.jaxb.Order;
import com.kibo.ng.bis.jaxb.Order.Payments;
import com.kibo.ng.bis.jaxb.Order.Payments.Payment;
import com.kibo.ng.bis.jaxb.OrderPaymentCreditCard;
import com.kibo.ng.bis.jaxb.Orders;
import com.kibo.ng.bis.jaxb.Output;
import com.kibo.ng.bis.jaxb.PriceList;
import com.kibo.ng.bis.jaxb.PriceListItem;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.CommandRequestType;
import com.kibo.ng.bis.model.CommandType;
import com.kibo.ng.bis.model.ConfigModel;

@Service
public class OrderStatusCommandService extends MozuApiService<Order>{

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDefaultOrderByString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception {
		// TODO Auto-generated method stub
		
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
