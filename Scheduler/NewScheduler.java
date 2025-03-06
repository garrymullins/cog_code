/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * NewScheduler.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import java.math.BigInteger;
import java.util.Calendar;

import com.cognos.developer.schemas.bibus._3.Account;
import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.AddressSMTP;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.BaseClassArrayProp;
import com.cognos.developer.schemas.bibus._3.BaseParameter;
import com.cognos.developer.schemas.bibus._3.BooleanProp;
import com.cognos.developer.schemas.bibus._3.Credential;
import com.cognos.developer.schemas.bibus._3.DateTimeProp;
import com.cognos.developer.schemas.bibus._3.DeleteOptions;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionAddressSMTPArray;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionEnum;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionMemoPart;
import com.cognos.developer.schemas.bibus._3.DeliveryOptionString;
import com.cognos.developer.schemas.bibus._3.MemoPartString;
import com.cognos.developer.schemas.bibus._3.MultilingualToken;
import com.cognos.developer.schemas.bibus._3.NmtokenProp;
import com.cognos.developer.schemas.bibus._3.Option;
import com.cognos.developer.schemas.bibus._3.OptionArrayProp;
import com.cognos.developer.schemas.bibus._3.ParameterValue;
import com.cognos.developer.schemas.bibus._3.ParameterValueArrayProp;
import com.cognos.developer.schemas.bibus._3.PositiveIntegerProp;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.ReportSaveAsEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionBoolean;
import com.cognos.developer.schemas.bibus._3.RunOptionEnum;
import com.cognos.developer.schemas.bibus._3.RunOptionSaveAs;
import com.cognos.developer.schemas.bibus._3.RunOptionString;
import com.cognos.developer.schemas.bibus._3.RunOptionStringArray;
import com.cognos.developer.schemas.bibus._3.Schedule;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.SmtpContentDispositionEnum;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.StringProp;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;
import com.cognos.developer.schemas.bibus._3.UpdateOptions;

public class NewScheduler extends BaseClass
{

	private Schedule newSchedule = new Schedule();
	NmtokenProp howOften = new NmtokenProp();

