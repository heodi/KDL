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
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
 
public class ProductPriceEnqActivity extends Activity {
 
	private Button callCustListBtn, callItemListBtn, priceRetrieveBtn, promoBtn;
	private EditText custCodeTxt, itemCodeTxt;
	private String custGroup, priceBk;
    private ListView mListView;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_price_enq);        
        initViews();
        setListensers();
        getParam();        
              
        this.setTitle("貨物價表");
    }

    private void initViews() {
        mListView = (ListView)findViewById(R.id.priceList);
        custCodeTxt = (EditText)findViewById(R.id.editCustCode);
        callCustListBtn = (Button)findViewById(R.id.butCallCustList);
        itemCodeTxt = (EditText)findViewById(R.id.editItemCode);
        callItemListBtn = (Button)findViewById(R.id.butCallItemList);
        priceRetrieveBtn = (Button)findViewById(R.id.butPriceRetrieve);
        promoBtn = (Button)findViewById(R.id.butPromo);
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    }
    
    private void getParam() {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	String callFromAct = bundle.getString("ActName");
    	
    	if (callFromAct.equals("RouteEnqSel")) {
    		custCodeTxt.setText(bundle.getString("cust_code"));
    	}
    	
    }
        
    private void setListensers() {
    	callCustListBtn.setOnClickListener(callCustClick);
    	callItemListBtn.setOnClickListener(callItemClick);
    	priceRetrieveBtn.setOnClickListener(priceRetrieveClick);
    	promoBtn.setOnClickListener(promoClick);
    }
    
    private Button.OnClickListener callCustClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		Intent it = new Intent(ProductPriceEnqActivity.this, CustEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "ProductPrice");
    		it.putExtras(bundle);    		
    		startActivityForResult(it, 0);
    	}
    };
    
    private Button.OnClickListener callItemClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		Intent it = new Intent(ProductPriceEnqActivity.this, ProductEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "ProductPrice");
    		it.putExtras(bundle);    		
    		startActivityForResult(it, 1);
    	}
    };
    
    private Button.OnClickListener priceRetrieveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		boolean valid = validate();
    		if (valid) {
    			showResults();
    		}
    	}
    };
    
    private Button.OnClickListener promoClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
        	//Pass data to another Activity
    		boolean valid = validate();
    		if (valid) {
            	Intent it = new Intent(ProductPriceEnqActivity.this, ProductPromoEnqActivity.class);
            	Bundle bundle = new Bundle();
            	bundle.putString("cust_code", custCodeTxt.getText().toString());
            	bundle.putString("cust_group", custGroup);
            	bundle.putString("item_code", itemCodeTxt.getText().toString());
            	it.putExtras(bundle);
            	startActivity(it);            		    			
    		}
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
    	if (requestCode == 1) {
    		String item_code = bundle.getString("item_code");
    		if (item_code.length() > 0) {
        		itemCodeTxt.setText(item_code);    			
    		}
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
    
    private boolean validate() {
    	
		String custCode = custCodeTxt.getText().toString();
		String itemCode = itemCodeTxt.getText().toString();
		Cursor c;
		boolean valid = true;
		
		db = dbh.getWritableDatabase();
		if (custCode.equals("")) {
			util.showDialog("請輸入客戶編號", ProductPriceEnqActivity.this);
			custCodeTxt.requestFocus();
			valid = false;
		} else {			
			c = db.rawQuery("select cust_group, price_book_code from customer_file "
					+ "where cust_code = ?", new String[]{custCode});
			if (c.getCount() <= 0) {
				util.showDialog("客戶編號不正確", ProductPriceEnqActivity.this);
				custCodeTxt.requestFocus();
				valid = false;
			} else {
				c.moveToFirst();
				custGroup = c.getString(c.getColumnIndex("cust_group"));
				priceBk = c.getString(c.getColumnIndex("price_book_code"));
			}
		}
		
		if (itemCode.equals("")) {
			util.showDialog("請輸入貨物編號", ProductPriceEnqActivity.this);
			itemCodeTxt.requestFocus();
			valid = false;
		} else {
			c = db.rawQuery("select item_code from item_file "
					+ "where item_code = ?", new String[]{itemCode});
			if (c.getCount() <= 0) {
				util.showDialog("貨物編號不正確", ProductPriceEnqActivity.this);
				itemCodeTxt.requestFocus();
				valid = false;
			}
		}
		db.close();
		
		return valid;
    }
 
    private void showResults() {
    	
    	db = dbh.getWritableDatabase();
    	/**
		Cursor c = db.rawQuery("select rowid as _id, "
				+ "qty_level, "
				+ "unit_price "
				+ "from price_book "
				+ "where price_book_code = ? "
				+ "and item_code = ? "
				+ "and effective_date <= ? "
				+ "order by qty_level asc, "
				+ "effective_date desc, "
				+ "priority asc", new String[] {priceBk, itemCodeTxt.getText().toString(), util.getCurDate() });
		if (c.getCount() == 0) {
			c = db.rawQuery("select rowid as _id, "
					+ "qty_level, unit_price "
					+ "from price_book "
					+ "where price_book_code = 'STD' "
					+ "and item_code = ? "
					+ "and effective_date <= ? "
					+ "order by qty_level asc, "
					+ "effective_date desc, "
					+ "priority asc", new String[] {itemCodeTxt.getText().toString(), util.getCurDate() });
		}				
		*/
		Cursor c = db.rawQuery("select rowid as _id, "
				+ "qty_level, "
				+ "unit_price "
				+ "from "
				+ "(select a.qty_level, "
				+ "(select b.unit_price "
				+ "from price_book b "
				+ "where b.price_book_code = a.price_book_code "
				+ "and b.item_code = a.item_code "
				+ "and b.qty_level = a.qty_level "
				+ "and b.effective_date = "
				+ "(select max(c.effective_date) "
				+ "from price_book c "
				+ "where c.price_book_code = b.price_book_code "
				+ "and c.item_code = b.item_code "
				+ "and c.qty_level = b.qty_level "
				+ "and c.effective_date <= ?) "
				+ ") unit_price "
				+ "from price_book a "
				+ "where a.price_book_code = ? "
				+ "and a.item_code = ? "
				+ "group by a.price_book_code, a.item_code, a.qty_level)"
				+ "order by qty_level", new String[] {util.getCurDate(), priceBk, itemCodeTxt.getText().toString()});
		if (c.getCount() == 0) {
			c = db.rawQuery("select rowid as _id, "
					+ "qty_level, "
					+ "unit_price "
					+ "from "
					+ "(select a.qty_level, "
					+ "(select b.unit_price "
					+ "from price_book b "
					+ "where b.price_book_code = a.price_book_code "
					+ "and b.item_code = a.item_code "
					+ "and b.qty_level = a.qty_level "
					+ "and b.effective_date = "
					+ "(select max(c.effective_date) "
					+ "from price_book c "
					+ "where c.price_book_code = b.price_book_code "
					+ "and c.item_code = b.item_code "
					+ "and c.qty_level = b.qty_level "
					+ "and c.effective_date <= ?) "
					+ ") unit_price "
					+ "from price_book a "
					+ "where a.price_book_code = 'STD' "
					+ "and a.item_code = ? "
					+ "group by a.price_book_code, a.item_code, a.qty_level)"
					+ "order by qty_level", new String[] {util.getCurDate(), itemCodeTxt.getText().toString()});
		}
		if (c.getCount() == 0) {
			util.showDialog("找不到相關貨物價格資料", ProductPriceEnqActivity.this);
		}
		db.close();
		
        // Specify the columns we want to display in the result
        String[] from = new String[] {
         		"qty_level", 
           		"unit_price"};
 
        // Specify the Corresponding layout elements where we want the columns to go
        int[] to = new int[] {     
           		R.id.sqty_level,
           		R.id.sprice};
           
        // Create a simple cursor adapter for the definitions and apply them to the ListView
        SimpleCursorAdapter items = new SimpleCursorAdapter(this,R.layout.product_price_list_item, c, from, to);
        mListView.setAdapter(items);
 
   } 
}