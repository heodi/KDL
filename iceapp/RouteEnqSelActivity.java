package com.iceapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
 
public class RouteEnqSelActivity extends Activity {
 
    private RadioGroup rgReason;
	private Button saveBtn, callProductPriceBtn, callCustBtn, callOrderBtn;
	private TextView custCodeTxt, custStatusTxt, custNameTxt;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
	
    private IceUtil util;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_enq_sel);        
        initViews();
        setListensers();
        getParaAndShowResults();
      
        this.setTitle("路線資料(2)");
    }

    private void initViews() {
    	
        rgReason = (RadioGroup)findViewById(R.id.rgReason);
        custCodeTxt = (TextView)findViewById(R.id.txtCustCode);
        custNameTxt = (TextView)findViewById(R.id.txtCustName);
        custStatusTxt = (TextView)findViewById(R.id.txtCustStatus);
        saveBtn = (Button)findViewById(R.id.butSave);
        callProductPriceBtn = (Button)findViewById(R.id.butStockPrice);
        callCustBtn = (Button)findViewById(R.id.butCustDetails);
        callOrderBtn = (Button)findViewById(R.id.butSales);
        
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    }

    private void setListensers() {
    	saveBtn.setOnClickListener(saveClick);
    	callProductPriceBtn.setOnClickListener(callProductPriceClick);
    	callCustBtn.setOnClickListener(callCustDetailsClick);
    	callOrderBtn.setOnClickListener(callOrderClick);
    }

    private void getParaAndShowResults () {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	custCodeTxt.setText(bundle.getString("custCode"));
    	custNameTxt.setText(bundle.getString("custName"));
    	custStatusTxt.setText(bundle.getString("custStatus"));
    }
    
    private Button.OnClickListener saveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		if (custStatusTxt.getText().toString().equals("已去")) {
    			util.showDialog("所選之客戶已落單，檔案不允更改", RouteEnqSelActivity.this);
    			
    		}
    		else {    				
    			int id = rgReason.getCheckedRadioButtonId(); 
    				
    			if (id < 0)
    				util.showDialog("請先選擇未去原因", RouteEnqSelActivity.this);
    			else {
    				int reason;
        			if (id == R.id.rbCalled)
        			{
        				reason = 2;
        				custStatusTxt.setText("已電");
        			}
        			else if (id == R.id.rbnoTime)
        			{
        				reason = 3;
        				custStatusTxt.setText("沒空");
        			}
        			else
        			{
        				reason = 4; // id == R.id.rbNoorder
        				custStatusTxt.setText("無落單");
        			}
        			
        			db = dbh.getWritableDatabase();
        			//update ROUTE_CUST table
        			ContentValues cv = new ContentValues();
    				cv.put("status", reason);
    				cv.put("visit_date", util.getCurDateTime());    			
    				db.update("route_cust", cv, 
    						"route = (select route from pda where default_route = 'Y') and cust_code = ?", 
    						new String[] {custCodeTxt.getText().toString()});
        			/**
        			String defaultRoute;
        	    	String query = "select route from pda "
        	    			+ "where default_route = 'Y'";
        	    	Cursor c = db.rawQuery(query, null);
        	    	c.moveToFirst();
        	    	defaultRoute = c.getString(c.getColumnIndex("route"));

        			//update ROUTE_CUST table
        			ContentValues cv = new ContentValues();
    				cv.put("status", reason);
    				cv.put("visit_date", util.getCurDateTime());    			
    				db.update("route_cust", cv, 
    						"route = ? and cust_code = ?", 
    						new String[] {defaultRoute, custCodeTxt.getText().toString()});
    				c.close();
    				*/
    				
    				db.close();
    				
    				Toast toast = Toast.makeText(RouteEnqSelActivity.this, "已儲存", Toast.LENGTH_SHORT);
    				toast.show();
    			}
    		}
    	}
    };
    
    private Button.OnClickListener callProductPriceClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent it = new Intent(RouteEnqSelActivity.this, ProductPriceEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "RouteEnqSel");
    		bundle.putString("cust_code", custCodeTxt.getText().toString());
    		it.putExtras(bundle);    		
    		//startActivity(it);
    		startActivityForResult(it, 0);
    	}    	
    };
    
    private Button.OnClickListener callCustDetailsClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		    		    		
        	String query = "SELECT "
        			+ "a.cust_code as _id, "
        			+ "a.cust_group, "
        			+ "a.pay_term, "    			
        			+ "a.chinese_name, "
        			+ "a.english_name, "
        			+ "a.address_1, "
        			+ "a.address_2, "
        			+ "a.address_3, "
        			+ "a.address_4, "
        			+ "a.contact, "
        			+ "a.tel_1, "
        			+ "a.tel_2, "
        			+ "c.last_order_date "    			
        			+ "from customer_file a, pda b, route_cust c "
        			+ "where a.route = b.route "
        			+ "and a.cust_code = c.cust_code "
        			+ "and a.cust_code = ? ";
        	
        	db = dbh.getWritableDatabase();
        	Cursor c = db.rawQuery(query, new String[] {custCodeTxt.getText().toString()} );

        	if (c.getCount() > 0) {        		
        		c.moveToFirst();
        		Intent it = new Intent(RouteEnqSelActivity.this, CustEnqDetailsActivity.class);
        		Bundle bundle = new Bundle();
        		bundle.putString("cust_code", custCodeTxt.getText().toString());    		
            	bundle.putString("pay_term", c.getString(c.getColumnIndex("pay_term")));
            	bundle.putString("chinese_name", c.getString(c.getColumnIndex("chinese_name")));                	
            	bundle.putString("english_name", c.getString(c.getColumnIndex("english_name")));        	
            	bundle.putString("address_1", c.getString(c.getColumnIndex("address_1")));        	
            	bundle.putString("address_2", c.getString(c.getColumnIndex("address_2")));        	
            	bundle.putString("address_3", c.getString(c.getColumnIndex("address_3")));        	
            	bundle.putString("address_4", c.getString(c.getColumnIndex("address_4")));
            	bundle.putString("contact", c.getString(c.getColumnIndex("contact")));
            	bundle.putString("tel_1", c.getString(c.getColumnIndex("tel_1")));
            	bundle.putString("tel_2", c.getString(c.getColumnIndex("tel_2")));
            	bundle.putString("last_order_date", c.getString(c.getColumnIndex("last_order_date")));
            	bundle.putString("cust_group", c.getString(c.getColumnIndex("cust_group")));
        		it.putExtras(bundle);    		
        		//startActivity(it);
        		startActivityForResult(it, 1);
        	}
        	db.close();
    	}    	    	
    };
    
    private Button.OnClickListener callOrderClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
        	//Pass data to another Activity 
        	Intent it = new Intent(RouteEnqSelActivity.this, SalesDetailsActivity.class);
        	Bundle bundle = new Bundle();
        	bundle.putString("numOrcode", "cust_code");
        	bundle.putString("value", custCodeTxt.getText().toString());
        	it.putExtras(bundle);
        	//startActivity(it);
        	startActivityForResult(it, 2);
    	}
    };    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode > 0) {
    		db = dbh.getWritableDatabase();    		
    		String query = "select "
    	    		+ "case status "
    	    		+ "when '0' then '未去' "
    	    		+ "when '1' then '已去' "
    	    		+ "when '2' then '已電' "
    	    		+ "when '3' then '沒空' "
    	    		+ "else '無落單' "
    	    		+ "end status "
    	    		+ "from route_cust "
    	    		+ "where route = "
    	    		+ "(select route from pda where default_route = 'Y') "
    	    		+ "and cust_code = ? ";
    		Cursor c = db.rawQuery(query, new String[] {custCodeTxt.getText().toString()});
    		if (c.moveToFirst())
    			custStatusTxt.setText(c.getString(c.getColumnIndex("status")));
    		c.close();
    		db.close();
    	}
    }
    
    @Override
    protected void onDestroy() {
    	
        super.onDestroy();
    }
 
    @Override
    public void onBackPressed() {

    	this.finish();
    }
     
}