	/**
	* Set and start a schedule for a report.
	*
	*@param connection Connection to Server
	*@param reportPath Search path to a report.
	*@param outFormat CSV,HTML,PDF,HTMLFragment,XHTML,XML. Default is HTML
	*@param pageOrientation landscape or portrait. Default is portrait
	*@param paper Letter, Legal, 11x17, A3, A4, B4 JIS, B5,JIS (all pageDefinition objects). 
	*@param delivery save, saveas, print, email
	*@param printerName as "CAMID(\":\")/printer[@name='default_printer']"
	*@param saveasName
	*@param emails An AddressSMTP array of all the e-mail addresses
	*@param weekDay weekdays in small letters sunday
	*@param monthlyStartDate The start date in the month, as a String
	*@param startMonth month with small letters (i.e. june)
	*@param relativeWeekValue
	*@param relativeDayValue
	*@param runPeriod minute, hour, day if dailyMonthlyYearly is daily
	*@param runFreq every n periods
	*@param dailyMonthlyYearly ByDay, ByWeek, ByMonth, ByYear
	*@param startOnDate If start date not passed set to today/now
	*@param endOnDate If end date not passed endOnTime must be indefinite
	*@param endOnTime Parameter endOnTime cannot be null! Options: indefinite or onDate
	*/
	public String setSchedule(
		CRNConnect connection,
		BaseClassWrapper report,
		String outFormat,
		String pageOrientation,
		String paper,
		String delivery,
		String printerName,
		String saveasName,
		AddressSMTP[] emails,
		int weekDay,
		String monthlyStartDate,
		String startMonth,
		String relativeWeekValue,
		String relativeDayValue,
		String runPeriod,
		String runFreq,
		String dailyMonthlyYearly,
		Calendar startOnDate,
		Calendar endOnDate,
		String endOnTime)
		throws java.rmi.RemoteException
	{

		
		// sn_dg_prm_smpl_schedulereport_P5_start_0
		OptionArrayProp roap = new OptionArrayProp();
		String reportPath = report.getBaseClassObject().getSearchPath().getValue();

		//Set options
		roap.setValue(
			this.setSchedulerOptions(
				outFormat,
				delivery,
				reportPath,
				printerName,
				saveasName,
				pageOrientation,
				paper,
				emails,
				connection));
		// sn_dg_prm_smpl_schedulereport_P5_end_0

		BooleanProp isActive = new BooleanProp();
		isActive.setValue(true);
		
		setFrequency(runFreq);
		if (dailyMonthlyYearly.trim() == "ByDay")
		{
			setFrequencyByDay(runPeriod, runFreq);
		}
		else if (dailyMonthlyYearly.trim() == "ByWeek")
		{
			setFrequencyByWeek(weekDay);
		}
		else if (dailyMonthlyYearly.trim() == "ByMonth")
		{
			if (monthlyStartDate != null)
			{
				setFrequencyByAbsoluteMonth(monthlyStartDate);
			}
			else
			{
				setFrequencyByRelativeMonth(
					relativeWeekValue,
					relativeDayValue);
			}
		}
		else if (dailyMonthlyYearly.trim() == "ByYear")
		{
			if (monthlyStartDate != null)
			{
				setFrequencyByAbsoluteYear(monthlyStartDate, startMonth);
			}
			else
			{
				setFrequencyByRelativeYear(
					relativeWeekValue,
					relativeDayValue,
					startMonth);
			}
		}
		else
		{
			System.out.println("Period is not correct.");
		}

		//Assign current user as the owner
		// sn_dg_prm_smpl_schedulereport_P4_start_0
		BaseClass[] owners = new BaseClass[] { Logon.getLogonAccount(connection) };
		BaseClassArrayProp ownersProp = new BaseClassArrayProp();
		ownersProp.setValue(owners);
		// sn_dg_prm_smpl_schedulereport_P4_end_0

		//
		//Make sure the account that is currently logged in has a credential,
		//then get a reference to the account credential
		// sn_dg_prm_smpl_schedulereport_P1_start_0
		if (! Credentials.hasCredential(connection))
		{
			Credentials newCred = new Credentials();
			newCred.addCredential(connection);
		}
		Account logonInfo = Logon.getLogonAccount(connection);
		Credential credential = new Credential();
		StringProp credentialPath = new StringProp();
		String credentialPathString = logonInfo.getSearchPath().getValue();
		credentialPathString = credentialPathString + "/credential[@name='Credential']";
		credentialPath.setValue(credentialPathString);
		credential.setSearchPath(credentialPath);
		BaseClassArrayProp credentials = new BaseClassArrayProp();
		credentials.setValue(new BaseClass[] {credential} );
		// sn_dg_prm_smpl_schedulereport_P1_end_0

		//
		//Set parameter values required for the report, if any
		ParameterValueArrayProp pv = new ParameterValueArrayProp();
		ParameterValue reportParameters[] = new ParameterValue[] {};
		ReportParameters repParms = new ReportParameters();
		BaseParameter[] prm = repParms.getReportParameters(
			report,
			connection);
		if (prm != null && prm.length > 0)
		{
			reportParameters =
				ReportParameters.setReportParameters(prm);
		}
		pv.setValue(reportParameters);

		//Set the remaining properties of the new Schedule object
		// sn_dg_prm_smpl_schedulereport_P6_start_0
		newSchedule.setActive(isActive);
		newSchedule.setCredential(credentials);
		newSchedule.setEndDate(this.setScheduleEndDate(endOnDate, endOnTime));
		newSchedule.setEndType(this.setScheduleEndTime(endOnTime));
		newSchedule.setOwner(ownersProp);
		newSchedule.setParameters(pv);
		newSchedule.setOptions(roap);
		newSchedule.setStartDate(this.setScheduleStartDate(startOnDate));
		newSchedule.setType(howOften);
		
		//  add the schedule to the report
		AddOptions ao = new AddOptions();
		ao.setUpdateAction(UpdateActionEnum.replace);
		BaseClass newBc = connection.getCMService().add(
			new SearchPathSingleObject(reportPath),
			new BaseClass[] { newSchedule },
			ao)[0];
		// sn_dg_prm_smpl_schedulereport_P6_end_0

		if (newBc != null)
		{
			return ("Schedule Created");
		}
		else
		{
			return ("Schedule NOT Created");
		}

	}

	public void setFrequency(String runFreq)
	{
		PositiveIntegerProp freq = new PositiveIntegerProp();
		freq.setValue(new BigInteger(runFreq));
		newSchedule.setEveryNPeriods(freq);
	}

