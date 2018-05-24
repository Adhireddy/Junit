package com.kibo.ng.bis.service;

import java.io.FileInputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.CommandResult.Failures;
import com.kibo.ng.bis.jaxb.CommandResult.Summary;
import com.kibo.ng.bis.jaxb.CommandResult;
import com.kibo.ng.bis.jaxb.Entity;
import com.kibo.ng.bis.jaxb.ExportCommand;
import com.kibo.ng.bis.jaxb.Failure;
import com.kibo.ng.bis.jaxb.Failure.ErrorFields;
import com.kibo.ng.bis.jaxb.Failure.FailedRecord;
import com.kibo.ng.bis.jaxb.FindByCodeParameters;
import com.kibo.ng.bis.jaxb.Failure.ErrorFields.ErrorField;
import com.kibo.ng.bis.jaxb.ImportCommand;
import com.kibo.ng.bis.jaxb.InputRecord;
import com.kibo.ng.bis.jaxb.Marketlive;
import com.kibo.ng.bis.jaxb.SkuPrice;
import com.kibo.ng.bis.model.CatalogModel;
import com.kibo.ng.bis.model.CommandType;
import com.kibo.ng.bis.model.ConfigModel;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.appdev.AppAuthInfo;
import com.mozu.api.contracts.productadmin.ProductPrice;
import com.mozu.api.contracts.productadmin.ProductVariation;
import com.mozu.api.contracts.productadmin.ProductVariationFixedPrice;
import com.mozu.api.security.AppAuthenticator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;

// TODO: Auto-generated Javadoc
/**
 * The Class MozuApiService.
 *
 * @param <Entity> the generic type
 */
@Service
public abstract class MozuApiService<Entity> {

	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** The page size. */
	public static Integer PAGE_SIZE = 100;

	/** The api context. */
	protected MozuApiContext apiContext;
	
	/** The format. */
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	/** The api call enabled. */
	@Value("${api.call.enabled}")
	private Boolean API_CALL_ENABLED;

	/** The field map. */
	@Resource(name = "fieldMap")
	HashMap<String, String> fieldMap;

	/** The criteria map. */
	@Resource(name = "criteriaMap")
	HashMap<String, String> criteriaMap;

	/** The order by map. */
	@Resource(name = "orderByMap")
	HashMap<String, String> orderByMap;
	
	/** The email service. */
	@Autowired
	private EmailService emailService;

	/**
	 * Api authentication.
	 *
	 * @param config the config
	 */
	public void apiAuthentication(ConfigModel config) {

		AppAuthInfo appAuthInfo = new AppAuthInfo();
		appAuthInfo.setApplicationId(config.getAppId());
		appAuthInfo.setSharedSecret(config.getSharedSecret());
		AppAuthenticator.initialize(appAuthInfo);
		apiContext = new MozuApiContext(config.getTenantId(), config.getSiteId());	
	}

	/**
	 * Import command.
	 *
	 * @param importCommand the import command
	 * @param marketLive the market live
	 * @param config the config
	 * @param catalogModel the catalog model
	 * @return true, if successful
	 */
	public abstract boolean importCommand(ImportCommand importCommand, Marketlive marketLive, ConfigModel config, CatalogModel catalogModel);

	/**
	 * Insert entity.
	 *
	 * @param entity the entity
	 * @param marketLive the market live
	 * @param catalogModel the catalog model
	 * @throws Exception the exception
	 */
	public abstract void insertEntity(Entity entity, Marketlive marketLive, CatalogModel catalogModel) throws Exception;

	/**
	 * Update entity.
	 *
	 * @param entity the entity
	 * @param marketLive the market live
	 * @param catalogModel the catalog model
	 * @throws Exception the exception
	 */
	public abstract void updateEntity(Entity entity, Marketlive marketLive, CatalogModel catalogModel) throws Exception;

	/**
	 * Update insert entity.
	 *
	 * @param entity the entity
	 * @param marketLive the market live
	 * @param catalogModel the catalog model
	 * @throws Exception the exception
	 */
	public abstract void updateInsertEntity(Entity entity, Marketlive marketLive, CatalogModel catalogModel) throws Exception;

	/**
	 * Sets the input record.
	 *
	 * @param record the record
	 * @param entity the entity
	 */
	public abstract void setInputRecord(InputRecord record, Entity entity);

	/**
	 * Sets the initial data.
	 *
	 * @throws Exception the exception
	 */
	public abstract void setInitialData(ConfigModel config) throws Exception;

