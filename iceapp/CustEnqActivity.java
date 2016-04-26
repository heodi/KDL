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
 
public class CustEnqActivity extends Activity implements SearchView.OnQueryTextListener,
SearchView.OnCloseListener {
 
    private ListView mListView;
    private SearchView searchView;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private String callFromAct;
    private Intent intent;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cust_enq);
        initViews();
        getParam();

        this.setTitle("·j´M«È¤á");
        showResults("");
    }
    
    private void initViews() {
    	
        mListView = (ListView)findViewById(R.id.custList);
        searchView = (SearchView)findViewById(R.id.custSearch);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
                 
    	dbh = new DatabaseHelper(this);
    }
 
    private void getParam() {
    	intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	callFromAct = bundle.getString("ActName");
    }
    
    public boolean onQueryTextChange(String newText) {
    	
        return false;        
    }
 
    public boolean onQueryTextSubmit(String query) {
    	
        showResults(query);
        return false;
    }
 
    public boolean onClose() {
    	
        showResults("");
        return false;
    }
 
    @Override
    public void onBackPressed() {
    	
        if (callFromAct.equals("Main")) {
        	this.finish();        	
        }                                                            
        else {
        	returnToCallingAct("");
        }
    }
    
    private void returnToCallingAct(String cust_code) {
    	
    	intent.putExtra("cust_code", cust_code);
    	CustEnqActivity.this.setResult(RESULT_OK, intent);
    	CustEnqActivity.this.finish();
    }
    
    public Cursor searchCustByInputText(String inputText) throws SQLException {

    	String query = "SELECT "
    			+ "a.cust_code as _id, "
    			+ "a.cust_code, "
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
    			+ "where b.default_route = 'Y' "
    			+ "and a.route = b.route "
    			+ "and a.cust_code = c.cust_code ";
    	if (!inputText.equals("")) {
    		query = query + 
    				"and (a.cust_code like '%" + inputText + "%' "+ 
    				"or a.chinese_name like '%" + inputText + "%' "+
    				"or a.tel_1 like '%" + inputText + "%' "+
    				"or a.tel_2 like '%" + inputText + "%') ";
    	}
    	query = query + "order by a.chinese_name;";
    	
    	db = dbh.getWritableDatabase();
        Cursor mCursor = db.rawQuery(query,null);       
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        db.close();
        return mCursor;
    }
    
    private void showResults(String query) {
    	
    	Cursor cursor = searchCustByInputText(query);
    	
        if (cursor == null) {
            //
        } else {
            // Specify the columns we want to display in the result
            String[] from = new String[] {
            		"cust_code", 
            		"chinese_name"};
 
            // Specify the Corresponding layout elements where we want the columns to go
            int[] to = new int[] {     
            		R.id.scust_code,
            		R.id.schinese_name};
           
            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter customers = new SimpleCursorAdapter(this,R.layout.cust_list_item, cursor, from, to);
            mListView.setAdapter(customers);
  
            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	
                    Cursor c = (Cursor) mListView.getItemAtPosition(position);
                    String cust_code = c.getString(c.getColumnIndex("cust_code"));
                    
                    if (callFromAct.equals("Main")) {                    
                    	String pay_term = c.getString(c.getColumnIndex("pay_term"));
                    	String chinese_name = c.getString(c.getColumnIndex("chinese_name"));
                    	String english_name = c.getString(c.getColumnIndex("english_name"));
                    	String address_1 = c.getString(c.getColumnIndex("address_1"));
                    	String address_2 = c.getString(c.getColumnIndex("address_2"));
                    	String address_3 = c.getString(c.getColumnIndex("address_3"));
                    	String address_4 = c.getString(c.getColumnIndex("address_4"));
                    	String contact = c.getString(c.getColumnIndex("contact"));
                    	String tel_1 = c.getString(c.getColumnIndex("tel_1"));
                    	String tel_2 = c.getString(c.getColumnIndex("tel_2"));
                    	String last_order_date = c.getString(c.getColumnIndex("last_order_date"));
                    	String cust_group = c.getString(c.getColumnIndex("cust_group"));
                    
                    	//Pass data to another Activity 
                    	Intent it = new Intent(CustEnqActivity.this, CustEnqDetailsActivity.class);            		
                    	Bundle bundle = new Bundle();
                    	bundle.putString("cust_code", cust_code);
                    	bundle.putString("pay_term", pay_term);
                    	bundle.putString("chinese_name", chinese_name);
                    	bundle.putString("english_name", english_name);
                    	bundle.putString("address_1", address_1);
                    	bundle.putString("address_2", address_2);
                    	bundle.putString("address_3", address_3);
                    	bundle.putString("address_4", address_4);
                    	bundle.putString("contact", contact);
                    	bundle.putString("tel_1", tel_1);
                    	bundle.putString("tel_2", tel_2);
                    	bundle.putString("last_order_date", last_order_date);
                    	bundle.putString("cust_group", cust_group);
                    	it.putExtras(bundle);                    
                    	startActivity(it);            		
                    }
                    else {
                    	
                    	returnToCallingAct(cust_code);
                    }                                        
                    //searchView.setQuery("",true);
                }
            });            
        }
    }
    
    @Override
    protected void onStop(){
    	
    	
    	
    	super.onStop();
    }
    
    
    @Override
    protected void onDestroy() {
    	
        super.onDestroy();
    }     
}