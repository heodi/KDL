package com.iceapp;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
 
public class RouteEnqActivity extends Activity implements SearchView.OnQueryTextListener,
SearchView.OnCloseListener{
 
	private TextView dateTxt;
	private Spinner statusSpinner;
	private SearchView searchView;
    private ListView mListView;
	private String filter, custCode, custStatus, custName;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private Cursor rcCursor;
    private int routePos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_enq);        
        initViews();
        setListensers();
      
        this.setTitle("路線資料");
        createStatusList();
    }

    private void initViews() {
    	
        mListView = (ListView)findViewById(R.id.routeList);
        dateTxt = (TextView)findViewById(R.id.textDate);
        statusSpinner = (Spinner)findViewById(R.id.spStatus);
        searchView = (SearchView)findViewById(R.id.custSearch);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        
    	dbh = new DatabaseHelper(this);
    	db = dbh.getWritableDatabase();
    	util = new IceUtil();
        dateTxt.setText(util.strToDateFmt(util.getCurDate(), "yyyy/MM/dd"));
        routePos = 0;
    }

    private void createStatusList() {
    	
    	List<String> list = new ArrayList<String>();
    	list.add("全部");
    	list.add("已去");
    	list.add("未去");
    	list.add("1天內無落單");
    	list.add("2天內無落單");
    	list.add("3天內無落單");
    	list.add("4天內無落單");
    	list.add("5天內無落單");
    	list.add("5天以上無落單");
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
    			this,android.R.layout.simple_spinner_item, list);
    	
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	statusSpinner.setAdapter(dataAdapter);
    }
    
    
    private void setListensers() {
    	statusSpinner.setOnItemSelectedListener(statusItemSelected);
    }

    private Spinner.OnItemSelectedListener statusItemSelected = new Spinner.OnItemSelectedListener() {
    	
    	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
    		
    		filter = "";
    		custCode = "";
    		custStatus = "";

    		String opt = adapterView.getItemAtPosition(position).toString();    	
    		if (opt.equals("未去")) {
    			//filter = "and a.status = 0 ";
    			filter = "and a.status = 0 ";
    		}
    		else if (opt.equals("已去")) {
    			filter = "and a.status = 1 ";
    		}
    		else if (opt.equals("1天內無落單")) {
    			//filter = "and strftime('%Y%m%d',date('now','-2 days')) = a.last_order_date ";
    			filter = "and a.last_order_date = strftime('%Y%m%d',date('now','-2 days')) ";
    		}
    		else if (opt.equals("2天內無落單")) {
    			//filter = "and strftime('%Y%m%d',date('now','-3 days')) = a.last_order_date ";
    			filter = "and a.last_order_date = strftime('%Y%m%d',date('now','-3 days')) ";
    		}
    		else if (opt.equals("3天內無落單")) {
    			//filter = "and strftime('%Y%m%d',date('now','-4 days')) = a.last_order_date ";
    			filter = "and a.last_order_date = strftime('%Y%m%d',date('now','-4 days')) ";
    		}
    		else if (opt.equals("4天內無落單")) {
    			//filter = "and strftime('%Y%m%d',date('now','-5 days')) = a.last_order_date ";
    			filter = "and a.last_order_date = strftime('%Y%m%d',date('now','-5 days')) ";
    		}
    		else if (opt.equals("5天內無落單")) {
    			//filter = "and strftime('%Y%m%d',date('now','-6 days')) = a.last_order_date ";
    			filter = "and a.last_order_date = strftime('%Y%m%d',date('now','-6 days')) ";
    		}
    		else if (opt.equals("5天以上無落單")) {
    			/**
    			filter = " and (strftime('%Y%m%d',date('now','-7 days')) <= a.last_order_date "
    					+ "or a.last_order_date is null) ";
    					*/
    			filter = "and (a.last_order_date <= strftime('%Y%m%d',date('now','-7 days')) "
    					+ "or a.last_order_date is null) ";    			
    		}
            createRouteList("");
            if (rcCursor.getCount() > 0 && opt.equals("全部"))
            	goToLastOrderNextRecord();

    	}
		public void onNothingSelected(AdapterView arg0) {
			//
		}
    };

    private void createRouteList(String keyWord) {

        String query =  "select a.rowid as _id, "
        		+ "a.delivery_seq sequence, "
        		+ "a.chinese_name chinese_name, "
        		+ "b.cust_code cust_code, "
        		+ "b.cust_group cust_group, "
        		+ "a.last_order_date last_order_date, "
        		+ "case a.status "
        		+ "when '0' then '未去' "
        		+ "when '1' then '已去' "
        		+ "when '2' then '已電' "
        		+ "when '3' then '沒空' "
        		+ "else '無落單' "
        		+ "end status "
        		+ "from route_cust a, customer_file b, pda c "
        		+ "where a.cust_code = b.cust_code "
        		+ "and b.cust_status = 'A' "
        		+ "and b.route = c.route "        		
        		+ "and c.default_route = 'Y' "
        		+ filter;        		
        
        if (!keyWord.equals("")) {
    		query = query + 
    				"and (b.cust_code like '%" + keyWord + "%' "+ 
    				"or b.chinese_name like '%" + keyWord + "%' "+
    				"or b.tel_1 like '%" + keyWord + "%' "+
    				"or b.tel_2 like '%" + keyWord + "%') ";
    	}        
        query = query + "order by a.delivery_seq, "
        		+ "case a.last_order_date "
        		+ "when '' then '99999999' "
        		+ "else a.last_order_date "
        		+ "end, a.chinese_name;";
        
    	rcCursor = db.rawQuery(query, null);
    	//Log.d("search","search query =" + query);
    	//Log.d("search","Number of record in search cursor = " + Integer.toString(rcCursor.getCount()));
    	
    	if (rcCursor != null && rcCursor.moveToFirst()) {
    		
            String[] from = new String[] {
            		"last_order_date", 
            		"chinese_name",
            		"status"};
 
            int[] to = new int[] {     
            		R.id.slastOrder,
            		R.id.scustName,
            		R.id.scustStatus};
            
            final SimpleCursorAdapter routelist = new SimpleCursorAdapter(this,R.layout.route_list_item, rcCursor, from, to);
            mListView.setAdapter(routelist);
            
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    
                	routePos = position;
                	Cursor curRec = (Cursor) mListView.getItemAtPosition(position);
                	custCode = curRec.getString(curRec.getColumnIndex("cust_code"));
                	custName = curRec.getString(curRec.getColumnIndex("chinese_name"));
                	custStatus = curRec.getString(curRec.getColumnIndex("status"));

                    //Pass data to another Activity 
                    Intent it = new Intent(RouteEnqActivity.this, RouteEnqSelActivity.class);            		
                    Bundle bundle = new Bundle();
                    bundle.putString("custCode", custCode);
                    bundle.putString("custName", custName);
                    bundle.putString("custStatus", custStatus);
                    it.putExtras(bundle);                    
                    startActivity(it);
                }
            });
        }
    }
    
    private void goToLastOrderNextRecord() {
    	String query = "select delivery_seq "
    			+ "from route_cust "
    			+ "where cust_code = "
    			+ "(select cust_code from sales_order_hdr "
    			+ "where order_no = "
    			+ "(select max(order_no) from sales_order_hdr "
    			+ "where order_date = ? ))";
        Cursor c = db.rawQuery(query, new String[] {util.getCurDate()});
        routePos = 0;
    	if (c.getCount() > 0) {        		
    		c.moveToFirst();
    		int seq = c.getInt(c.getColumnIndex("delivery_seq"));
    		rcCursor.moveToFirst();
    		while (!rcCursor.isLast() && rcCursor.getInt(rcCursor.getColumnIndex("sequence")) < seq) {
    			routePos++;
    			rcCursor.moveToNext();
    		}
    	}
 
    	if (routePos > 0)
    		mListView.setSelection(routePos);    	
    	c.close();    
    }
    
    
    @Override
    protected void onResume() {

    	super.onResume();
    	if (rcCursor != null) {
    		rcCursor.requery();
    		if (routePos > 0)
    			mListView.setSelection(routePos);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	
        super.onDestroy();
        if (dbh != null) {
            dbh.close();            
        }        
    }
 
    @Override
    public void onBackPressed() {

    	rcCursor.close();
    	db.close();
    	dbh.close();
    	this.finish();
    }

	@Override
	public boolean onClose() {
		createRouteList("");
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		//createRouteList(newText);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		createRouteList(query);
		return false;
	}
     
}