	/**
	 * Import entity record.
	 *
	 * @param importCommand the import command
	 * @param entity the entity
	 * @param marketLive the market live
	 * @param config the config
	 * @param catalogModel the catalog model
	 */
	public void importEntityRecord(ImportCommand importCommand, Entity entity, Marketlive marketLive,
			ConfigModel config, CatalogModel catalogModel) {
			
		try {
			apiAuthentication(config);
			setInitialData(config);
			updateResults(marketLive);
			CommandType type = CommandType.getType(importCommand.getType());
			switch (type) {
			case INSERT:
				insertEntity(entity, marketLive, catalogModel);
				break;
			case UPDATE:
				updateEntity(entity, marketLive, catalogModel);
				break;
			case CREATEORUPDATE:
				updateInsertEntity(entity, marketLive, catalogModel);
				break;
			default:
				logger.error("Error: Can't find the command type");
				break;
			}
		
		} catch (Exception e) {
			logger.error("Error accessing API {}", e);
			emailService.sendFatalEmail(e.getMessage());
			addFailure(marketLive, entity, e.getMessage());
		}
	}

	/**
	 * This method adds the failure.
	 *
	 * @param marketLive the market live
	 * @param entity the entity
	 * @param exceptionMsg the exception msg
	 */
	public void addFailure(Marketlive marketLive, Entity entity, String exceptionMsg) {
		Failure failure = new Failure();
		com.kibo.ng.bis.jaxb.Entity en = (com.kibo.ng.bis.jaxb.Entity) entity;
		failure.setCode(1); // TODO what to set here ? question to be asked

		ErrorFields fields = new ErrorFields();
		ErrorField field = new ErrorField();
		field.setFieldName("What to set here ?");
		field.setValue("What to Set here ");

		fields.getErrorField().add(field);
		failure.setErrorFields(fields);

		FailedRecord failedRecord = new FailedRecord();
		failedRecord.setEntity("What to set here ?");
		failedRecord.setID("What to set here ?");
		failure.setFailedRecord(failedRecord);

		JAXBElement<String> element = new JAXBElement<String>(
				new QName("http://marketlive.com/integration/xmlbean", "message"), String.class, exceptionMsg);

		failure.setMessage(element);
		InputRecord record = new InputRecord();
		failure.setInputRecord(record);
		setInputRecord(record, entity);

		if (marketLive.getResults().getResult().get(0).getFailures() == null)
			marketLive.getResults().getResult().get(0).setFailures(new Failures());

		marketLive.getResults().getResult().get(0).getFailures().getFailure().add(failure);

		Summary summary = marketLive.getResults().getResult().get(0).getSummary();
		if (summary.getFailed() == null) {
			summary.setFailed(1);
		} else {
			summary.setFailed(summary.getFailed() + 1);
		}
	}

	/**
	 * This method updates results.
	 *
	 * @param marketLive the market live
	 */
	private void updateResults(Marketlive marketLive) {
		Summary summary = marketLive.getResults().getResult().get(0).getSummary();
		if (summary.getTotal() == null) {
			summary.setTotal(1);
		} else {
			summary.setTotal(summary.getTotal() + 1);
		}
	}

	/**
	 * Export entity.
	 *
	 * @param exportCommand the export command
	 * @param marketLive the market live
	 * @return true, if successful
	 */
	public abstract boolean exportEntity(ExportCommand exportCommand, Marketlive marketLive);

	/**
	 * Export entity by criteria.
	 *
	 * @param exportCommand the export command
	 * @param marketLive the market live
	 * @param config the config
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean exportEntityByCriteria(ExportCommand exportCommand, Marketlive marketLive, ConfigModel config)
			throws Exception {

		apiAuthentication(config);
		String hql = exportCommand.getFindByCriteriaParameters().getHql();
		String filterStr = getFilterString(hql);
		String orderByStr = getOrderByString(hql);
		exportEntityByCriteria(marketLive, filterStr, orderByStr);
		return true;

	}

	/**
	 * This method exports entity by code.
	 *
	 * @param exportCommand the export command
	 * @param marketLive the market live
	 * @param config the config
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean exportEntityByCode(ExportCommand exportCommand, Marketlive marketLive, ConfigModel config)
			throws Exception {
		apiAuthentication(config);
		exportEntityByCode(marketLive, exportCommand.getFindByCodeParameters());
		return true;
	}

	/**
	 * This method exports entity by criteria.
	 *
	 * @param marketLive the market live
	 * @param filterStr the filter str
	 * @param orderByStr the order by str
	 * @throws Exception the exception
	 */
	abstract void exportEntityByCriteria(Marketlive marketLive, String filterStr, String orderByStr) throws Exception;

