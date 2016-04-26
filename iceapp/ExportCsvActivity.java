package com.iceapp;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ExportCsvActivity extends Activity {
	private Button exportlocalButton;
	private SQLiteDatabase db;
	private String defaultRoute;
	private String defaultRouteLower;
	private String ftp_server_ip;
	private String ftp_folder;
	private String csv_local_folder;
	private String upload_folder;
	private String download_folder;
	private String appFolder;
	private String exportFolder;
	private File deleteDir;
	private File exportDir;
	private String subfolder1;
	private String subfolder2;
	private String csv_write_dir;
	private String replenishCsv;
	private String salesOrderCsv;
	private String deletedSalesOrderCsv;
	private String visitInfoCsv;
	private String advSalesOrderCsv;
	private String currentDate;
	private String currentDate_ddMMyyyy;
	private DatabaseHelper helper;
	private IceUtil util;
	private ProgressDialog exportDialog;
	private EditText mEdit;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export_csv);
		util = new IceUtil();
		helper = new DatabaseHelper(this);
		db = helper.getWritableDatabase();
		
		// Edittext for user to enter the import file directory
		mEdit = (EditText) findViewById(R.id.input_edittext); 
		
		// Button for opening file browser
		Button browseButton = (Button) findViewById(R.id.input_button);
		browseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExportCsvActivity.this,FileBrowser.class);
				startActivityForResult(intent, 1);
			}
		});
		// Download all csv files from pda server(require local wifi login )
		exportlocalButton = (Button) findViewById(R.id.button_local);
		exportlocalButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				exportCsv();
				File[] csvfile = exportDir.listFiles();
				ExportCsvTask task = new ExportCsvTask();
				task.execute(csvfile);
				setProgressDialog();
				
			}
						
		});

	}
	
	/*
	 * Generate Csv file from pdadata table
	 */
	
	protected void exportCsv() {
		Cursor basicDataCur;
		basicDataCur = db.rawQuery(
				"SELECT route as _id,ftp_server_ip,ftp_folder,csv_local_folder,upload_folder,download_folder FROM pda WHERE default_route =?",
				new String[] { "Y" });
		
		if (basicDataCur.moveToFirst()) {

			// Setup variables from getting pda table data
			defaultRoute = basicDataCur.getString(basicDataCur.getColumnIndex("_id"));
			ftp_server_ip = basicDataCur.getString(basicDataCur.getColumnIndex("ftp_server_ip"));
			ftp_folder = basicDataCur.getString(basicDataCur.getColumnIndex("ftp_folder"));
			csv_local_folder = basicDataCur.getString(basicDataCur.getColumnIndex("csv_local_folder"));
			upload_folder = basicDataCur.getString(basicDataCur.getColumnIndex("upload_folder"));
			download_folder = basicDataCur.getString(basicDataCur.getColumnIndex("download_folder"));
				
			//Set up directory for the CSV should be written
			subfolder1=csv_local_folder.substring(0,8);
			exportFolder = subfolder1+"EXPORT/";
			csv_write_dir = subfolder1+"EXPORT/"+defaultRoute+"/";	
			deleteDir = new File(Environment.getExternalStorageDirectory().getPath(),exportFolder);
			Log.d("exportCsv","subfolder1 = "+subfolder1);
			Log.d("exportCsv","subfolder2 = "+subfolder2);
			Log.d("exportCsv","csv_write_dir = "+csv_write_dir);
			
			if (util.EditTextisEmpty(mEdit)){
				// EditText is empty, export file through default setting
				exportDir = new File(Environment.getExternalStorageDirectory().getPath(),csv_write_dir);
			}else {
				// EditText has content, path is selected
				exportDir = new File(mEdit.getText().toString());
			}
			
			Log.d("exportCsv","exportDir = "+exportDir);
			//delete existing file
			util.DeleteRecursive(exportDir);
			Log.d("exportCsv","return value of exportDir.exists()="+String.valueOf(exportDir.exists()));
			exportDir.mkdirs();
			
		}		
		basicDataCur.close();
		exportGetCurDate();
		Log.d("exportCsv","exportGetCurDate() finish" );
		Log.d("exportCsv","exportCsv() finish" );
		
	}
	
	/*
	 * Generate Csv file from item_file table
	 */
	protected void replenish() throws SQLException{
		
		//Replenish
		//Write csv with specified name
		Cursor replenishCur = db.rawQuery(
			"SELECT '" + defaultRoute + "' AS route, '" +
			currentDate_ddMMyyyy + "' AS req_date, " +
            " a.item_code,"+ 
            " b.opening_qty  AS open_stock,"+
            " b.sales_qty AS sales_qty,"+
            " c.qty AS req_qty,"+
            " c.qty AS actual_qty"+
            " FROM item_file a LEFT OUTER JOIN truck_open b ON a.item_code = b.item_code"+  	
            " AND '" + defaultRoute + "' = b.route " +
            " AND b.opening_qty IS NOT NULL " +
            " AND b.opening_qty > 0 " +
            " LEFT OUTER JOIN replenish c " +
            " ON a.item_code = c.item_code " +
            " AND c.route = ? " +				//c.route = default_route
            " AND c.replenish_date = ? " +    	//Replenish_date = system date
            " AND c.qty IS NOT NULL " +
            " AND c.qty > 0 " +
            " ORDER BY a.item_code", new String[] {defaultRoute,util.getCurDate()}
		);
		
		Log.d("replenish","replenishCur set");
		
		
		File exportCSV = new File(exportDir, replenishCsv);
		try {
			exportCSV.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (replenishCur.moveToFirst()) {
			try{
				
				//CSVWriter csvWrite = new CSVWriter(new FileWriter(exportCSV),'\t',CSVWriter.NO_QUOTE_CHARACTER);
				CSVWriter csvWrite = new CSVWriter(new FileWriter(exportCSV),',',CSVWriter.NO_QUOTE_CHARACTER);			
	            //  int c = curCSV.getColumnCount();
	            //csvWrite.writeNext(replenishCur.getColumnNames());
	            
	            int loopCounter = replenishCur.getCount();
	            for (int i=0;i<loopCounter;i++)  {
	               	String column0; 
	               	String column1;
	               	String column2;
	               	int column3;
	               	int column4;
	               	int column5;
	               	int column6;
	               	
	               	column0 = replenishCur.getString(0); 
	               	column1 = replenishCur.getString(1);
	               	column2 = replenishCur.getString(2);
	               	//if open_stock is null, set it to 0
	               	if (replenishCur.isNull(3)){
	               		column3 = 0;
	               	}else{
	               		column3 = replenishCur.getInt(3);
	               	}
	               	//if sales_qty is null, set it to 0
	               	if (replenishCur.isNull(4)){
	               		column4 = 0;
	               	}else{
	               		column4 = replenishCur.getInt(4);
	               	}
	               	//if req_qty is null, set it to 0
	               	if (replenishCur.isNull(5)){
	               		column5 = 0;
	               	}else{
	               		column5 = replenishCur.getInt(5);
	               	}
	               	//if actual_qty is null, set it to 0
	               	if (replenishCur.isNull(6)){
	               		column6 = 0;
	               	}else{
	               		column6 = replenishCur.getInt(6);
	               	}
	               	
	               	if (!(column3 == 0 && column4 == 0 && column5 == 0)) {	               		
		               	String arrStr[] = { 
			               		column0,
			               		column1,
			               		column2,
			               		Double.toString(column3),
			               		Double.toString(column4),
			               		Double.toString(column5),
			               		Double.toString(column6) + Character.toString((char)13),
			               	};
			               	//Write column data into csv
			                csvWrite.writeNext(arrStr);
	               	}
	                replenishCur.moveToNext();
	            }
	            csvWrite.close();
	            replenishCur.close();
	            
			}catch(SQLException sqlEx){
				Log.e("replenish", sqlEx.getMessage(), sqlEx);
			}catch (IOException IOe) {
				Log.e("replenish", IOe.getMessage(), IOe);
			}
		}
		Log.d("replenish", "Replenish() done");
	}
	
	/*
	 * Get data from Sales_Order_Hdr , Customer_File table where Orderstatus =/= 'D'
	 * Then generate Csv file from Sales_Order_Dtl, Item_File table
	 */
	protected void salesOrder() throws SQLException{
		
		Cursor salesOrderCur = db.rawQuery(
			"SELECT a.Order_No as id"+
	            ", a.Order_Date" +
	            ", a.Cust_Code" +
	            ", a.Customer_Ref_No" +
	            ", b.Pay_Term" +
	            ", b.Price_Book_Code" +
	            ", c.line_no"+
           		", c.item_code"+
           		", d.unit_code"+
           		", c.tran_code"+
           		", c.order_qty"+
           		", c.unit_price"+
	        " FROM Sales_Order_Hdr a, Customer_File b, Sales_Order_Dtl c, Item_File d" +
	        " WHERE a.Cust_Code = b.Cust_Code" +
	        " AND c.item_code = d.item_code "+
	        " AND a.Order_status <> 'D'" +
	        " AND a.Order_Date = ?"+
	        " AND c.order_no = a.order_no" +
	        " ORDER BY a.order_no, c.line_no",new String[]{util.getCurDate()}
		);
		
		Log.d("salesOrder","salesOrderCur1 set");
		
		File exportCSV = new File(exportDir, salesOrderCsv);
		Log.d("salesOrder","export salesOrderCsv = "+exportCSV);
		Log.d("salesOrder","salesOrderCur.getCount() = "+Integer.toString(salesOrderCur.getCount()));
		Log.d("salesOrder","salesOrderCsv column = "+Arrays.toString(salesOrderCur.getColumnNames()));
		
		try {
			exportCSV.createNewFile();
			Log.d("salesOrder","salesOrder csv created");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (salesOrderCur.moveToFirst()) {
			
			try{
				CSVWriter csvWrite = new CSVWriter(new FileWriter(exportCSV),',',CSVWriter.NO_QUOTE_CHARACTER);
				String[] salesOrderCol = {	//write column of salesOrderCsv
					"default_route",
					"order_no",
					"order_date",
					"cust_code",
					"customer_ref_no",
					"pay_term",
					"price_bk_code",
					"item_code",
					"unit_code",
					"tran_code",
					"unit_price",
					"order_qty"};
	            //csvWrite.writeNext(salesOrderCol);
	            
	            int loopCounter = salesOrderCur.getCount();
	            for (int i=0;i<loopCounter;i++){
	            	String mOrder_No = salesOrderCur.getString(0);
	               	String mOrder_Date = salesOrderCur.getString(1);
	               	String mCust_Code = salesOrderCur.getString(2);
	               	String mCustomer_Ref_No = salesOrderCur.getString(3);
	               	String mPay_Term = salesOrderCur.getString(4);
	               	String mPrice_BK_Code = salesOrderCur.getString(5);
                    String mItem_Code = salesOrderCur.getString(7);
                    String mUnit_Code = salesOrderCur.getString(8);
                    String mTran_Code = salesOrderCur.getString(9);
                    Double mOrder_Qty = salesOrderCur.getDouble(10);
                    Double mUnit_Price = salesOrderCur.getDouble(11);
                    mOrder_Date = mOrder_Date.substring(6,8) + "-" +
                                  mOrder_Date.substring(4,6) + "-" +
                    		      mOrder_Date.substring(0,4);
                	
                    Log.d("salesOrder","cur data get");
                    
                    String arrStr[] = { 
                    	defaultRoute,
                    	mOrder_No,
                    	mOrder_Date,
                    	mCust_Code,
                    	mCustomer_Ref_No,
                    	mPay_Term,
                    	mPrice_BK_Code,
                    	mItem_Code,
                    	mUnit_Code,
                    	mTran_Code,
                    	String.format("%.4f",mUnit_Price),
                    	String.format("%.4f",mOrder_Qty)+ Character.toString((char)13)
	               	};
                    
                    Log.d("salesOrder","mUnit_Price = "+Double.toString(mUnit_Price));
                    
                    Log.d("salesOrder","salesOrderCsv data = "+Arrays.toString(arrStr));
                    
                	//Write column data into csv
	                csvWrite.writeNext(arrStr);
	                salesOrderCur.moveToNext();
                    
	            }
	            
	            csvWrite.close();
	            salesOrderCur.close();
	            
			}catch(SQLException sqlEx){
				Log.e("salesOrder", sqlEx.getMessage(), sqlEx);
			}catch (IOException IOe) {
				Log.e("salesOrder", IOe.getMessage(), IOe);
			}
			Log.d("salesOrder","salesorder csv finished");
		}
		Log.d("salesOrder","salesOrder() done");
		
	}
	
	/*
	 * Get data from Sales_Order_Hdr , Customer_File table where Orderstatus = 'D'
	 * Then generate Csv file from Sales_Order_Dtl, Item_File table
	 */
	protected void deletedSalesOrder() throws SQLException {
		Cursor deletedSalesOrderCur = db.rawQuery(
			"SELECT a.Order_No"+
			", a.Order_Date" +
			", a.Cust_Code" +
			", a.Customer_Ref_No" +
			", a.Create_System_Date" +
			", a.Last_Mod_System_Date" +
			", c.line_no"+
			", c.item_code"+
			", c.tran_code"+
       		", c.unit_price"+
       		", c.order_qty"+
			" FROM Sales_Order_Hdr a, Customer_File b, Sales_Order_Dtl c, Item_File d" +
			" WHERE a.Cust_Code = b.Cust_Code" +
			" AND c.item_code = d.item_code" +
			" AND a.Order_status == 'D'" +
			" AND a.Order_Date = ?"+
			" AND c.order_no = a.order_no" +
			" ORDER BY a.order_no, c.line_no",
			new String[]{util.getCurDate()}
		);

		Log.d("deletedsalesorder","salesOrderCur set");
			
		File exportCSV = new File(exportDir, deletedSalesOrderCsv);
		Log.d("sadeletedsalesorderlesOrder","export deletedSalesOrderCsv = "+exportCSV);
		Log.d("deletedsalesorder","deletedSalesOrderCur.getCount() = "+Integer.toString(deletedSalesOrderCur.getCount()));
		Log.d("deletedsalesorder","deletedSalesOrderCsv column = "+Arrays.toString(deletedSalesOrderCur.getColumnNames()));
			
		try {
			exportCSV.createNewFile();
			Log.d("deletedsalesorder","deletedSalesOrder csv created");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (deletedSalesOrderCur.moveToFirst()) {
				
			try{
				CSVWriter csvWrite = new CSVWriter(new FileWriter(exportCSV),',',CSVWriter.NO_QUOTE_CHARACTER);
		        //  int c = curCSV.getColumnCount();
				String[] curCol = {
					"order_no",
					"order_date",
					"cust_code",
					"customer_ref_no",
					"create_system_date",
					"last_mod_system_date",
					"line_no",
					"item_code",
					"tran_code",
					"unit_price",
					"order_qty"
				};				
				//csvWrite.writeNext(curCol);
				//Log.d("deletedsalesorder","Write column finish");
				int loopCounter = deletedSalesOrderCur.getCount();	
				
		        for(int i=0;i<loopCounter;i++){
		            String mOrder_No = deletedSalesOrderCur.getString(0);
		            String mOrder_Date = deletedSalesOrderCur.getString(1);
		            String mCust_Code = deletedSalesOrderCur.getString(2);
		            String mCustomer_Ref_No = deletedSalesOrderCur.getString(3);
		            String mCreate_System_Date = deletedSalesOrderCur.getString(4);
		            String mLast_Mod_System_Date = deletedSalesOrderCur.getString(5);
		            int mLine_no = deletedSalesOrderCur.getInt(6);
		            String mItem_Code= deletedSalesOrderCur.getString(7);
		            String mTran_Code = deletedSalesOrderCur.getString(8);
		            Float mUnit_Price = deletedSalesOrderCur.getFloat(9);
		            Float mOrder_Qty = deletedSalesOrderCur.getFloat(10);
                    mOrder_Date = mOrder_Date.substring(6,8) + "-" +
                                  mOrder_Date.substring(4,6) + "-" +
              		              mOrder_Date.substring(0,4);
                    mCreate_System_Date = mCreate_System_Date.substring(6,8) + "-" +
                    		              mCreate_System_Date.substring(4, 6) + "-" +
                    		              mCreate_System_Date.substring(0,4) + " " +
                    		              mCreate_System_Date.substring(8,10) + ":" +
                    		              mCreate_System_Date.substring(10,12) + ":" +
                    		              mCreate_System_Date.substring(12,14);
                    mLast_Mod_System_Date = mLast_Mod_System_Date.substring(6,8) + "-" +
                    		                mLast_Mod_System_Date.substring(4,6) + "-" +
                    		                mLast_Mod_System_Date.substring(0,4) + " " +
                    		                mLast_Mod_System_Date.substring(8,10) + ":" +
                    		                mLast_Mod_System_Date.substring(10,12) + ":" +
                    		                mLast_Mod_System_Date.substring(12,14);
		            
		            
		            Log.d("deletedsalesorder","deletedsalesorder cur data get");
		            Log.d("deletedsalesorder","mOrder_No = "+ mOrder_No);
		                        
		            String arrStr[] = { 
		            	mOrder_No,
		                mOrder_Date,
		                mCust_Code,
		                mCustomer_Ref_No,
		                mCreate_System_Date,
		                mLast_Mod_System_Date,
		                Integer.toString(mLine_no),
		                mItem_Code,
		                mTran_Code,
		                String.format("%.4f", mUnit_Price),
		                String.format("%.4f", mOrder_Qty) + Character.toString((char)13)
		    	    };
		            
		            Log.d("deletedsalesorder","deletedSalesOrderCsv data = "+Arrays.toString(arrStr));
		            
		            csvWrite.writeNext(arrStr);
		            deletedSalesOrderCur.moveToNext();
		        }
		        csvWrite.close();
	            deletedSalesOrderCur.close();
	        }catch(SQLException sqlEx){
				Log.e("deletedsalesorder", sqlEx.getMessage(), sqlEx);
			}catch (IOException IOe) {
				Log.e("deletedsalesorder", IOe.getMessage(), IOe);
			}
			Log.d("deletedsalesorder", "deletedsalesorder csv finished");
		}
		Log.d("deletedsalesorder", "deletedsalesorder() done");
	}
	
	/*
	 * Generate Csv file from route_cust table
	 */
	protected void visitInfo() throws SQLException{
		//Replenish
		//Write csv with specified name

		
		Cursor visitInfoCur = db.rawQuery(
			"SELECT Route as route"+
				", Cust_Code as cust_code"+
                ", Visit_Date as visit_date"+
                ", Status as status"+
                ", Chinese_Name as chinese_name"+
                ", Last_Order_Date as last_order_date"+
                ", Delivery_Seq as delivery_seq" +
            " FROM Route_Cust"+
            " WHERE Route = ?"+
            " AND Visit_Date is not null" +
            " AND Visit_Date like ?",
            new String[]{defaultRoute,util.getCurDate()+"%"}
            //new String[]{defaultRoute}
		);
		Log.d("visitinfo","defaultRoute"+defaultRoute);		
		Log.d("visitinfo","visitInfo set");
		
		//Creat visitinfo csv
		File exportCSV = new File(exportDir, visitInfoCsv);
		try {
			exportCSV.createNewFile();
			Log.d("visitinfo","visitInfo csv created");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("visitinfo","No of visitinfo record = " + Integer.toString(visitInfoCur.getCount()));
		if (visitInfoCur.moveToFirst()) {
			try{
				CSVWriter csvWrite = new CSVWriter(new FileWriter(exportCSV),',',CSVWriter.NO_QUOTE_CHARACTER);
			    //  int c = curCSV.getColumnCount();
				
				String[] curCol = {
					"route",
					"cust_code",
					"visit_date",
					"status"
				};
				//csvWrite.writeNext(curCol);				
				//Log.d("visitinfo","Write column finish");
				
				int loopCounter = visitInfoCur.getCount();
				for(int i=0;i<loopCounter;i++){
					String mRoute;
			        String mCust_Code; 
			        String mVisit_date;
			        String mStatus;
			        			               	
			        mRoute = visitInfoCur.getString(0); 
			        mCust_Code = visitInfoCur.getString(1);
			        
			        mVisit_date = visitInfoCur.getString(2);
			        String yyyy = mVisit_date.substring(0, 4);
			        String MM = mVisit_date.substring(4, 6);
			        String dd = mVisit_date.substring(6, 8);
			        String hh = mVisit_date.substring(8, 10);
			        String mm = mVisit_date.substring(10, 12);
			        String ss = mVisit_date.substring(12, 14);
			        mVisit_date = dd+"-"+MM+"-"+yyyy+" "+hh+":"+mm+":"+ss;
			        
			        mStatus = visitInfoCur.getString(3);
			        Log.d("visitinfo","mVisit_date" + visitInfoCur.getString(2));
			        Log.d("visitinfo","visitinfo cur data get");
			        
			        String arrStr[] = { 
			        	mRoute,
			        	mCust_Code,
			        	mVisit_date,
			        	mStatus + Character.toString((char)13)
			        };
			                
			        //Write column data into csv
			        csvWrite.writeNext(arrStr);
			        Log.d("visitinfo", "visitInfoCsv data = " + Arrays.toString(arrStr));
			        visitInfoCur.moveToNext();        
			    }
			    csvWrite.close();
			    visitInfoCur.close();
			    Log.d("visitinfo", "visitInfoCsv is finished");
			}catch(SQLException sqlEx){
				Log.e("visitinfo", sqlEx.getMessage(), sqlEx);
			}catch (IOException IOe) {
				Log.e("visitinfo", IOe.getMessage(), IOe);
			}
		}
		Log.d("visitinfo", "visitInfo() done");
	}
	
	/*
	 * Generate Csv file from adv_sales_order table
	 */
	protected void advSalesOrder() throws SQLException{
		Log.d("advsalesorder", "advSalesOrder() START");
		
		Cursor advSalesOrderCur = db.rawQuery(
			"SELECT order_date" +
			", cust_code"+
			", line_no"+
            ", item_code"+
            ", tran_code"+
            ", order_qty"+
            ", customer_ref_no "+
            "FROM adv_sales_order "+
            "WHERE Date(order_date) > Date(?)"+
            "ORDER BY order_date, cust_code, line_no", 
            //null
            new String[]{util.getCurDate()}
		);
					
		Log.d("advsalesorder","advSalesOrderCur SET");
		Log.d("advsalesorder","Current date = "+util.getCurDate());
					
		File exportCSV = new File(exportDir, advSalesOrderCsv);
		try {
			exportCSV.createNewFile();
			Log.d("advsalesorder","advSalesOrder csv created");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("advsalesorder","No of advsalesorder record = " + Integer.toString(advSalesOrderCur.getCount()));
		
		if (advSalesOrderCur.moveToFirst()) {
			try{
				CSVWriter csvWrite = new CSVWriter(new FileWriter(exportCSV),',',CSVWriter.NO_QUOTE_CHARACTER);
				//  int c = curCSV.getColumnCount();
			    
				String[] curCol = {
					"order_date",
					"cust_code",
					"line_no",
					"item_code",
					"tran_code",
					"order_qty",
					"customer_ref_no"
				};
				//csvWrite.writeNext(curCol);
				//Log.d("advsalesorder","Write column finish");
				
				int loopCounter = advSalesOrderCur.getCount();
				for(int i=0;i<loopCounter;i++){
			        String mOrder_Date = advSalesOrderCur.getString(0); 
			        String mCust_Code = advSalesOrderCur.getString(1);
			        int mLine_No = advSalesOrderCur.getInt(2);
			        String mItem_Code = advSalesOrderCur.getString(3);
			        String mTran_Code = advSalesOrderCur.getString(4);
			        float mOrder_Qty = advSalesOrderCur.getFloat(5);
			        String mCustomer_Ref_No = advSalesOrderCur.getString(6);
                    mOrder_Date = mOrder_Date.substring(6,8) + "-" +
                                  mOrder_Date.substring(4,6) + "-" +
              		              mOrder_Date.substring(0,4);
			        
				               	
				    String arrStr[] = { 
				    	mOrder_Date,
				    	mCust_Code,
				        Integer.toString(mLine_No),
				        mItem_Code,
				        mTran_Code,
				        Double.toString(mOrder_Qty),
				        mCustomer_Ref_No + Character.toString((char)13)
				    };
				                
				    //Write column data into csv
			        csvWrite.writeNext(arrStr);
			        Log.d("advsalesorder", "advsalesorder data = " + Arrays.toString(arrStr));
			        advSalesOrderCur.moveToNext();
				}
				csvWrite.close();
				advSalesOrderCur.close();
				            
			}catch(SQLException sqlEx){
				Log.e("advsalesorder", sqlEx.getMessage(), sqlEx);
			}catch (IOException IOe) {
				Log.e("advsalesorder", IOe.getMessage(), IOe);
			}
		}
		Log.d("advsalesorder", "advSalesOrder() DONE");
	}
	
	protected void exportGetCurDate(){  //Get current date
		currentDate = util.getCurDate();
		currentDate_ddMMyyyy = util.strToDateFmt(currentDate,"dd-MMM-yyyy");
		defaultRouteLower = defaultRoute.toLowerCase();
		replenishCsv = "replenish_" + defaultRouteLower + "_" + util.getCurDate() + ".csv"; 
		salesOrderCsv = "salesorder_" + defaultRouteLower + "_" + util.getCurDate() + ".csv";
		deletedSalesOrderCsv = "deletedsalesorder_" + defaultRouteLower + "_" + util.getCurDate() + ".csv";
		visitInfoCsv = "visitinfo_" + defaultRouteLower + "_" + util.getCurDate() + ".csv";
		advSalesOrderCsv = "advsalesorder_" + defaultRouteLower + "_" + util.getCurDate() + ".csv";
		
		Log.d("exportGetCurDate","default_route = "+defaultRoute );
		Log.d("exportGetCurDate","currentDate = "+currentDate );
		Log.d("exportGetCurDate","currentDate_ddMMyyyy = "+currentDate_ddMMyyyy );
		Log.d("exportGetCurDate","advSalesOrderCsv = "+advSalesOrderCsv );
	}

	
	public void ftpUpload(File sourceFolder){
		
		// Setup variables from getting pda table data
		Log.d("ftpupload","export csv directory = "+sourceFolder.getPath());
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		Uri ftpUri = Uri.parse("ftp://" + ftp_server_ip);
		intent.setDataAndType(ftpUri, "vnd.android.cursor.dir/lysesoft.andftp.uri");
		intent.putExtra("command_type", "upload");
		//intent.putExtra("ftp_username", "root");
		//intent.putExtra("ftp_password", "liaixlau");
		intent.putExtra("ftp_username", "pda");
		intent.putExtra("ftp_password", "pda209");
		//intent.putExtra("ftp_keyfile", "/sdcard/rsakey.txt");
		//intent.putExtra("ftp_keypass", "optionalkeypassword");
		intent.putExtra("ftp_pasv", "true");
		//intent.putExtra("ftp_resume", "true");
		//intent.putExtra("ftp_encoding", "UTF-8");
		intent.putExtra("progress_title", "Uploading folder ...");
		
		intent.putExtra("local_file1", sourceFolder.getPath()+"/"+replenishCsv);
		intent.putExtra("local_file2", sourceFolder.getPath()+"/"+salesOrderCsv);
		intent.putExtra("local_file3", sourceFolder.getPath()+"/"+deletedSalesOrderCsv);
		intent.putExtra("local_file4", sourceFolder.getPath()+"/"+visitInfoCsv);
		intent.putExtra("local_file5", sourceFolder.getPath()+"/"+advSalesOrderCsv);
		// Optional Initial remote folder (it must exist before upload)
		intent.putExtra("remote_folder", upload_folder);;
		
		startActivityForResult(intent, 2);
		
		String status = intent.getStringExtra("TRANSFERSTATUS");
		String files = intent.getStringExtra("TRANSFERAMOUNT"); 
		String size = intent.getStringExtra("TRANSFERSIZE");
		String time = intent.getStringExtra("TRANSFERTIME");
		
		
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		Log.i("upload", "Result: "+resultCode+ " from request: "+requestCode);
		if(intent!=null){
			if (requestCode == 1)	//Export to chosen directory
			{
			
				Bundle extras = intent.getExtras();
				if (extras != null) {
					mEdit.setText(extras.getString("匯出路徑資料"));
				}
			
			}else if (requestCode ==2){
				String transferredBytesStr = intent.getStringExtra("TRANSFERSIZE");
				String transferTimeStr = intent.getStringExtra("TRANSFERTIME");
				Log.i("upload", "Transfer status: " + intent.getStringExtra("TRANSFERSTATUS"));
				Log.i("upload", "Transfer amount: " + intent.getStringExtra("TRANSFERAMOUNT") + " file(s)");
				Log.i("upload", "Transfer size: " + transferredBytesStr + " bytes");
				Log.i("upload", "Transfer time: " + transferTimeStr + " milliseconds");
				// Compute transfer rate.
				if ((transferredBytesStr != null) && (transferTimeStr != null))
				{
					try
					{
						long transferredBytes = Long.parseLong(transferredBytesStr);
						long transferTime = Long.parseLong(transferTimeStr);
						double transferRate = 0.0;
						if (transferTime > 0) transferRate = ((transferredBytes) * 1000.0) / (transferTime * 1024.0);
						Log.i("upload", "Transfer rate: " + transferRate + " KB/s");
					} 
					catch (NumberFormatException e)
					{
						// Cannot parse string.
					}
				}
				
				util.DeleteRecursive(deleteDir);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////
	//          Override lifecycle function          //
	///////////////////////////////////////////////////
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (helper != null) {
			helper.close();
		}
	}
	
	public boolean onClose() {
		return false;
	}
	
	///////////////////////////////////////////////////
	//               Progress Dialog                 //
	///////////////////////////////////////////////////
	
	// Show Dialog Box with Progress bar
	private void setProgressDialog() {
	
		exportDialog = new ProgressDialog(this, 2);
		exportDialog.setTitle("生成檔案中");
		exportDialog.setMessage("Exporting csv files. Please wait...");
		exportDialog.setIndeterminate(false);
		exportDialog.setMax(5);
		exportDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		exportDialog.setCancelable(false);
		exportDialog.show();
	
	}
	
	// Async Task Class
	private class ExportCsvTask extends AsyncTask<File, Integer, Integer> {
	
		String fileName;
	
		// Import csv after download
		@Override
		protected Integer doInBackground(File... csv_listfiles) {
			long debugRV;
			int csvUpdateCounter = 0;
			int exportProgress = 0;
			
			//Export required Csv files one by one
						
			fileName = replenishCsv;
			exportProgress++;
			publishProgress(exportProgress);
			replenish();
			Log.d("ExportCsvTask","export replenishCsv");
			
			fileName = salesOrderCsv;
			exportProgress++;
			publishProgress(exportProgress);
			salesOrder();
			Log.d("ExportCsvTask","export salesOrderCsv");
			
			fileName = deletedSalesOrderCsv;
			exportProgress++;
			publishProgress(exportProgress);
			deletedSalesOrder();
			Log.d("ExportCsvTask","export deletedSalesOrderCsv");
			
			fileName = visitInfoCsv;
			exportProgress++;
			publishProgress(exportProgress);
			visitInfo();
			Log.d("ExportCsvTask","export visitInfoCsv");
			
			fileName = advSalesOrderCsv;
			exportProgress++;
			publishProgress(exportProgress);
			advSalesOrder();
			Log.d("ExportCsvTask","export advSalesOrderCsv");
			
			return csvUpdateCounter;
	
		}
	
		// While Downloading Music File
		protected void onProgressUpdate(Integer... progress) {
			// Set progress percentage
			exportDialog.setProgress(progress[0]);
			exportDialog.setMessage("Exporting " + fileName);
		}
	
		// Once Music File is downloaded
		@Override
		protected void onPostExecute(Integer importResult) {
			exportDialog.dismiss();
			Toast.makeText(getApplicationContext(), "Export complete",Toast.LENGTH_LONG).show();
			ftpUpload(exportDir);
			

		}
		
	}
	
}