	public void setFrequencyByDay(String runPeriod, String runFreq)
	{
		howOften.setValue("daily");
		
		NmtokenProp period = new NmtokenProp();
		period.setValue(runPeriod);

		PositiveIntegerProp runFreqProp = new PositiveIntegerProp();
		runFreqProp.setValue(new BigInteger(runFreq));
		
		newSchedule.setDailyPeriod(period);
		newSchedule.setEveryNPeriods(runFreqProp);
	}

	public void setFrequencyByWeek(int weekDay)
	{
		howOften.setValue("weekly");
		BooleanProp dayOfTheWeek = new BooleanProp();
		dayOfTheWeek.setValue(true);
		//This will set only one day of the week
		//If you want more than one, use if statement
		switch (weekDay)
		{
			case 1 :
				newSchedule.setWeeklySunday(dayOfTheWeek);
				break;
			case 2 :
				newSchedule.setWeeklyMonday(dayOfTheWeek);
				break;
			case 3 :
				newSchedule.setWeeklyTuesday(dayOfTheWeek);
				break;
			case 4 :
				newSchedule.setWeeklyWednesday(dayOfTheWeek);
				break;
			case 5 :
				newSchedule.setWeeklyThursday(dayOfTheWeek);
				break;
			case 6 :
				newSchedule.setWeeklyFriday(dayOfTheWeek);
				break;
			case 7 :
				newSchedule.setWeeklySaturday(dayOfTheWeek);
				break;
		}
	}

	public void setFrequencyByAbsoluteMonth(String monthlyStartDate)
	{
		howOften.setValue("monthlyAbsolute");
		this.m_setMonthlyAbsoluteDay(monthlyStartDate);
	}

	public void setFrequencyByRelativeMonth(
		String relativeWeekValue,
		String relativeDayValue)
	{
		howOften.setValue("monthlyRelative");
		this.m_setMonthlyRelativeDay(relativeDayValue);
		this.m_setMonthlyRelativeWeek(relativeWeekValue);
	}

	public void setFrequencyByAbsoluteYear(
		String monthlyStartDate,
		String startMonth)
	{
		howOften.setValue("yearlyAbsolute");
		this.m_setYearlyAbsoluteMonth(startMonth);
		this.m_setYearlyAbsoluteDay(monthlyStartDate);
	}

	public void setFrequencyByRelativeYear(
		String relativeWeekValue,
		String relativeDayValue,
		String startMonth)
	{
		howOften.setValue("yearlyRelative");
		this.m_setYearlyRelativeDay(relativeDayValue);
		this.m_setYearlyRelativeWeek(relativeWeekValue);
		this.m_setYearlyRelativeMonth(startMonth);
	}

	public Option[] setSchedulerOptions(
		String outFormat,
		String delivery,
		String reportPath,
		String printerName,
		String saveasName,
		String pageOrientation,
		String paper,
		AddressSMTP[] emails,
		CRNConnect connection)
	{
		//Declare run options

		RunOptionStringArray format = new RunOptionStringArray();
		RunOptionBoolean prompt = new RunOptionBoolean();

		int numberRunOptions = 3; //default run options
		if (outFormat.compareToIgnoreCase("PDF") == 0)
		{
			//one more each for orientation and paper size
			numberRunOptions = numberRunOptions + 2;
		}

		if (delivery.compareToIgnoreCase("email") == 0)
		{
			//one more each for e-mail addresses, subject and body
			numberRunOptions = numberRunOptions + 4;
		}
		else if (delivery.compareToIgnoreCase("print") == 0)
		{
			//this one is for the printer
			numberRunOptions++;
		}

		Option ro[] = new Option[numberRunOptions];

		//Set option to replace any existing output
		//Don't prompt for values
		prompt.setName(RunOptionEnum.prompt);
		prompt.setValue(false);

		ro[--numberRunOptions] = prompt;

		if (delivery.compareToIgnoreCase("saveas") == 0)
		{
			//If saveAs name not provided a default will be used
			ro[--numberRunOptions] =
				setDeliveryMethodSaveAs(connection, saveasName, reportPath);
		}
		else if (delivery.compareToIgnoreCase("save") == 0)
		{
			ro[--numberRunOptions] = setDeliveryMethodSave();
		}
		else if (delivery.compareToIgnoreCase("print") == 0)
		{
			ro[--numberRunOptions] = setDeliveryMethodPrint();
			ro[--numberRunOptions] = setPrinter(printerName);
		}
		else if (delivery.compareToIgnoreCase("email") == 0)
		{
			ro[--numberRunOptions] = setDeliveryMethodEmail();
			ro[--numberRunOptions] = setEmailAddresses(emails);
			ro[--numberRunOptions] = setEmailBody();
			ro[--numberRunOptions] = setEmailSubject();
			ro[--numberRunOptions] = setEmailAttach();
		}

		//set the output format to outFormat
		format.setName(RunOptionEnum.outputFormat);
		format.setValue(new String[] { outFormat });
		ro[--numberRunOptions] = format;

		//If outFormat is PDF set page orientation and paper size
		if (outFormat.compareToIgnoreCase("PDF") == 0)
		{
			ro[--numberRunOptions] = setOrientation(pageOrientation);
			ro[--numberRunOptions] = setPaperSize(paper);
		}

		return ro;
	}