	/**
	 * This method exports entity by code.
	 *
	 * @param marketLive the market live
	 * @param code the code
	 * @throws Exception the exception
	 */
	abstract void exportEntityByCode(Marketlive marketLive, FindByCodeParameters code) throws Exception;

	/**
	 * This method gets the filter string.
	 *
	 * @param hql the hql
	 * @return the filter string
	 * @throws JSQLParserException the JSQL parser exception
	 */
	public String getFilterString(String hql) throws JSQLParserException {

		hql = formatHQL(hql);

		logger.debug("<HQL> -{}", hql);

		Select select = (Select) CCJSqlParserUtil.parse(new StringReader(hql));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		Expression whereExp = plainSelect.getWhere();
		String name = ((Table) plainSelect.getFromItem()).getName();

		String filter = handleExpression(whereExp, name);
		logger.debug("<FILTER> - {}", filter);
		// filter = "auditInfo.updateDate gt 2017-01-02";
		return filter;
	}

	/**
	 * This method formats HQL.
	 *
	 * @param hql the hql
	 * @return the string
	 */
	private String formatHQL(String hql) {
		StringTokenizer tokens = new StringTokenizer(hql);
		tokens.nextToken();
		tokens.nextToken();
		String str3 = tokens.nextToken();

		hql = "SELECT " + str3 + " " + hql;
		hql = hql.replaceAll("&gt;", ">");
		hql = hql.replaceAll("\\b(?i)is\\b", "=");
		hql = hql.replaceAll("(?i)= NOT NULL", "is not null");
		hql = hql.replaceAll("(?i)= NULL", "is null");
		hql = hql.replaceAll("(?i)Order o", "Orders o");
		return hql;
	}

	/**
	 * This method gets the order by string.
	 *
	 * @param hql the hql
	 * @return the order by string
	 * @throws Exception the exception
	 */
	public String getOrderByString(String hql) throws Exception {
		hql = formatHQL(hql);
		Select select = (Select) CCJSqlParserUtil.parse(new StringReader(hql));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

		ArrayList<OrderByElement> orderByList = (ArrayList<OrderByElement>) plainSelect.getOrderByElements();
		if (orderByList != null) {
			for (OrderByElement orderByElement : orderByList) {
				return orderByElement.getExpression().toString();
			}
		} else {
			return getDefaultOrderByString();
		}
		return "";
	}

	/**
	 * Gets the default order by string.
	 *
	 * @return the default order by string
	 */
	public abstract String getDefaultOrderByString();

	/**
	 * This method handles expression.
	 *
	 * @param exp the exp
	 * @param name the name
	 * @return the string
	 */
	public String handleExpression(Expression exp, String name) {
		if (exp instanceof IsNullExpression) {
			return handleIsNullExpression((IsNullExpression) exp, name);
		} else if (exp instanceof AndExpression) {
			return handleAndExpression((AndExpression) exp, name);
		} else if (exp instanceof EqualsTo) {
			return handleEQExpression((EqualsTo) exp, name);
		} else if (exp instanceof InExpression) {
			return handleInExpression((InExpression) exp, name);
		} else if (exp instanceof GreaterThan) {
			return handleGtExpression((GreaterThan) exp, name);
		} else if (exp instanceof Subtraction) {
			return handleSubtractionExpression((Subtraction) exp);
		} else if (exp instanceof Parenthesis) {
			return handleParenthesisExpression((Parenthesis) exp, name);
		} else if (exp instanceof Function) {
			return handleFunctionExpression((Function) exp, name);
		} else if (exp instanceof Column) {
			return handleColumnExpression((Column) exp, name);
		}
		return "**not found**";
	}

	/**
	 * This method handles function expression.
	 *
	 * @param exp the exp
	 * @param name the name
	 * @return the string
	 */
	private String handleFunctionExpression(Function exp, String name) {
		StringBuffer str = new StringBuffer();

		List<Expression> list = exp.getParameters().getExpressions();
		for (Expression expression : list) {
			return handleExpression(expression, name);
		}
		return str.toString();
	}

	/**
	 * This method handles column expression.
	 *
	 * @param col the col
	 * @param name the name
	 * @return the string
	 */
	private String handleColumnExpression(Column col, String name) {
		StringBuffer str = new StringBuffer();

		if ("sysdate".equalsIgnoreCase(col.getColumnName())) {
			str.append(format.format(new DateTime().toDate()));
			return str.toString();
		}
		String criteria = fieldMap.get(name + "." + col.getColumnName());
		if (criteria == null) {
			criteria = col.getColumnName();
		}
		str.append(criteria);

		return str.toString();
	}

