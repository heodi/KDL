package com.iceapp;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.WindowManager;
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
 
public class ProductEnqActivity extends Activity implements SearchView.OnQueryTextListener,
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
        setContentView(R.layout.product_enq);
        initViews();
        getParam();

        this.setTitle("·j´M³fª«");
        showResults("");
    }
    
    private void initViews() {
    	
        mListView = (ListView)findViewById(R.id.itemList);
        searchView = (SearchView)findViewById(R.id.itemSearch);
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

    @Override
    protected void onDestroy() {
    	
        super.onDestroy();
    }
 
    public boolean onQueryTextChange(String newText) {
    	
        showResults(newText);
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
        	db.close();
        	this.finish();        	
        }                                                            
        else {
        	db.close();
        	returnToCallingAct("");
        }    	
    }
    
    private void returnToCallingAct(String item_code) {
    	
    	intent.putExtra("item_code", item_code);
    	ProductEnqActivity.this.setResult(RESULT_OK, intent);
    	ProductEnqActivity.this.finish();
    }
    
    public Cursor searchCustByInputText(String inputText) throws SQLException {

    	String query = "SELECT "
    			+ "a.item_code as _id, "
    			+ "a.item_code, "
    			+ "a.chinese_desc, "
    			+ "b.opening_qty - b.sales_qty stockqty "
    			+ "from item_file a, truck_open b, pda c "
    			+ "where c.default_route = 'Y' "
    			+ "and c.route = b.route "
    			+ "and b.item_code = a.item_code ";
    	if (!inputText.equals("")) {
    		query = query + "and item_file MATCH '" + inputText + "*' ";
    	}
    	query = query + "order by chinese_desc;";

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
            		"item_code", 
            		"chinese_desc",
            		"stockqty"};
 
            // Specify the Corresponding layout elements where we want the columns to go
            int[] to = new int[] {     
            		R.id.sitem_code,
            		R.id.sitem_cdesc,
            		R.id.sonhand_qty};
           
            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter items = new SimpleCursorAdapter(this,R.layout.product_list_item, cursor, from, to);
            mListView.setAdapter(items);
             
            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) mListView.getItemAtPosition(position);
                	String item_code = cursor.getString(cursor.getColumnIndexOrThrow("item_code"));
                    
                    if (callFromAct.equals("ProductPrice")) {
                    	returnToCallingAct(item_code);
                    }                                                            
                    //searchView.setQuery("",true);
                }                                
            });            
        }
    } 
}