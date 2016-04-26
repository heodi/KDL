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
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
 
public class ProductPromoEnqActivity extends Activity {
 
	private TextView custCodeTxt, itemCodeTxt;
	private String custGroup;
    private ListView mListView;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_promo_enq);        
        initViews();
      
        this.setTitle("貨物推廣表");
        showResults();
    }

    private void initViews() {
        mListView = (ListView)findViewById(R.id.promoList);
        custCodeTxt = (TextView)findViewById(R.id.txtCustCode);
        itemCodeTxt = (TextView)findViewById(R.id.txtItemCode);
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    }

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
     
    private void showResults() {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	custCodeTxt.setText(bundle.getString("cust_code"));
    	custGroup = bundle.getString("cust_group");
    	itemCodeTxt.setText(bundle.getString("item_code"));
   	    	
    	db = dbh.getWritableDatabase();
		Cursor c = db.rawQuery("select a.rowid as _id, "
				+ "c.buy_qty buy_qty, "
				+ "d.get_item_code get_item_code, "
				+ "d.get_qty get_qty "
				+ "from promo_for_cust a, promo_header b, promo_buy c, promo_get d "
				+ "where a.cust_code = ? "
				+ "and a.promo_code = b.promo_code "
				+ "and ? between b.promo_date_from and b.promo_date_to "
				+ "and a.promo_code = c.promo_code "
				+ "and a.promo_code = d.promo_code "
				+ "and c.buy_item_code = ? "
				+ "order by c.buy_qty", new String[] {custCodeTxt.getText().toString(), 
						util.getCurDate(), itemCodeTxt.getText().toString() });  		
		
		if (c.getCount() == 0) {
			c = db.rawQuery("select a.rowid as _id, "
					+ "c.buy_qty buy_qty, "
					+ "d.get_item_code get_item_code, "
					+ "d.get_qty get_qty "
					+ "from promo_for_group a, promo_header b, promo_buy c, promo_get d "
					+ "where a.cust_group = ? "
					+ "and a.promo_code = b.promo_code "
					+ "and ? between b.promo_date_from and b.promo_date_to "
					+ "and a.promo_code = c.promo_code "
					+ "and a.promo_code = d.promo_code "
					+ "and c.buy_item_code = ? "
					+ "and a.promo_code not in "
					+ "(select promo_code from promo_exception "
					+ "where cust_code = ? "
					+ "and cust_group = ?) "
					+ "order by c.buy_qty", new String[] {custGroup,  
							util.getCurDate(), itemCodeTxt.getText().toString(), 
							custCodeTxt.getText().toString(), custGroup });
		}
		if (c.getCount() == 0) {
			util.showDialog("找不到相關貨物推廣資料", ProductPromoEnqActivity.this);
		}
		db.close();
		
        // Specify the columns we want to display in the result
        String[] from = new String[] {
         		"buy_qty", 
           		"get_item_code", 
           		"get_qty"};
 
        // Specify the Corresponding layout elements where we want the columns to go
        int[] to = new int[] {     
           		R.id.sbuy_qty,
           		R.id.sget_item, 
           		R.id.sget_qty};
           
        // Create a simple cursor adapter for the definitions and apply them to the ListView
        SimpleCursorAdapter items = new SimpleCursorAdapter(this,R.layout.product_promo_list_item, c, from, to);
        mListView.setAdapter(items);

    } 
}