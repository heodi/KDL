package com.iceapp;

import android.app.Activity;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import android.view.ViewGroup;
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

public class CopyInvoicesActivity extends Activity {
 
	private RadioGroup rgSel;
	private RadioButton rbOldInv, rbAdvInv;
	private TextView dateTxt;
	private ListView mListView;
	private Button addBtn, cancelBtn;
	private String callFromAct, custCode;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private Intent intent;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copy_invoices);        
        initViews();
        setListensers();
        getParam();
              
        this.setTitle("½Æ»s¾P°â³æ");
    }

    private void initViews() {

    	rgSel = (RadioGroup)findViewById(R.id.rgSelect);
    	rbOldInv = (RadioButton)findViewById(R.id.rbOldInvoice);
    	rbAdvInv = (RadioButton)findViewById(R.id.rbAdvInvoice);
    	dateTxt = (TextView)findViewById(R.id.txtDate);
    	mListView = (ListView)findViewById(R.id.copyOrderList);
    	addBtn = (Button)findViewById(R.id.butAdd);
    	cancelBtn = (Button)findViewById(R.id.butCancel);
        dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    	disableButton(addBtn);
    }

    private void setListensers() {
    	rgSel.setOnCheckedChangeListener(selChange);
    	addBtn.setOnClickListener(addClick);
    	cancelBtn.setOnClickListener(cancelClick);
    }

    private RadioGroup.OnCheckedChangeListener selChange = new RadioGroup.OnCheckedChangeListener() {    			
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == R.id.rbOldInvoice)
				createInvoicesList("OLD");
			else
				createInvoicesList("ADV");
		}
	};
    private Button.OnClickListener addClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String fmDate = util.dateToStrFmt(dateTxt.getText().toString());
			if (rgSel.getCheckedRadioButtonId() == R.id.rbOldInvoice)
				returnToCallingAct("OLD", fmDate);
			else
				returnToCallingAct("ADV", fmDate); 
    	}
    };
    
    private Button.OnClickListener cancelClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		returnToCallingAct(null,null);
    	}
    };
        
    private void createInvoicesList(String iFrom) {
    	String invDate = util.getCurDate();
    	String query;
    	Cursor c;
    	
    	db = dbh.getWritableDatabase();
    	if (iFrom.equals("OLD")) {
    		query = "select "
    				+ "b.line_no as _id, "
    				+ "a.order_date, "
    				+ "c.chinese_desc, "
                    + "b.order_qty "
                    + "from sales_order_hdr a, sales_order_dtl b, item_file c "
                    + "where a.order_no = b.order_no "
                    + "and b.item_code = c.item_code "
                    + "and b.tran_code not in ('RP','PG') "
                    + "and a.order_date < ? "
                    + "and a.cust_code = ? "
                    + "and a.order_status <> 'D' "
                    + "and a.order_date in "
                    + "(select max(order_date) "
                    + "from sales_order_hdr "
                    + "where cust_code = ? "
                    + "and order_date < ? "
                    + "and order_status <> 'D') "
                    + "order by b.line_no";
    		c = db.rawQuery(query, new String[] {invDate, custCode, custCode, invDate});
    	} else {
    		query = "select "
    				+ "b.line_no as _id, "
    				+ "b.order_date, "
    				+ "c.chinese_desc, "
    				+ "b.order_qty "
    				+ "from adv_sales_order b, item_file c "
    				+ "where b.item_code = c.item_code "
    				+ "and b.tran_code not in ('RP','PG') "
    				+ "and b.cust_code = ? "
    				+ "and b.order_date in "
    				+ "(select min(order_date) "
    				+ "from adv_sales_order "
    				+ "where cust_code = ? "
    				+ "and order_date > ? )"
    				+ "order by b.line_no";
    		c = db.rawQuery(query, new String[] {custCode, custCode, invDate});
    	}

    	if (c.moveToFirst()) {
    		dateTxt.setText(util.strToDateFmt(c.getString(c.getColumnIndex("order_date")), "yyyy/MM/dd"));
    		enableButton(addBtn);
    	} else {
    		dateTxt.setText(null);
    		disableButton(addBtn);
    	}
        String[] from = new String[] {
            	"chinese_desc", 
            	"order_qty"};    			

        int[] to = new int[] {     
        		R.id.sitemDesc,
        		R.id.sqty};
        
        final SimpleCursorAdapter solist = new SimpleCursorAdapter(this,R.layout.copy_invoices_list_item, c, from, to);
        mListView.setAdapter(solist);         
    	
    	db.close();    	
    }

    private void getParam() {
    	intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	callFromAct = bundle.getString("ActName");
    	custCode= bundle.getString("cust_code");
    	
    	if (callFromAct.equals("AdvSalesActivity")) {
    		rbAdvInv.setVisibility(View.INVISIBLE);
    		rbOldInv.setChecked(true);
    		createInvoicesList("OLD");
    	}    		    		
    }
    
    private void returnToCallingAct(String iFrom, String iDate) {
    	if (callFromAct.equals("AdvSalesActivity"))
        	intent.putExtra("order_date", iDate);
    	else {
        	intent.putExtra("invoice_from", iFrom);
        	intent.putExtra("order_date", iDate);
    	}
        CopyInvoicesActivity.this.setResult(RESULT_OK, intent);
        CopyInvoicesActivity.this.finish();    		
    }
 
    private void disableButton(Button b) {
    	b.setEnabled(false);
    	b.setClickable(false);    	
    }

    private void enableButton(Button b) {
    	b.setEnabled(true);
    	b.setClickable(true);
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

    	returnToCallingAct(null,null);
    }
}