	//	public NmtokenProp m_setMonthlyRelativeDay(String relativeDayValue)
	public void m_setMonthlyRelativeDay(String relativeDayValue)
	{
		NmtokenProp relativeDay = new NmtokenProp();
		relativeDay.setValue(relativeDayValue); //"sunday"
		newSchedule.setMonthlyRelativeDay(relativeDay);
	}

	public void m_setMonthlyRelativeWeek(String relativeWeekValue)
	{
		NmtokenProp relativeWeek = new NmtokenProp();
		relativeWeek.setValue(relativeWeekValue);
		newSchedule.setMonthlyRelativeWeek(relativeWeek);
	}

	public void m_setMonthlyAbsoluteDay(String monthlyStartDate)
	{
		PositiveIntegerProp absoluteDay = new PositiveIntegerProp();
		absoluteDay.setValue(new BigInteger(monthlyStartDate));
		//This method should be called ONLY for "ByMonth" 
		newSchedule.setMonthlyAbsoluteDay(absoluteDay);
	}

	public void m_setYearlyAbsoluteDay(String monthlyStartDate)
	{
		PositiveIntegerProp absoluteDay = new PositiveIntegerProp();
		absoluteDay.setValue(new BigInteger(monthlyStartDate));
		//This method should be called ONLY for "ByYear"
		newSchedule.setYearlyAbsoluteDay(absoluteDay);
	}

	public void m_setYearlyAbsoluteMonth(String startMonth)
	{
		NmtokenProp absoluteMonth = new NmtokenProp();
		absoluteMonth.setValue(startMonth.toLowerCase().trim());
		newSchedule.setYearlyAbsoluteMonth(absoluteMonth);
	}

	public void m_setYearlyRelativeMonth(String startMonth)
	{
		NmtokenProp relativeMonth = new NmtokenProp();
		relativeMonth.setValue(startMonth.toLowerCase().trim());
		newSchedule.setYearlyRelativeMonth(relativeMonth);
	}

	public void m_setYearlyRelativeDay(String relativeDayValue)
	{
		NmtokenProp relativeDay = new NmtokenProp();
		relativeDay.setValue(relativeDayValue); //"sunday"
		newSchedule.setYearlyRelativeDay(relativeDay);
	}

	public void m_setYearlyRelativeWeek(String relativeWeekValue)
	{
		NmtokenProp relativeWeek = new NmtokenProp();
		relativeWeek.setValue(relativeWeekValue); //"first"
		newSchedule.setYearlyRelativeWeek(relativeWeek);
	}

	// sn_dg_prm_smpl_schedulereport_P2_start_0
	public DateTimeProp setScheduleStartDate(Calendar startOnDate)
	{
		DateTimeProp startDate = new DateTimeProp();
		//If start date not passed set to today/now
		if (startOnDate != null)
			startDate.setValue(startOnDate);
		else
			startDate.setValue(Calendar.getInstance());

		return startDate;
	}
	// sn_dg_prm_smpl_schedulereport_P2_end_0

	// sn_dg_prm_smpl_schedulereport_P3_start_0
	public DateTimeProp setScheduleEndDate(
		Calendar endOnDate,
		String endOnTime)
	{
		DateTimeProp endDate = new DateTimeProp();
		if (endOnTime.compareToIgnoreCase("onDate") == 0)
			if (endOnDate != null)
			{
				endDate.setValue(endOnDate);
			}
			else
				System.out.println(
					"Parameter endOnTime cannot be onDate if no enddate provided");
		return endDate;
	}