	/**
	 * This method handles order by column expression.
	 *
	 * @param col the col
	 * @param name the name
	 * @return the string
	 */
	private String handleOrderByColumnExpression(Column col, String name) {
		StringBuffer str = new StringBuffer();
		String criteria = orderByMap.get(name + "." + col.getColumnName());
		if (criteria == null) {
			criteria = col.getColumnName();
		}
		str.append(criteria);

		return str.toString();
	}

	/**
	 * This method handles parenthesis expression.
	 *
	 * @param exp the exp
	 * @param name the name
	 * @return the string
	 */
	private String handleParenthesisExpression(Parenthesis exp, String name) {
		return handleExpression(exp.getExpression(), name);
	}

	/**
	 * This method handles is null expression.
	 *
	 * @param exp the exp
	 * @param name the name
	 * @return the string
	 */
	private String handleisNullExpression(IsNullExpression exp, String name) {
		StringBuffer str = new StringBuffer();

		return str.toString();
	}

	/**
	 * This method handles and expression.
	 *
	 * @param and the and
	 * @param name the name
	 * @return the string
	 */
	public String handleAndExpression(AndExpression and, String name) {
		StringBuffer str = new StringBuffer();
		Expression exp = and.getLeftExpression();
		boolean addAnd = false;
		if (exp instanceof AndExpression) {
			String str1 = handleAndExpression((AndExpression) exp, name);
			if (!str1.isEmpty()) {
				str.append(str1);
				addAnd = true;
			}
		} else {
			String leftExpression = handleExpression(exp, name);
			str.append(leftExpression);
			if (!leftExpression.isEmpty()) {
				addAnd = true;
			}
		}
		String right = handleExpression(and.getRightExpression(), name);
		if (!right.isEmpty()) {
			if (addAnd)
				str.append(" and ");
			str.append(right);
		}

		return str.toString();
	}

	/**
	 * This method handles is null expression.
	 *
	 * @param isNull the is null
	 * @param name the name
	 * @return the string
	 */
	public String handleIsNullExpression(IsNullExpression isNull, String name) {
		StringBuffer str = new StringBuffer();

		String leftExpression = handleExpression(isNull.getLeftExpression(), name);
		if (leftExpression.contains("SKIP"))
			return "";
		str.append(leftExpression);
		str.append(" IS NULL");
		return str.toString();
	}

	/**
	 * This method handles gt expression.
	 *
	 * @param gt the gt
	 * @param name the name
	 * @return the string
	 */
	public String handleGtExpression(GreaterThan gt, String name) {
		StringBuffer str = new StringBuffer();
		String leftExpression = handleExpression(gt.getLeftExpression(), name);
		if (leftExpression.contains("SKIP"))
			return "";
		str.append(leftExpression);

		str.append(" gt ");
		str.append(handleExpression(gt.getRightExpression(), name));

		return str.toString();
	}

	/**
	 * This method handles subtraction expression.
	 *
	 * @param sub the sub
	 * @return the string
	 */
	public String handleSubtractionExpression(Subtraction sub) {
		StringBuffer str = new StringBuffer();
		Column col = (Column) sub.getLeftExpression();
		if ("sysdate".equalsIgnoreCase(col.getColumnName())) {
			DateTime time = new DateTime();
			Expression exp = sub.getRightExpression();
			if (exp instanceof LongValue) {
				LongValue value = (LongValue) sub.getRightExpression();
				time = time.minusHours((int) value.getValue() * 24);
				str.append(format.format(time.toDate()));
			} else if (exp instanceof Division) {
				LongValue left = (LongValue) ((Division) exp).getLeftExpression();

				LongValue right = (LongValue) ((Division) exp).getRightExpression();
				double d = (double) left.getValue() / (double) right.getValue() * 24;
				time = time.minusHours((int) d);
				str.append(format.format(time.toDate()));
			}
		}

		return str.toString();
	}

	/**
	 * This method Handles EQ expression.
	 *
	 * @param eq the eq
	 * @param name the name
	 * @return the string
	 */
	public String handleEQExpression(EqualsTo eq, String name) {
		StringBuffer str = new StringBuffer();
		String leftExpression = handleExpression(eq.getLeftExpression(), name);
		if (leftExpression.contains("SKIP"))
			return "";
		str.append(leftExpression);

		str.append(" eq ");

		Object objRight = eq.getRightExpression();
		// str.append(handleExpression(eq.getRightExpression()));

		if (objRight instanceof Function) {
			str.append(handleExpression((Expression) objRight, name));
		} else {
			if (eq.getRightExpression().toString().contains("0")) {
				str.append("false");
			} else if (eq.getRightExpression().toString().contains("1")) {
				str.append("true");
			} else {
				str.append(eq.getRightExpression());
			}
		}

		return str.toString();
	}

