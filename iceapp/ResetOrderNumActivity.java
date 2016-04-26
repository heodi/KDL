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

public class ResetOrderNumActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_order_num);
        this.setTitle("重設銷售單號");
        initViews();
        setListensers();
        loadData();
    }

    private Button saveBtn;
    private EditText prefixTxt, lastOrderNumTxt;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;

    private void initViews() {
    	prefixTxt = (EditText)findViewById(R.id.editPrefix);
    	lastOrderNumTxt = (EditText)findViewById(R.id.editLastOrderNum);
    	saveBtn = (Button)findViewById(R.id.butSave);
    	util = new IceUtil();
    	dbh = new DatabaseHelper(this);
    }
    
    private void setListensers() {
    	saveBtn.setOnClickListener(saveClick);    	
    }
    
    private void loadData() {
    	String query = "select prefix, "
    			+ "last_seq_no "
    			+ "from sales_order_num ";
    	
    	db = dbh.getWritableDatabase();
    	Cursor c = db.rawQuery(query, null);
    	if (c.moveToFirst()) {
    		prefixTxt.setText(c.getString(c.getColumnIndex("prefix")));
    		lastOrderNumTxt.setText(c.getString(c.getColumnIndex("last_seq_no")));    		
    	}
    	db.close();
    }
    
    private Button.OnClickListener saveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String prefix = prefixTxt.getText().toString();
    		String lastOrderNum = lastOrderNumTxt.getText().toString();
    		if (prefix.equals("")) {
    			util.showDialog("Prefix must be entered.", ResetOrderNumActivity.this);
    			prefixTxt.requestFocus();
    		}
    		else if (lastOrderNum.equals("")) {
    			util.showDialog("Last Order No. must be entered.", ResetOrderNumActivity.this);
    			lastOrderNumTxt.requestFocus();
    		}
    		else {
    			db = dbh.getWritableDatabase();
    			String query = "select order_no "
    					+ "from sales_order_hdr "
    					+ "where substr(order_no, 1,2) = ? "
    					+ "and substr(order_no,3) > ?";
    			Cursor c = db.rawQuery(query, new String[] {prefix, lastOrderNum});
    					    				
        		if (c.moveToFirst()) {
        			util.showDialog(c.getString(c.getColumnIndex("order_no")), ResetOrderNumActivity.this);
        			util.showDialog("已設定之銷售單號小於現有之銷售單號，請重新設定", ResetOrderNumActivity.this);
        		} else {
        			//remove the original record
        			db.delete("sales_order_num", null, null);
        			
        			//insert a new record
        			ContentValues cv = new ContentValues();
        			cv.put("prefix", prefix);
        			cv.put("last_seq_no", Integer.parseInt(lastOrderNum));
        			
        			db.insert("sales_order_num", null, cv);

    				Toast toast = Toast.makeText(ResetOrderNumActivity.this, "已修改", Toast.LENGTH_SHORT);				
    				toast.show();    					    			        			
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
