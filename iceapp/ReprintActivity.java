package com.iceapp;

import android.support.v7.app.ActionBarActivity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.os.Build;
import android.widget.EditText;
import android.database.Cursor;
import android.content.Intent;
import android.app.Activity;
import android.content.ContentValues;

public class ReprintActivity extends PrintInvActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reprint);
        this.setTitle("列印銷售單");
        initViews();
        setListensers();
    }

    private Button okBtn;
    private EditText orderNumTxt;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;

    private void initViews() {
    	orderNumTxt = (EditText)findViewById(R.id.editOrderNum);
    	okBtn = (Button)findViewById(R.id.butOk);
    	util = new IceUtil();
    	dbh = new DatabaseHelper(this);
    }
    
    private void setListensers() {
    	okBtn.setOnClickListener(okClick);    	
    }
    
    private Button.OnClickListener okClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String orderNo = orderNumTxt.getText().toString();
    		if (orderNo.equals("")) {
    			util.showDialog("請輸入銷售單號", ReprintActivity.this);
    			orderNumTxt.requestFocus();
    		}
    		else {
    			db = dbh.getWritableDatabase();
    			String query = "select order_no "
    					+ "from sales_order_hdr "
    					+ "where order_no = ? "
    					+ "and order_status = 'C'";
    					
				Cursor c = db.rawQuery(query, new String[] {orderNo});
				
        		if (c.moveToFirst()) {
        			printInvoice(orderNo);
        		} else {
        			util.showDialog("找不到此銷售單號", ReprintActivity.this);
        		}
    			db.close();    		
    		}
    	}
    };
    
    @Override
    public void onBackPressed() {
    	finish();
    }
    	        
}
