package com.iceapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ImportCsvActivity extends ActionBarActivity {
	private static final String TAG = MainActivity.class.getName();
	private static final int DOWNLOAD_FOLDER_REQUEST = 2;
	private static final String tableName[] = { "adv_sales_order","company",
			"conversion_file", "customer_file", "item_alias_for_cust",
			"item_alias_for_group", "item_file", "not_sell_for_cust",
			"not_sell_for_group", "price_book", "promo_buy", "promo_exception",
			"promo_for_cust", "promo_for_group", "promo_get", "promo_header",
			"route_cust", "sales_order_dtl", "sales_order_hdr", "truck_open",
			"vendor_item_desc" };

	private String totalStock;
	private DatabaseHelper helper;
	private SQLiteDatabase db;
	private ProgressDialog importDialog;
	public static final int progress_bar_type = 0;
	private File appDirectory;
	private File routeFolder;
	private File csv;
	private TextView reportMessage;
	private int checkCounter = 0;
	private EditText mEdit;
	private Button downloadFolderButton;
	private IceUtil util;
	private static final String RECORD = "import_result";
	int totalUpdate =21;
	private int routeCustCounter;
	private ArrayList<String>  mCustList= new ArrayList<String>();
	final String[] routeList = {"A1","B1","C1","D1","E1","F1","G1","S1","Z1"};
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_csv);
		helper = new DatabaseHelper(this);
		db = helper.getWritableDatabase();
		util = new IceUtil();
		
		//Check current time
		if (!(Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000)){
	    	totalUpdate =19;
	    }

		//get last updated record
		SharedPreferences imptHis = getSharedPreferences(RECORD, 0);
		String history = imptHis.getString("history", null);
		
		// Setup layout
		// reportTitle = (TextView)findViewById(R.id.output_label);
		reportMessage = (TextView) findViewById(R.id.output_result);
		reportMessage.setText(history);
		// title = (TextView)findViewById(R.id.input_label);
		mEdit = (EditText) findViewById(R.id.input_edittext); // Edittext for user to enter the import file directory
		// Get the content in Edittext and perform download and import process
		Button browseButton = (Button) findViewById(R.id.input_button);

		// Browse the local file
		browseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ImportCsvActivity.this,FileBrowser.class);
				startActivityForResult(intent, 1);
			}
		});
		
		
	    
		
		// Download all csv files from pda server(require local wifi login )
		downloadFolderButton = (Button) findViewById(R.id.button_download_folder_id);
		downloadFolderButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (util.EditTextisEmpty(mEdit)) {
					// import file through default setting

					Cursor cur = db.rawQuery(
									"SELECT route as _id,ftp_server_ip,ftp_folder,csv_local_folder,upload_folder,download_folder FROM pda WHERE default_route =?",
									new String[] { "Y" });
					String default_route;
					String ftp_server_ip;
					String ftp_folder;
					String csv_local_folder;
					String upload_folder;
					String download_folder;
					String appFolder;

					

					if (cur.moveToFirst()) {

						// Setup variables from getting pda table data
						default_route = cur.getString(cur.getColumnIndex("_id"));
						ftp_server_ip = cur.getString(cur.getColumnIndex("ftp_server_ip"));
						ftp_folder = cur.getString(cur.getColumnIndex("ftp_folder"));
						csv_local_folder = cur.getString(cur.getColumnIndex("csv_local_folder"));
						upload_folder = cur.getString(cur.getColumnIndex("upload_folder"));
						download_folder = cur.getString(cur.getColumnIndex("download_folder"));

						// [Debug]Check the above values
						Log.d("Directory", "default_route = " + default_route);
						Log.d("Directory", "ftp_server_ip = " + ftp_server_ip);
						Log.d("Directory", "ftp_folder = " + ftp_folder);
						Log.d("Directory", "csv_local_folder = "+ csv_local_folder);
						Log.d("Directory", "upload_folder = " + upload_folder);
						Log.d("Directory", "download_folder = "+ download_folder);

						// Prepare route variables(folder for String class,
						// directory for File Class)
						File memorydirectory = Environment.getExternalStorageDirectory();
						appFolder = memorydirectory.getPath()+ csv_local_folder;
						Log.d("Directory", "appFolder = " + appFolder);
						appDirectory = new File(appFolder); // appDirectory =/storage/emulated/0/ICEAPP/CSV
						routeFolder = new File(appFolder + "/" + default_route); // Directory to be deleted
						Log.d("Directory", "routeFolder = " + appFolder + "/" + default_route);

						// Delete all old csv files
						util.DeleteRecursive(routeFolder);
						Log.d("DeleteRecursive","getCurDateTime().substring(8,14) = " + util.getCurDateTime().substring(8,14));
						

						// Delete old data
						db.delete(tableName[0], null, null);
						db.delete(tableName[1], null, null);
						db.delete(tableName[2], null, null);
						db.delete(tableName[3], null, null);
						db.delete(tableName[4], null, null);
						db.delete(tableName[5], null, null);
						db.delete(tableName[6], null, null);
						db.delete(tableName[7], null, null);
						db.delete(tableName[8], null, null);
						db.delete(tableName[9], null, null);
						db.delete(tableName[10], null, null);
						db.delete(tableName[11], null, null);
						db.delete(tableName[12], null, null);
						db.delete(tableName[13], null, null);
						db.delete(tableName[14], null, null);
						db.delete(tableName[15], null, null);
						if (Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000){ //midnight?
							//Yes,delete
							db.delete(tableName[16], null, null);//keep specical record only
							db.delete(tableName[17], null, null);//sales_order_dtl
							db.delete(tableName[18], null, null);//sales_order_hdr
							Log.d("test", "time within");
						}else {
							//no,keep status !=0 record
							db.delete(tableName[16],"status = \"0\"",null);
							Cursor routeCustCur = db.rawQuery("SELECT cust_code as _id FROM route_cust",null);
							routeCustCounter= routeCustCur.getCount();
							if (routeCustCur.moveToFirst()) {
								for (int i=0;i<routeCustCounter;i++){
									String cust_code = routeCustCur.getString(0);
									Log.d("routecust", "cust_code ="+cust_code);
									mCustList.add(cust_code);
									routeCustCur.moveToNext();
								}
								
							}
							routeCustCur.close();
							
						}
						db.delete(tableName[19], null, null);
						db.delete(tableName[20], null, null);

						// /////////////////////Download
						// CSV/////////////////////////
						// Recreate a new empty folder
						appDirectory.mkdir();		
						// Modify route_directory to lowest level folder
						// Call Andftp to download csv files from ftpserver
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_PICK);
						// FTP URL (Starts with ftp://, sftp://, ftps:// or
						// scp:// followed by hostname and port).
						Uri ftpUri = Uri.parse("ftp://" + ftp_server_ip);
						intent.setDataAndType(ftpUri,
								"vnd.android.cursor.dir/lysesoft.andftp.uri");
						// FTP credentials (optional)
						intent.putExtra("ftp_username", "oracle9i");
						intent.putExtra("ftp_password", "aixlilau");
						// intent.putExtra("ftp_keyfile", "/sdcard/dsakey.txt");
						// intent.putExtra("ftp_keypass",
						// "optionalkeypassword");
						// FTP settings (optional)
						intent.putExtra("ftp_pasv", "true");
						// intent.putExtra("ftp_resume", "true");
						// intent.putExtra("ftp_encoding", "UTF-8");
						// Download
						intent.putExtra("command_type", "download");
						// Activity title
						intent.putExtra("progress_title",
								"Downloading folder ...");
						// Remote folder to download (must not end with /).
						intent.putExtra("remote_file1", ftp_folder + "/adv_sales_order.csv");
						intent.putExtra("remote_file2", ftp_folder + "/company.csv");
						intent.putExtra("remote_file3", ftp_folder + "/conversion_file.csv");
						intent.putExtra("remote_file4", ftp_folder + "/customer_file.csv");
						intent.putExtra("remote_file5", ftp_folder + "/item_alias_for_cust.csv");
						intent.putExtra("remote_file6", ftp_folder + "/item_alias_for_group.csv");
						intent.putExtra("remote_file7", ftp_folder + "/item_file.csv");
						intent.putExtra("remote_file8", ftp_folder + "/not_sell_for_cust.csv");
						intent.putExtra("remote_file9", ftp_folder + "/not_sell_for_group.csv" );
						intent.putExtra("remote_file10", ftp_folder + "/price_book.csv");
						intent.putExtra("remote_file11", ftp_folder + "/promo_buy.csv");
						intent.putExtra("remote_file12", ftp_folder + "/promo_exception.csv");
						intent.putExtra("remote_file13", ftp_folder + "/promo_for_cust.csv");
						intent.putExtra("remote_file14", ftp_folder + "/promo_for_group.csv");
						intent.putExtra("remote_file15", ftp_folder + "/promo_get.csv");
						intent.putExtra("remote_file16", ftp_folder + "/promo_header.csv");
						intent.putExtra("remote_file17", ftp_folder + "/route_cust.csv");
						intent.putExtra("remote_file18", ftp_folder + "/truck_open.csv");
						intent.putExtra("remote_file19", ftp_folder + "/vendor_item_desc.csv");
						intent.putExtra("remote_file20", ftp_folder + "/sales_order_dtl.csv");
						intent.putExtra("remote_file21", ftp_folder + "/sales_order_hdr.csv");
												
						// intent.putExtra("local_folder", directory);
						intent.putExtra("local_folder", appFolder+ "/"+default_route);
						startActivityForResult(intent, DOWNLOAD_FOLDER_REQUEST);

					}
					cur.close();

				} else {

					// import file from local folder

					routeFolder = new File(mEdit.getText().toString());

					if (!routeFolder.exists()) {
						checkCounter++;
					}

					for (int m = 0; m < 21; m++) {
						csv = new File(routeFolder.toString() + "/" + tableName[m] + ".csv");
						if (!csv.exists()) {
							checkCounter++;
						}
					}

					if (checkCounter > 0) {
						new AlertDialog.Builder(ImportCsvActivity.this)
								.setIcon(R.drawable.ic_launcher)
								.setTitle("警告")
								.setMessage("Path not found or the csv files do not match")
								.setPositiveButton("OK", null).show();
					} else {
						File[] csvfile = routeFolder.listFiles();
						ImportCsvTask task = new ImportCsvTask();
						task.execute(csvfile);
						setProgressDialog();
					}

				}
			}
		});


	}

	///////////////////////////////////////////////////
	//         Reporting class for AndFTP result     //
	///////////////////////////////////////////////////

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Log.i(TAG, "Result: " + resultCode + " from request: " + requestCode);
		if (intent != null) {
			if (requestCode == 1) {

				Bundle extras = intent.getExtras();
				if (extras != null) {
					mEdit.setText(extras.getString("path"));
					downloadFolderButton.setText("下載已選擇路徑的資料");
				}
			} else if (requestCode == 2) {
				String transferredBytesStr = intent.getStringExtra("TRANSFERSIZE");
				String transferTimeStr = intent.getStringExtra("TRANSFERTIME");
				Log.i(TAG, "Transfer status: " + intent.getStringExtra("TRANSFERSTATUS"));
				Log.i(TAG, "Transfer amount: "	+ intent.getStringExtra("TRANSFERAMOUNT") + " file(s)");
				Log.i(TAG, "Transfer size: " + transferredBytesStr + " bytes");
				Log.i(TAG, "Transfer time: " + transferTimeStr + " milliseconds");
				// Compute transfer rate.
				if ((transferredBytesStr != null) && (transferTimeStr != null)) {
					try {
						long transferredBytes = Long .parseLong(transferredBytesStr);
						long transferTime = Long.parseLong(transferTimeStr);
						double transferRate = 0.0;
						if (transferTime > 0)
							transferRate = ((transferredBytes) * 1000.0) / (transferTime * 1024.0);
						Log.i(TAG, "Transfer rate: " + transferRate + " KB/s");
					} catch (NumberFormatException e) {
						// Cannot parse string.
					}
				}

				///////////////////////Import CSV/////////////////////////

				File[] csvfile = routeFolder.listFiles();
				Log.d("Files", "routeFolder = " + routeFolder);
				Log.d("Files", "csvfile.length = " + Integer.toString(csvfile.length));
				for (int i=0; i < csvfile.length; i++)
				{
				    Log.d("Files", "FileName = " + csvfile[i].getName());
				}
				ImportCsvTask task = new ImportCsvTask();
				task.execute(csvfile);
				setProgressDialog();
			}
		}
	}

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

	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences imptHis = getSharedPreferences(RECORD,0);
		SharedPreferences.Editor editor = imptHis.edit();
		editor.putString("history",reportMessage.getText().toString() );

	    // Commit the edits!
	    editor.commit();
		
	}
	

	///////////////////////////////////////////////////
	//               Progress Dialog                 //
	///////////////////////////////////////////////////

	// Show Dialog Box with Progress bar
	private void setProgressDialog() {

		importDialog = new ProgressDialog(this, 2);
		importDialog.setTitle("載入檔案中");
		importDialog.setMessage("Importing csv files. Please wait...");
		importDialog.setIndeterminate(false);
		importDialog.setMax(totalUpdate);
		importDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		importDialog.setCancelable(false);
		importDialog.show();

	}

	// Async Task Class
	private class ImportCsvTask extends AsyncTask<File, Integer, Integer> {

		String fileName;
		String table;

		// Import csv in background
		@Override
		protected Integer doInBackground(File... csv_listfiles) {
			long debugRV;
			int csvUpdateCounter = 0;
			int totalUpdate =21;
			String[] custArr = new String[mCustList.size()];
			Log.d("routecust", "mCustList.size() ="+Integer.toString(mCustList.size()));
			custArr = mCustList.toArray(custArr);
			try {
				// Put all name of csv into a string array
				
			    
				Log.d("ImportCsvTask", "start for loop");
				for (int j = 0; j < totalUpdate; j++) {

					InputStreamReader isr = new InputStreamReader(new FileInputStream(csv_listfiles[j]), "BIG5");
					Log.d("ImportCsvTask", "InputStreamReader created");
					BufferedReader buffer = new BufferedReader(isr);
					Log.d("ImportCsvTask", "BufferedReader created");
					ContentValues contentValues = new ContentValues();
					
					fileName = csv_listfiles[j].getName(); // get csv file name
					Log.d("ImportCsvTask", "importing " + fileName);
					int endIndex = fileName.lastIndexOf("."); //get the index of "." in file name
				    if (endIndex != -1)  
				    {
				    	table = fileName.substring(0, endIndex); // cut away ".csv" from file name to get table name
				    }
				    Log.d("test", "Should " + fileName + " be imported ? " +String.valueOf((!(table.equals("sales_order_dtl") || table.equals("sales_order_hdr")))||(Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000)));
				    
					if ((!(table.equals("sales_order_dtl") || table.equals("sales_order_hdr")))||(Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000)){
						Log.d("test", "fileName = " + fileName);
						Log.d("test", "table = " + table);
						Log.d("test","Is it sales_order_dtl? " + String.valueOf(table == "sales_order_dtl"));
						Log.d("test","Is it sales_order_hdr? " + String.valueOf(table == "sales_order_hdr"));
						Log.d("test","Is it not sales_order? " + String.valueOf(!(table == "sales_order_dtl" || table == "sales_order_hdr")));
						Log.d("test","util.getCurDateTime().substring(8,14) = " + util.getCurDateTime().substring(8,14));
						Log.d("test","Is it after 8pm?" + String.valueOf(Integer.parseInt(util.getCurDateTime().substring(8,14))>200000));
						Log.d("test","Is it before 8am?" + String.valueOf(Integer.parseInt(util.getCurDateTime().substring(8,14))<80000));
						Log.d("test","Is it midnight? " + String.valueOf((Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000)));
						
						
						String line = buffer.readLine(); // read first line to get the column
						String[] cols = line.split("\t");
											
						if (table.equals("route_cust")){
							Log.d("routecust", "importing route_cust record");
							Log.d("routecust", "custArr.length ="+Integer.toString(custArr.length));
							for(int k=0;k<custArr.length;k++){
								Log.d("routecust", "custArr["+Integer.toString(k)+"] = "+custArr[k]);
							}
							
							
							while ((line = buffer.readLine()) != null) {
								String[] str = line.split("\t"); // read every single line of record in  csv
								for (int i = 0; i < cols.length; i++) {
									str[i] = str[i].replaceAll("\"", "");
									
									if (!isCustExist(str[1],custArr)){
										contentValues.put(cols[i], str[i].trim());
										Log.d("ice", cols[i] + "= " + str[i]);
									}else {
										Log.d("routecust", "!isCustExist = "+Boolean.toString(!isCustExist(str[i],custArr)));
										Log.d("routecust", "matched "+cols[i]+" = "+str[i]);
									}
									
								}
								if (!isCustExist(str[1],custArr)){
									debugRV = db.insert(table, null, contentValues);
									Log.d("ice", "Import result for " + table + "= " +Long.toString(debugRV));
								}
								// Displaying import progress for each file using
								// horizontal bar
								publishProgress(j);
							}
						}else{
							while ((line = buffer.readLine()) != null) {
								Log.d("ice", "Read next record");
								Log.d("ice", "Line" + Integer.toString(j) + ": " + line);
								String[] str = line.split("\t"); // read every single line of record in  csv
								for (int i = 0; i < cols.length; i++) {
									str[i] = str[i].replaceAll("\"", "");
									contentValues.put(cols[i], str[i].trim());
									Log.d("ice", cols[i] + "= " + str[i]);
								}
								debugRV = db.insert(table, null, contentValues);
								Log.d("ice", "Import result for " + table + "= " +Long.toString(debugRV));

								// Displaying import progress for each file using
								// horizontal bar
								publishProgress(j);
							}
						}
						
					}					

					buffer.close();
					csvUpdateCounter++;

				}
				
				Log.d("retrieve","Should traceback? " + String.valueOf((!(table.equals("sales_order_dtl") || table.equals("sales_order_hdr")))||(Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000)));
				if (!(Integer.parseInt(util.getCurDateTime().substring(8,14))>200000 || Integer.parseInt(util.getCurDateTime().substring(8,14))<80000)){
					Log.d("retrieve","traceBackTOC() started");
					traceBackTOC();
				}

			} catch (IOException e) {
				Log.e(getClass().getSimpleName(),
					"Caught IOException when importing files", e);
			}
			return csvUpdateCounter;
		}

		// While Downloading Csv File
		protected void onProgressUpdate(Integer... progress) {
			// Set progress percentage
			importDialog.setProgress(progress[0]);
			importDialog.setMessage("Importing " + fileName);
			
			
		}

		// Once Csv File is downloaded
		@Override
		protected void onPostExecute(Integer importResult) {
			importDialog.dismiss();
			Toast.makeText(getApplicationContext(), "Import complete",Toast.LENGTH_LONG).show();
			Cursor cur = db.rawQuery("SELECT SUM(opening_qty) as _id FROM truck_open ", null);
			if (cur.moveToFirst()) {
				totalStock = cur.getString(0);
				reportMessage.setText("Number of files updated: " + Integer.toString(importResult) + "\n" + "Total stock: " + totalStock);
			}
			cur.close();
			
			util.DeleteRecursive(appDirectory);
			
			
		}
	}
	
	////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.import_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.default_import:
	        	
	        	Builder defaultRouteDialog = new AlertDialog.Builder(this);
	        	defaultRouteDialog.setTitle("請選擇路線");
	        	//建立選擇的事件
	        	DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener(){
	        		public void onClick(DialogInterface dialog, int choice) {
	        			importDefaultData(routeList[choice]);
	        	}
	        	
	        	};
	        	
	        	DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener(){
	        		public void onClick(DialogInterface dialog, int which) {
	        		}
	        	}; 
	        	
	        	defaultRouteDialog.setItems(routeList, ListClick);
	        	defaultRouteDialog.setNeutralButton("取消",OkClick );
	        	defaultRouteDialog.show();
    			
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public void importDefaultData(String inputRoute){
		int deleterv;
		long insertrv;
		String route;
		
		deleterv = db.delete("pda", null, null);
		
		
		ContentValues cv = new ContentValues();
		
		for (int i=0;i<9;i++){
			cv.put("route",routeList[i]);
			cv.put("print_server_ip", "192.168.1.2");
			cv.put("print_queue", "p1");
			cv.put("lpr_log_file", "LPR.log");
			cv.put("ftp_server_ip", "192.9.205.1");
			cv.put("ftp_folder", "/pda/pdadata/"+routeList[i]);
			cv.put("csv_local_folder", "/ICEAPP/CSV");
			cv.put("upload_folder", "/tmp");
			cv.put("download_folder", "/home/oracle9i/pdadata");
			cv.put("phone", "61917963");
			if (routeList[i] ==inputRoute){
				cv.put("default_route", "Y");
			}else {
				cv.put("default_route", "N");
			}
			
			insertrv = db.insert("pda", null, cv);
			Log.d("ice","Insert return value of A1: " + Long.toString(insertrv));
		}
		
		/*
		cv.put("route", "A1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/A1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "61917963");
		cv.put("default_route", "Y");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of A1: " + Long.toString(insertrv));
		// insert A1 settings

		cv.put("route", "B1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/B1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "61917964");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of B1: " + Long.toString(insertrv));
		// insert B1 setting

		cv.put("route", "C1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/C1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "61917965");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of C1: " + Long.toString(insertrv));
		// insert C1 setting

		cv.put("route", "D1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/D1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "61917966");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of D1: " + Long.toString(insertrv));
		// insert D1 setting

		cv.put("route", "E1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/E1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp/");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "61917967");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of E1: " + Long.toString(insertrv));
		// insert E1 setting

		cv.put("route", "F1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/F1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp/");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "61919243");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of F1: " + Long.toString(insertrv));
		// insert F1 setting

		cv.put("route", "G1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/G1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp/");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "66800433");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of G1: " + Long.toString(insertrv));
		// insert G1 setting

		cv.put("route", "S1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/S1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp/");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "59315813");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of S1: " + Long.toString(insertrv));
		// insert S1 setting
		Log.d("ice", "Finish inserting default values into pda table.");
		
		cv.put("route", "Z1");
		cv.put("print_server_ip", "192.168.1.2");
		cv.put("print_queue", "p1");
		cv.put("lpr_log_file", "LPR.log");
		cv.put("ftp_server_ip", "192.9.205.1");
		cv.put("ftp_folder", "/pda/pdadata/Z1");
		cv.put("csv_local_folder", "/ICEAPP/CSV");
		cv.put("upload_folder", "/tmp/");
		cv.put("download_folder", "/home/oracle9i/pdadata");
		cv.put("phone", "59315813");
		cv.put("default_route", "N");
		insertrv = db.insert("pda", null, cv);
		Log.d("ice","Insert return value of Z1: " + Long.toString(insertrv));
		// insert S1 setting
		Log.d("ice", "Finish inserting default values into pda table.");
		*/
		
		Toast.makeText(getApplicationContext(), "Default route = "+inputRoute, Toast.LENGTH_LONG).show();
	}
	
	
	//trace back today invoice
	public void traceBackTOC(){
		String retrieve_qty;
		int soldItemCounter;
		ContentValues cv = new ContentValues();
		String where = "item_code=?";
		String[] whereArgs = new String[]{""};
		
		Cursor retrieveCur = db.rawQuery(
				"SELECT SUM(a.order_qty), " +
				"a.item_code "+
				"FROM sales_order_dtl a, " +
				"sales_order_hdr b " +
				"WHERE a.order_no = b.order_no "+
				"AND b.order_date = ? " +
				"AND b.order_status <> ? "+
				"group by a.item_code",
				new String[] { util.getCurDate(),"D" });
		
		soldItemCounter = retrieveCur.getCount();	
		Log.d("retrieve", "getCount = " +soldItemCounter);
		Log.d("retrieve", "retrieve_qty = " +soldItemCounter);
		
		if (retrieveCur.moveToFirst()){
			for(int i=0;i<soldItemCounter;i++){
				retrieve_qty = retrieveCur.getString(0);
				Log.d("retrieve", "retrieve_qty OK");
				whereArgs[0] = retrieveCur.getString(1);
				Log.d("retrieve", "whereArgs[0] OK");
				cv.put("sales_qty", retrieve_qty);
				Log.d("retrieve", "order_qty = "+retrieve_qty + "  item_code = "+whereArgs[0]);
				db.update("truck_open", cv, where, whereArgs);
				retrieveCur.moveToNext();
				
			}
		}
		
		
		retrieveCur.close();
	}
	

	
	public boolean isCustExist(String cust, String[] custArr){
		//Log.d("routecust", "cust = "+cust);
		
		for (int i=0;i<custArr.length;i++){
			//Log.d("routecust", "custArr["+Integer.toString(i)+"] = "+custArr[i]);
			if (cust.equals(custArr[i])){
				return true;
			}
		}
		return false;
	}
	
	
	

}