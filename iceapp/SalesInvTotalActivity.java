package com.iceapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
public class SalesInvTotalActivity extends PrintInvActivity {
 
	private Button printBtn;
	private TextView orderNoTxt, invTotTxt;
    private ListView mListView;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private String orderNo;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales_inv_total);        
        initViews();
        setListensers();
        getParam();      
        this.setTitle("銷售單總計資料");
    }

    private void initViews() {

        mListView = (ListView)findViewById(R.id.salesTotalList);
        orderNoTxt = (TextView)findViewById(R.id.txtOrderNo);
        invTotTxt = (TextView)findViewById(R.id.txtInvTotal);
        printBtn = (Button)findViewById(R.id.butPrint);
        dbh = new DatabaseHelper(this);
    }


    private void setListensers() {
    	printBtn.setOnClickListener(printClick);
    }
    
    private void getParam() {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	orderNo = bundle.getString("order_no");
    	showResults();
    	createSummaryList();
    }
    
    private void showResults() {
    	String query = "select "
    			+ "total_amt "
    			+ "from sales_order_hdr "
    			+ "where order_no = ? ";
    	db = dbh.getWritableDatabase();
    	Cursor c = db.rawQuery(query, new String[] {orderNo});
    	if (c.moveToFirst()) {
    		double amt = c.getDouble(c.getColumnIndex("total_amt"));
    		orderNoTxt.setText(orderNo);
    		invTotTxt.setText(String.format("%.2f", amt));
    	}
    	db.close();
    }
    
    private void createSummaryList() {
    
        String query = "select a.rowid as _id, "
    			+ "a.item_cat, "
    			+ "sum(1) item_cnt "
    			+ "from item_file a "
    			+ ", (select distinct "
    			+ "item_code "
    			+ "from sales_order_dtl "
    			+ "where order_no = ? "
    			+ ") b "
    			+ "where a.item_code = b.item_code "
    			+ "group by "
    			+ "a.item_cat";

        db = dbh.getWritableDatabase();
        Cursor c = db.rawQuery(query, new String[] {orderNo});
    	c.moveToFirst();
    	
        String[] from = new String[] {
        		"item_cat", 
            	"item_cnt",}; 

        int[] to = new int[] {     
        		R.id.sitemCat,
        		R.id.sitemCatCnt};
        
        final SimpleCursorAdapter solist = new SimpleCursorAdapter(this,R.layout.sales_inv_total_list_item, c, from, to);
        mListView.setAdapter(solist);
        
    	db.close();
    }

    private Button.OnClickListener printClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		printInvoice(orderNo);
    		//update order_status
    		db = dbh.getWritableDatabase();
        	String query = "update sales_order_hdr "
        			+ "set order_status  = 'C' "
        			+ "where order_no = '" + orderNo + "' ";
        	db.execSQL(query);
        	db.close();
    	}    	
    };

    @Override
    protected void onResume() {
    	
    	super.onResume();
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