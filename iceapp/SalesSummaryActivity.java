package com.iceapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;


 
public class SalesSummaryActivity extends Activity {
 
	private TextView salesDateTxt, cashTotalTxt, cashCountTxt, creditTotalTxt, creditCountTxt;
	private TextView cancelTotalTxt, cancelCountTxt;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales_summary);        
        initViews();
      
        this.setTitle("銷售總結");
        showResults();
    }

    private void initViews() {
        salesDateTxt = (TextView)findViewById(R.id.txtSalesDate);
        cashTotalTxt = (TextView)findViewById(R.id.txtCashTotal);
        cashCountTxt = (TextView)findViewById(R.id.txtCashCount);
        creditTotalTxt = (TextView)findViewById(R.id.txtCreditTotal);
        creditCountTxt = (TextView)findViewById(R.id.txtCreditCount);
        cancelTotalTxt = (TextView)findViewById(R.id.txtCancelTotal);
        cancelCountTxt = (TextView)findViewById(R.id.txtCancelCount);
    	dbh = new DatabaseHelper(this);    	
    	util = new IceUtil();
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
    	
    	db = dbh.getWritableDatabase();
    	salesDateTxt.setText(util.strToDateFmt(util.getCurDate(), "yyyy/MM/dd"));
    	
        Cursor c = db.rawQuery("select "
        		+ "case a.pay_term "
        		+ "when '00' then '0' "
        		+ "else '1' "
        		+ "end _id, "
        		+ "case b.order_status "
        		+ "when 'D' then 'D' "
        		+ "else 'A' "
        		+ "end status, "
        		+ "case a.pay_term "
        		+ "when '00' then '現金' "
        		+ "else '月結' "
        		+ "end pay_term, "
        		+ "sum(b.total_amt) total_amt, "
        		+ "sum(1) no_of_invoice "
        		+ "from customer_file a, sales_order_hdr b "
        		+ "where a.cust_code = b.cust_code "
        		+ "and b.order_date = ? "        		
        		+ "group by "
        		+ "case b.order_status when 'D' then 'D' else 'A' end "
        		+ " ,case a.pay_term when '00' then '現金' else '月結' end "
        		+ ", case a.pay_term when '00' then '0' else '1' end",
        		new String[] { util.getCurDate() });
                		
		if (c.getCount() > 0) {
			int count = 0;
			double amt = 0;
			while (c.moveToNext()) {
				if (c.getString(c.getColumnIndex("status")).equals("A")) {
					if (c.getString(c.getColumnIndex("pay_term")).equals("現金")) {
						cashTotalTxt.setText(String.format("%.2f", c.getDouble(c.getColumnIndex("total_amt"))));
						//cashTotalTxt.setText(Double.toString(c.getDouble(c.getColumnIndex("total_amt"))));
						cashCountTxt.setText(c.getString(c.getColumnIndex("no_of_invoice")));
					}
					else {
						creditTotalTxt.setText(String.format("%.2f", c.getDouble(c.getColumnIndex("total_amt"))));
						//creditTotalTxt.setText(Double.toString(c.getDouble(c.getColumnIndex("total_amt"))));
						creditCountTxt.setText(c.getString(c.getColumnIndex("no_of_invoice")));					
					}
				} else {
					amt = amt + c.getFloat(c.getColumnIndex("total_amt"));
					count = count + c.getInt(c.getColumnIndex("no_of_invoice"));
				}
			}
			cancelTotalTxt.setText(String.format("%.2f", amt));
			cancelCountTxt.setText(Integer.toString(count));
		}
		db.close();
   } 
}