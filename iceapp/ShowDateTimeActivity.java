package com.iceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.Calendar;
import java.util.Date; 
import java.util.Locale;
import java.text.SimpleDateFormat;

public class ShowDateTimeActivity extends Activity {
 
	private TextView dateTxt, timeTxt;
	private Button okBtn;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_datetime);        
        initViews();
        setListensers();
      
        this.setTitle("系統時間");
        showDateTime();        
    }

    private void initViews() {
        dateTxt = (TextView)findViewById(R.id.txtDate);
        timeTxt = (TextView)findViewById(R.id.txtTime);
        okBtn = (Button)findViewById(R.id.butOk);
        dbh = new DatabaseHelper(this);
        util = new IceUtil();
    }

    private void setListensers() {
    	okBtn.setOnClickListener(okClick);
    }
    
    private void showDateTime() {
    	    
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日ahh時mm分ss秒", Locale.CHINA);
        String now = formatter.format(new Date());
        now.replace("AM", "上午");
        now.replace("PM", "下午");
        dateTxt.setText(now.substring(0, 11));
        timeTxt.setText(now.substring(11, 22));
        
   }
    
    private void checkDate()  {
    	Calendar sysCal = Calendar.getInstance();
    	Calendar offCal = Calendar.getInstance();
    	
    	sysCal.set(Integer.valueOf(dateTxt.getText().toString().substring(0,4)),
    			 Integer.valueOf(dateTxt.getText().toString().substring(5, 7))-1,
    			 Integer.valueOf(dateTxt.getText().toString().substring(8,10)));
    	
    	db = dbh.getWritableDatabase();
    	Cursor c = db.rawQuery("select office_date from company", null);
    	if (c.moveToFirst()) {
    		String offDateStr = c.getString(c.getColumnIndex("office_date"));
        	offCal.set(Integer.valueOf(offDateStr.substring(0,4)),
       			     Integer.valueOf(offDateStr.substring(4, 6))-1,
       			     Integer.valueOf(offDateStr.substring(6,8)));
        	
        	long fdate = sysCal.getTimeInMillis();
        	long tdate = offCal.getTimeInMillis();
        	long diff = ((fdate - tdate) % 10 == 0) ? fdate - tdate : fdate - tdate + 1;
        	long daysDiff = diff / (24 * 60 * 60 * 1000);
        	
    		if (daysDiff > 0) {    			
        		AlertDialog.Builder ab = new Builder(ShowDateTimeActivity.this);
        		ab.setTitle(R.string.dialog_title);
        		ab.setMessage("系統日期與資料庫日期相差了 " + String.valueOf(daysDiff) + " 日");
        		ab.setPositiveButton("繼續", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        				dialog.dismiss();
        				callActivity();
        			}
        		});
        		ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        				dialog.dismiss();
        			}
        		});
        		ab.show();    		    			    			
    		} else {
    			callActivity();
    		}
    	} else {
    		util.showDialog("找不到資料庫日期資料", ShowDateTimeActivity.this);
    	}
    	db.close();
    }
    
    private void callActivity() {
		Intent intent = new Intent(ShowDateTimeActivity.this, MainActivity.class);
		startActivityForResult(intent,2);    		    			    	
    }
    
    private Button.OnClickListener okClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		checkDate();
    	}    	
    };
    
    @Override
    protected void onResume() {
    	
    	super.onResume();
    	showDateTime();
    }
 
    @Override
    protected void onStop() {
        
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {

    	this.finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 2) {
        	setResult(1);
            finish();
        }
    }
         
}