	public NmtokenProp setScheduleEndTime(String endOnTime)
	{
		NmtokenProp endTime = new NmtokenProp();
		if (endOnTime != null)
		{
			endTime.setValue(endOnTime);
		}
		else
			System.out.println(
				"Parameter endOnTime cannot be null! Options: indefinite or onDate");
		return endTime;
	}
	// sn_dg_prm_smpl_schedulereport_P3_end_0

	public RunOptionBoolean setDeliveryMethodPrint()
	{
		RunOptionBoolean printOption = new RunOptionBoolean();

		printOption.setName(RunOptionEnum.print);
		//?saveOutput, e-mail,print,burst?
		printOption.setValue(true);

		return printOption;
	}

	public RunOptionBoolean setDeliveryMethodSave()
	{
		RunOptionBoolean saveOutput = new RunOptionBoolean();

		saveOutput.setName(RunOptionEnum.saveOutput);
		saveOutput.setValue(true);

		return saveOutput;
	}

	public DeliveryOptionAddressSMTPArray setEmailAddresses(AddressSMTP[] emails)
	{
		//The emails can also be taken from CM as group, user, etc.(see Email class)
		DeliveryOptionAddressSMTPArray emailAddress = new DeliveryOptionAddressSMTPArray();

		emailAddress.setName(DeliveryOptionEnum.toAddress);
		emailAddress.setValue(emails);

		return emailAddress;
	}

	public DeliveryOptionMemoPart setEmailBody()
	{
		DeliveryOptionMemoPart bodyText = new DeliveryOptionMemoPart();
		MemoPartString memoText = new MemoPartString();
		
		memoText.setName("Body");
		memoText.setText("This is a scheduled report");
		memoText.setContentDisposition(SmtpContentDispositionEnum.inline);

		bodyText.setName(DeliveryOptionEnum.memoPart);
		bodyText.setValue(memoText);
		return bodyText;
	}

	public DeliveryOptionString setEmailSubject()
	{
		DeliveryOptionString subjectText = new DeliveryOptionString();
		subjectText.setName(DeliveryOptionEnum.subject);
		subjectText.setValue("Sample Scheduled Report");
		return subjectText;
	}

	public RunOptionBoolean setEmailAttach()
	{
		RunOptionBoolean attach = new RunOptionBoolean();
		attach.setName(RunOptionEnum.emailAsAttachment);
		attach.setValue(true);
		return attach;
	}

	public RunOptionBoolean setDeliveryMethodEmail()
	{
		RunOptionBoolean sendEmail = new RunOptionBoolean();
		sendEmail.setName(RunOptionEnum.email);
		sendEmail.setValue(true);

		return sendEmail;
	}

	public RunOptionString setPrinter(String printerName)
	{
		RunOptionString printerOption = new RunOptionString();

		//if ptinterOption not set, it will print to the default one
		printerOption.setName(RunOptionEnum.printer);
		printerOption.setValue(printerName);

		return printerOption;
	}

	public RunOptionString setOrientation(String pageOrientation)
	{
		RunOptionString orientation = new RunOptionString();

		orientation.setName(RunOptionEnum.outputPageOrientation);
		orientation.setValue(pageOrientation);
		return orientation;
	}

	public RunOptionString setPaperSize(String paper)
	{
		String quotChar = "\'";
		if (paper.indexOf(quotChar) >= 0)
		{
			quotChar = "\"";
		}
		RunOptionString paperSize = new RunOptionString();

		paperSize.setName(RunOptionEnum.outputPageDefinition);
		paperSize.setValue(
			"/configuration/pageDefinition[@name="
				+ quotChar
				+ paper
				+ quotChar
				+ "]");
		return paperSize;
	}

	public Option setDeliveryMethodSaveAs(
		CRNConnect connection,
		String saveasName,
		String reportPath)
	{
		CSHandlers csh = new CSHandlers();
		// sn_dg_prm_smpl_schedulereport_P5_start_1
		RunOptionSaveAs saveAs = new RunOptionSaveAs();
		MultilingualToken[] obj = new MultilingualToken[1];

		//Set the name of the reportView
		obj[0] = new MultilingualToken();
		obj[0].setLocale("en-us");

		//If no name provided use default
		if (saveasName != null)
		{
			obj[0].setValue(saveasName);
		}
		else
		{
			obj[0].setValue("View of Report " + reportPath);
		}

		//Save the object as report view with name saveasName
		saveAs.setName(RunOptionEnum.saveAs);
		saveAs.setObjectClass(ReportSaveAsEnum.reportView);
		saveAs.setObjectName(obj);
		saveAs.setParentSearchPath(csh.getParentPath(connection, reportPath));
		// sn_dg_prm_smpl_schedulereport_P5_end_1

		//add saveAs option to RunOptions
		return saveAs;
	}

