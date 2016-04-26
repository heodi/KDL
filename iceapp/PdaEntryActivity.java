package com.iceapp;

import android.os.Bundle;
import android.view.MenuInflater;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;


public class PdaEntryActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pda_entry);
        this.setTitle("PDA Master Edit");
        initViews();
        setListensers();
        getData();
    }

    private static final int selDefRouteOpt = Menu.FIRST;
    private EditText RouteEdt, PServerEdt, UpFolderEdt, PhoneEdt;
    private EditText PQueueEdt, LprEdt, FtpIpEdt, FtpFolderEdt, CsvEdt;
    private Button SaveBtn;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private MenuInflater mi;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	mi = getMenuInflater();
    	mi.inflate(R.menu.route_menu, menu);    	
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {    	
    		case R.id.routeOpt:
				createSelList();
				break;	
    	}
    	return true;
    }
    
    private void initViews() {
    	RouteEdt = (EditText)findViewById(R.id.editRoute);
    	PhoneEdt = (EditText)findViewById(R.id.editPhone);
    	PServerEdt = (EditText)findViewById(R.id.editPrintServer);
    	PQueueEdt = (EditText)findViewById(R.id.editPrintQueue);
    	LprEdt = (EditText)findViewById(R.id.editLPR);
    	FtpIpEdt = (EditText)findViewById(R.id.editFtpIP);
    	FtpFolderEdt = (EditText)findViewById(R.id.editFtpFolder);
    	CsvEdt = (EditText)findViewById(R.id.editCsvFolder);
    	UpFolderEdt = (EditText)findViewById(R.id.editUploadFolder);
    	SaveBtn = (Button)findViewById(R.id.butSave);
    	
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    }

    private void setListensers() {
    	SaveBtn.setOnClickListener(SaveClick);
    }
    
    public void onBackPressed() {
        this.finish();        	
    }
    
    private Button.OnClickListener SaveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		if (RouteEdt.length() == 0) {
    			util.showDialog("路線不可空白", PdaEntryActivity.this);
    		}
    		else {
        		UpdateTable();    			
    		}
    	}
    };
    
    private void getData() {
    	
    	db = dbh.getWritableDatabase();
    	Cursor c = db.rawQuery("select * from pda where default_route = 'Y'",null);
    	if (c.getCount() > 0) {
    		c.moveToFirst();
    		showData(c);
    	}
    	db.close();
    }
    
    private void showData(Cursor c) {
		RouteEdt.setText(c.getString(c.getColumnIndex("route")));
    	PhoneEdt.setText(c.getString(c.getColumnIndex("phone")));
    	PServerEdt.setText(c.getString(c.getColumnIndex("print_server_ip")));
    	PQueueEdt.setText(c.getString(c.getColumnIndex("print_queue")));
    	LprEdt.setText(c.getString(c.getColumnIndex("lpr_log_file")));
    	FtpIpEdt.setText(c.getString(c.getColumnIndex("ftp_server_ip")));
    	FtpFolderEdt.setText(c.getString(c.getColumnIndex("ftp_folder")));
    	CsvEdt.setText(c.getString(c.getColumnIndex("csv_local_folder")));
    	UpFolderEdt.setText(c.getString(c.getColumnIndex("upload_folder")));
    }
    
    private void UpdateTable() {

    	db = dbh.getWritableDatabase();
    	ContentValues cv =  new ContentValues();    	
    	
    	//Reset the default route
    	cv.put("default_route", "N");
    	db.update("PDA", cv, null, null); 

    	cv.clear();
    	cv.put("phone", PhoneEdt.getText().toString());
    	cv.put("print_server_ip", PServerEdt.getText().toString());
    	cv.put("print_queue", PQueueEdt.getText().toString());
    	cv.put("lpr_log_file", LprEdt.getText().toString());
    	cv.put("ftp_server_ip", FtpIpEdt.getText().toString());
    	cv.put("ftp_folder", FtpFolderEdt.getText().toString());
    	cv.put("csv_local_folder", CsvEdt.getText().toString());
    	cv.put("upload_folder", UpFolderEdt.getText().toString());
    	cv.put("default_route", "Y");
    	
    	String query = "select route "
    			+ "from pda "
    			+ "where route = ?";
    	Cursor c = db.rawQuery(query, new String[] {RouteEdt.getText().toString()});
    	if (c.getCount() > 0) {
        	//Update the data into the default route
        	db.update("PDA", cv, 
        			"route = '" + RouteEdt.getText().toString().toUpperCase() + "'", 
        			null);    		
    	} else {
    		cv.put("route", RouteEdt.getText().toString().toUpperCase());
    		db.insert("PDA", null, cv);
    	}
    	db.close();
    	    	    	    	    	
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(R.string.dialog_title);
		ad.setMessage("已儲存");
		ad.setCancelable(false);
		ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				finish();
			}
		});
		ad.show();
    	
    }
    
	private void createSelList() {
		String query = "SELECT "
				+ "route, "
				+ "phone, "
				+ "print_server_ip, "
				+ "print_queue, "
				+ "lpr_log_file, "
				+ "ftp_server_ip, "
				+ "ftp_folder, "
				+ "csv_local_folder, "
				+ "upload_folder "
				+ "FROM pda "
				+ "order by route";
		
		db = dbh.getWritableDatabase();
		final Cursor c = db.rawQuery(query,null);		
		final String [] routes = new String[c.getCount()];
		int i = 0;
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				routes[i] = c.getString(c.getColumnIndex("route"));
				i++;
			} while (c.moveToNext());
		}
		db.close();
		
		//create Pop-UP list
		AlertDialog.Builder RouteBuilder = new AlertDialog.Builder(this);
		RouteBuilder.setTitle("請選擇預設路線");
		RouteBuilder.setSingleChoiceItems(routes, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				c.moveToPosition(which);
				showData(c);
		    	c.close();
				dialog.dismiss();
			}
		});
		AlertDialog ad = RouteBuilder.create();
		ad.show();
	}
}
