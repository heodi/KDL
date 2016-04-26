package com.iceapp;

import android.app.Activity;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class SalesEnqActivity extends Activity {
 
	private Button callCustListBtn, newBtn, searchBtn;
	private EditText custCodeTxt;
	private Spinner optSpinner, selSpinner;
    private ListView mListView;
	private String custGroup, priceBk;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private Cursor soCursor;
    private String ordStatus="所有";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales_enq);        
        initViews();
        setListensers();
              
        this.setTitle("銷售單查詢");
    }

    private void initViews() {

    	optSpinner = (Spinner)findViewById(R.id.spOption);
        mListView = (ListView)findViewById(R.id.salesOrderList);
        custCodeTxt = (EditText)findViewById(R.id.editCustCode);
        newBtn = (Button)findViewById(R.id.butNewOrder);
        searchBtn = (Button)findViewById(R.id.butSearch);
        dbh = new DatabaseHelper(this);
        db = dbh.getWritableDatabase();
    	util = new IceUtil();

    	createOptList();
    }

    private void setListensers() {
    	mListView.setOnItemClickListener(detailsClick);
    	optSpinner.setOnItemSelectedListener(optSelected);
    	newBtn.setOnClickListener(newClick);
    	searchBtn.setOnClickListener(searchClick);
    }
               
    private void showHiddenHead1() {
    	LinearLayout rHeadLayout = (LinearLayout)findViewById(R.id.rightHeading);
    	View hiddenHead = getLayoutInflater().inflate(R.layout.sales_enq_head1 , rHeadLayout, false);
    	rHeadLayout.addView(hiddenHead);
        selSpinner = (Spinner)findViewById(R.id.spSelect);
        ordStatus = "";
    	createSelList();        
    }

    private void showHiddenHead2() {
    	LinearLayout rHeadLayout = (LinearLayout)findViewById(R.id.rightHeading);
    	View hiddenHead = getLayoutInflater().inflate(R.layout.sales_enq_head2 , rHeadLayout, false);
    	rHeadLayout.addView(hiddenHead);
        custCodeTxt = (EditText)findViewById(R.id.editCustCode);
        callCustListBtn = (Button)findViewById(R.id.butCallCustList);
        callCustListBtn.setOnClickListener(callCustClick);
    }
    
    private void createOptList() {
    	List<String> list = new ArrayList<String>();
    	list.add("銷售單狀態");
    	list.add("客戶編號");
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
    			this,android.R.layout.simple_spinner_item, list);
    	
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	optSpinner.setAdapter(dataAdapter);    	
    }

    private Spinner.OnItemSelectedListener optSelected = new Spinner.OnItemSelectedListener() {
    	
    	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
    		
    		String opt = adapterView.getItemAtPosition(position).toString();
    		TextView lcol3 = (TextView)findViewById(R.id.lColTitle3);
    		if (opt.equals("銷售單狀態")) {
    			View myView = findViewById(R.id.hiddenHead2);
    			if (myView != null) {
    				ViewGroup parent = (ViewGroup) myView.getParent();
    				parent.removeView(myView);
    			}
    			showHiddenHead1();
    			lcol3.setText("狀態");
    			disableButton(newBtn);
    			disableButton(searchBtn);
    		} else {
    			View myView = findViewById(R.id.hiddenHead1);
    			if (myView != null) {
    				ViewGroup parent = (ViewGroup) myView.getParent();
    				parent.removeView(myView);
    			}
    			showHiddenHead2();
    			lcol3.setText("付款式");
    			enableButton(newBtn);
    			enableButton(searchBtn);
    		}
    		createSalesList();
    	}
		public void onNothingSelected(AdapterView arg0) {}
    };
    
    private void createSelList() {
    	
    	List<String> list = new ArrayList<String>();
    	list.add("所有");
    	list.add("新單");
    	list.add("確認");
    	list.add("取消");
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
    			this,android.R.layout.simple_spinner_item, list);
    	
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	selSpinner.setAdapter(dataAdapter);
    	
    	selSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
    		
    		@Override
    		public void onItemSelected(AdapterView parent, View view, int position, long id) { 
    	        ordStatus = parent.getItemAtPosition(position).toString();
    	        createSalesList();
    	    }
    		
    		@Override
			public void onNothingSelected(AdapterView parent) {}    		
    	});
    }
    
    private Button.OnClickListener callCustClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		Intent it = new Intent(SalesEnqActivity.this, CustEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "SalesEnq");
    		it.putExtras(bundle);    		
    		startActivityForResult(it, 0);
    	}
    };
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Bundle bundle = data.getExtras();
    	if (requestCode == 0) {
    		String cust_code = bundle.getString("cust_code");
    		if (cust_code.length() > 0) {
        		custCodeTxt.setText(cust_code);    			
    		}
    	}
    }
        
    private void createSalesList() {
    
    	String payStatus;
    	String filter = "";
        String query = "select a.rowid as _id, "
        		+ "a.order_no, "
        		+ "a.cust_code, "
        		+ "b.chinese_name, "
        		+ "b.cust_group, "
        		+ "b.price_book_code, "
        		+ "case ? "
        		+ "when 'S' then "
        		+ "case a.order_status "
        		+ "when 'N' then '新單' "
        		+ "when 'C' then '確認' "
        		+ "else '取消' "
        		+ "end "
        		+ "else b.pay_term "
        		+ "end pay_status "
        		+ "from sales_order_hdr a, customer_file b "
        		+ "where a.cust_code = b.cust_code "
        		+ "and a.order_date = ? ";

        if (optSpinner.getSelectedItem().toString().equals("銷售單狀態"))  {
        	payStatus = "S";
        	if (ordStatus.equals("新單"))
        		filter = "and a.order_status = 'N' ";
        	else if (ordStatus.equals("確認"))
        		filter = "and a.order_status = 'C' ";
        	else if (ordStatus.equals("取消"))
        		filter = "and a.order_status = 'D' ";
        } else {
        	payStatus = "P";
        	if (!custCodeTxt.getText().equals(""))
        		filter = "and a.cust_code like '"
        				+ custCodeTxt.getText() + "%' ";
        	filter = filter + "and a.order_status <> 'D' ";
        }
        query = query + filter + "order by a.order_no desc ";
        
        //db = dbh.getWritableDatabase();
    	soCursor = db.rawQuery(query, new String[] {payStatus, util.getCurDate()});
    	soCursor.moveToFirst();
    	
        String[] from = new String[] {
        		"order_no", 
            	"chinese_name", 
            	"pay_status"};    			

        int[] to = new int[] {     
        		R.id.sorderNo,
        		R.id.schineseName,
        		R.id.spayStatus};
        
        final SimpleCursorAdapter solist = new SimpleCursorAdapter(this,R.layout.sales_enq_list_item, soCursor, from, to);
        mListView.setAdapter(solist);
        
    	//db.close();
    }

    private ListView.OnItemClickListener detailsClick = new ListView.OnItemClickListener() {
    	@Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	//routePos = position;
        	Cursor curRec = (Cursor) mListView.getItemAtPosition(position);
        	String orderNo = curRec.getString(curRec.getColumnIndex("order_no"));
        	
        	//Pass data to another Activity 
        	Intent it = new Intent(SalesEnqActivity.this, SalesDetailsActivity.class);
        	Bundle bundle = new Bundle();
        	bundle.putString("numOrcode", "order_no");
        	bundle.putString("value", orderNo);
        	it.putExtras(bundle);
        	startActivity(it);
        }    	
    };
    
    private Button.OnClickListener newClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String custCode = custCodeTxt.getText().toString();
    		if (custCode.equals(""))
    			util.showDialog("請輸入客戶編號", SalesEnqActivity.this);
    		else {
    			String query = "select a.cust_code, "
    					+ "c.order_no "
    					+ "from customer_file a "
    					+ "INNER JOIN pda b "
    					+ "on a.route = b.route "
    					+ "and b.default_route = 'Y' "
    					+ "LEFT OUTER JOIN sales_order_hdr c "
    					+ "on a.cust_code = c.cust_code "
    					+ "and c.order_date = ? "
    					+ "and c.order_status <> 'D' "
    					+ "where a.cust_code = ? ";
    			Cursor c = db.rawQuery(query, new String[] {util.getCurDate(), custCode}); 
    			if (!c.moveToFirst())
    				util.showDialog("客戶編號不正確!", SalesEnqActivity.this);
    			else {
    				if (c.getString(c.getColumnIndex("order_no")) != null)    				
    					util.showDialog("此客戶已經落單", SalesEnqActivity.this);
    				else {
        	        	//Pass data to another Activity 
        	        	Intent it = new Intent(SalesEnqActivity.this, SalesDetailsActivity.class);
        	        	Bundle bundle = new Bundle();
        	        	bundle.putString("numOrcode", "cust_code");
        	        	bundle.putString("value", custCode);
        	        	it.putExtras(bundle);
        	        	startActivity(it);    				    					
    				}
    			}
    		}
    	}    	
    };

    private Button.OnClickListener searchClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		createSalesList();
    	}    	
    };
    
    private void disableButton(Button b) {
    	b.setEnabled(false);
    	b.setClickable(false);    	
    }

    private void enableButton(Button b) {
    	b.setEnabled(true);
    	b.setClickable(true);
    } 
    
    @Override
    protected void onResume() {
    	
    	super.onResume();
    	if (soCursor != null) {
        	//db = dbh.getWritableDatabase();
        	soCursor.requery();
        	//db.close();
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

    	soCursor.close();
    	db.close();
    	dbh.close();
    	this.finish();
    }
}