	/**
	* Return 1 if the report has a schedule and get the schedule .
	*
	*@param connection Connection to Server
	*@param path Search path to a report.
	*/
	public int getSchedule(CRNConnect connection, String path)
	{
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.displaySequence,
				PropEnum.weeklyWednesday,
				PropEnum.everyNPeriods };
		
		String schedulePath = path + "/schedule";
		try
		{
			connection.getCMService().query(
				new SearchPathMultipleObject(schedulePath),
				props,
				new Sort[] {},
				new QueryOptions());
			return 1;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return 0;
		}
	}

	public void setSchedule(Schedule sch)
	{
		newSchedule = sch;
	}
	public BaseClass getSchedule()
	{
		return newSchedule;
	}

	public void setScheduleSearchPath(String path)
	{
		StringProp schPath = new StringProp();
		schPath.setValue(path);
		newSchedule.setSearchPath(schPath);
	}

	/**
	* If schedule exists delete it from the Content store.
	*
	*@param connection Connection to Server
	*@param reportPath Search path to a report.
	*/
	public void deleteSchedule(CRNConnect connection, String path)
	{
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };
		try
		{
			//this.getSchedule(connect, path);
			this.getSchedule();
			//Define delete options							  
			DeleteOptions del = new DeleteOptions();
			del.setForce(true);

			String schedulePath = path + "/schedule";
			BaseClass bc[] =
				connection.getCMService().query(
					new SearchPathMultipleObject(schedulePath),
					props,
					new Sort[] {},
					new QueryOptions());

			if (bc != null)
			{
				if (bc.length > 0)
				{
					int i = connection.getCMService().delete(bc, del);
					if (i > 0)
					{
						System.out.println(
							"The schedule was deleted successfully");
					}
					else
						System.out.println(
							"Error occurred while deleting the schedule");
				}
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	* Searches the content store for the schedule path
	* and returns a Schedule object
	*@param connection 	Connection to Server
	*@param path 		Search path to a report.
	*/

	public Schedule getScheduleFromCM(
		CRNConnect connection,
		String path)
	{
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.options};
		try
		{
			BaseClass[] bc =
				connection.getCMService().query(
					new SearchPathMultipleObject(path),
					props,
					new Sort[] {},
					new QueryOptions());
			if (bc.length > 0)
			{
				return (Schedule)bc[0];
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return null;
	}

	/** If the report has a schedule, disable it.
	*
	*@param connection 	Connection to Server
	*@param path 		Search path to a report.
	*/

	public int updateSchedule(CRNConnect connection, String path)
	{
		PropEnum props[] =
			new PropEnum[] {
				PropEnum.searchPath,
				PropEnum.defaultName,
				PropEnum.options };
		try
		{
			BaseClass[] bc =
				connection.getCMService().query(
					new SearchPathMultipleObject(path),
					props, new Sort[] {},
					new QueryOptions());
			if (bc.length > 0)
			{
				newSchedule = (Schedule)bc[0];
				connection.getCMService().update(bc, new UpdateOptions());
			}
			return 1;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return 0;
		}
	}

	/**
	* If the report has a schedule, disable it.
	*
	*@param connection 	Connection to Server
	*@param path 		Search path to a report.
	*/
	public int disableSchedule(CRNConnect connection, String path)
	{
		PropEnum props[] =
			new PropEnum[] { PropEnum.searchPath, PropEnum.defaultName };

		try
		{
			Schedule sch = new Schedule();
			if (this.getSchedule(connection, path) != 0)
			{
				BooleanProp isActive = new BooleanProp();
				if (path.indexOf("/schedule") <= 0)
					path += "/schedule";
				BaseClass[] bc =
				connection.getCMService().query(
					new SearchPathMultipleObject(path), 
					props, 
					new Sort[] {}, 
					new QueryOptions());
				if (bc.length > 0)
				{
					sch = (Schedule)bc[0];
					isActive.setValue(false);
					sch.setActive(isActive);

					connection.getCMService().update(bc, new UpdateOptions());
				}

			}
			return 1;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return 0;
		}
	}
}