	/**
	 * This method Handles in (hql) expression.
	 *
	 * @param in the in
	 * @param name the name
	 * @return the string
	 */
	public String handleInExpression(InExpression in, String name) {
		StringBuffer str = new StringBuffer();
		String leftExpression = handleExpression(in.getLeftExpression(), name);
		if (leftExpression.contains("SKIP"))
			return "";
		str.append(leftExpression);
		Object exp = in.getRightItemsList();
		if (exp instanceof ExpressionList) {
			ExpressionList expressionList = (ExpressionList) in.getRightItemsList();
			str.append(" in ");
			List<Expression> expressions = expressionList.getExpressions();
			str.append(" [ ");
			int index = 1;
			for (Expression expression : expressions) {
				str.append(criteriaMap.get(name + "." + leftExpression + "." + expression.toString()));
				if (index++ < expressions.size())
					str.append(" , ");
			}
			str.append(" ] ");

		} else if (exp instanceof SubSelect) {

			str.append("||| ");

			SubSelect sub = (SubSelect) in.getRightItemsList();

			PlainSelect select = (PlainSelect) sub.getSelectBody();
			str.append(select.toString());
			handleExpression(select.getWhere(), name);
		}

		return str.toString();
	}

	/**
	 * This method Updates ML sku price to NG Product price.
	 *
	 * @param skuPrice the sku price
	 * @param ngProduct the ng product
	 */
	public void updatePrice(SkuPrice skuPrice, com.mozu.api.contracts.productadmin.Product ngProduct) {
		ProductPrice ngPrice = ngProduct.getPrice();
		if (ngPrice == null) {
			ngPrice = new ProductPrice();
			ngProduct.setPrice(ngPrice);
		}

		skuPrice.getOverweightPrice(); // TODO where to map

		if (skuPrice.getCurrency() != null)
			ngPrice.setIsoCurrencyCode(skuPrice.getCurrency());

		if (skuPrice.getRegularPrice() != null)
			ngPrice.setPrice(skuPrice.getRegularPrice().doubleValue());

		if (skuPrice.getSalePrice() != null)
			ngPrice.setSalePrice(skuPrice.getSalePrice().doubleValue());
	}

	/**
	 * This method Updates ML Sku Price to NG ProductVariation Price.
	 *
	 * @param skuPrice the sku price
	 * @param ngVariation the ng variation
	 */
	public void updatePrice(SkuPrice skuPrice, ProductVariation ngVariation) {
		ProductVariationFixedPrice fixedPrice = null;

		fixedPrice = ngVariation.getFixedPrice();
		if (fixedPrice == null) {
			fixedPrice = new ProductVariationFixedPrice();
			ngVariation.setFixedPrice(fixedPrice);
		}

		skuPrice.getOverweightPrice(); // TODO where to map

		if (skuPrice.getCurrency() != null)
			fixedPrice.setCurrencyCode(skuPrice.getCurrency());

		if (skuPrice.getRegularPrice() != null)
			fixedPrice.setListPrice(skuPrice.getRegularPrice().doubleValue());

		if (skuPrice.getSalePrice() != null)
			fixedPrice.setSalePrice(skuPrice.getSalePrice().doubleValue());
	}

	/**
	 * This method converts XMLgregoriancalendar to datetime.
	 *
	 * @param dateTime the date time
	 * @return the date time
	 */
	public static DateTime XMLGregorianCalendarTodateTime(XMLGregorianCalendar dateTime) {
		DateTime convertedDate = null;
		try {

			convertedDate = new DateTime(dateTime.toGregorianCalendar().getTime());

		} catch (Exception e) {
		}
		return convertedDate;
	}

	/**
	 * Decrypt fields.
	 *
	 * @param results the results
	 * @param config the config
	 */
	public abstract void decryptFields(List<CommandResult> results, ConfigModel config);

	/**
	 * This method used to Decrypt string.
	 *
	 * @param str the str
	 * @param config the config
	 * @return the string
	 */
	public String decryptString(String str, ConfigModel config) {
		try {

			FileInputStream keySec = new FileInputStream(config.getSecring());
			String decryptedText = KiboPGPUtil.decrypt(str, keySec, config.getPassphrase().toCharArray());

			return decryptedText;

		} catch (Exception e) {
			logger.error("Decryption Failed {}",e);
			
			emailService.sendFatalEmail(e.getMessage());
		
		}

		return str;
	}
}
