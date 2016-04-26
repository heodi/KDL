package com.iceapp;

import android.app.AlertDialog;
import android.content.Context;
import android.app.Activity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.net.ParseException;
import android.util.Log;
import android.widget.EditText;

public class IceUtil {
	private boolean result;
	
	public void showDialog(String msg, Activity act) {
		AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(act);
		MyAlertDialog.setTitle(R.string.dialog_title);
		MyAlertDialog.setMessage(msg);
		MyAlertDialog.setCancelable(false);
		MyAlertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		MyAlertDialog.show();
	}

	public String strToDateFmt(String dateInStr, String dateFmt) {
	
		String strDate = "";		
		Date myDate = null;
		
		if (dateInStr != null) {
			
			SimpleDateFormat fmDateFormat = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat toDateFormat = new SimpleDateFormat(dateFmt);			
			//SimpleDateFormat toDateFormat = new SimpleDateFormat("yyyy/MM/dd");
			try {
				myDate = fmDateFormat.parse(dateInStr);
				strDate = toDateFormat.format(myDate);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
				
		return strDate;		
	}

	public String dateToStrFmt(String dateInDate) {
		
		String strDate = "";		
		Date myDate = null;
		
		if (dateInDate != null) {
			
			SimpleDateFormat fmDateFormat = new SimpleDateFormat("yyyy/MM/dd");
			SimpleDateFormat toDateFormat = new SimpleDateFormat("yyyyMMdd");
			try {
				myDate = fmDateFormat.parse(dateInDate);
				strDate = toDateFormat.format(myDate);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
				
		return strDate;		
	}
	
	public String getCurDate() {
		
		SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyyMMdd");
		String strDate ="";
		
		Calendar c = Calendar.getInstance();
		strDate = dateFormat.format(c.getTime());
		
		return strDate;		
	}		

	public String getCurDateTime() {
		
		//SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyyMMddHHmmss");
		String strDate ="";
		
		Calendar c = Calendar.getInstance();
		strDate = dateFormat.format(c.getTime());
		
		return strDate;		
	}
	
	public String getDateStr(Calendar iCalendar) {
		SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyyMMdd");
		String strDate ="";
		
		strDate = dateFormat.format(iCalendar.getTime());
		
		return strDate;				
	}
	
	public String getUprice(Context iContext,
			DatabaseHelper iDbh, SQLiteDatabase iDb,  
			String iPriceBk, String iItem, String iDate, String iQty) {
		float price = 0;
		boolean dbNowOpen = false;
		String query;
		Cursor c;

		DatabaseHelper dbh = iDbh;
		SQLiteDatabase db;
		
		if (iDb.isOpen()) {
			db = iDb;
		}
		else {
			dbNowOpen = true;
			db = dbh.getWritableDatabase();
		}

		query = "select "
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
				+ "group by a.price_book_code, a.item_code, a.qty_level) "
				+ "where qty_level <= ?"
				+ "order by qty_level desc";
        
        c = db.rawQuery(query, new String[] {iDate, iPriceBk, iItem, iQty});
        if (c.moveToFirst())
        	price = c.getFloat(c.getColumnIndex("unit_price"));        
        else {
    		query = "select "
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
    				+ "group by a.price_book_code, a.item_code, a.qty_level) "
    				+ "where qty_level <= ?"
    				+ "order by qty_level desc";
        	            
            c = db.rawQuery(query, new String[] {iDate, iItem, iQty});
            if (c.moveToFirst())
            	price = c.getFloat(c.getColumnIndex("unit_price"));
        }
        if (dbNowOpen)
        	dbh.close();
        return String.valueOf(price);				        
	}
	
	public List<List<String>> getPromoItemQty(Context iContext, 
			DatabaseHelper iDbh, SQLiteDatabase iDb,  
			String iCustCode, String iCustGroup, 
			String iDate, String iItem, int iNsQty) {
		Cursor c;
		String query;
		List<List<String>> pgList = new ArrayList<List<String>>();

		DatabaseHelper dbh = iDbh;
		SQLiteDatabase db = iDb;
				
        query = "select "
        		+ "c.buy_qty, "
        		+ "d.get_item_code, "
        		+ "d.get_qty "
        		+ "from promo_for_cust a, promo_header b, promo_buy c, promo_get d "
        		+ "where a.cust_code = ? "
        		+ "and a.promo_code = b.promo_code "
        		+ "and a.promo_code = c.promo_code "
        		+ "and a.promo_code = d.promo_code "
        		+ "and ? between b.promo_date_from and b.promo_date_to "
        		+ "and c.buy_item_code = ? "
        		+ "order by c.buy_qty desc";
        
        c = db.rawQuery(query, new String[] {iCustCode, iDate, iItem});
        if (c.moveToFirst())
        	pgList = getPromo(c, iNsQty);
        else {
            query = "select "
            		+ "c.buy_qty, "
            		+ "d.get_item_code, "
            		+ "d.get_qty "
            		+ "from promo_for_group a, promo_header b, promo_buy c, promo_get d "
            		+ "where a.cust_group = ? "
            		+ "and ? between b.promo_date_from and b.promo_date_to "
            		+ "and a.promo_code = b.promo_code "
            		+ "and a.promo_code = c.promo_code "
            		+ "and a.promo_code = d.promo_code "
            		+ "and c.buy_item_code = ? "
            		+ "and a.promo_code not in "
            		+ "(select promo_code from promo_exception "
            		+ "where cust_code = ? "
            		+ "and cust_group = ?) "
            		+ "order by c.buy_qty desc";
            
        	c = db.rawQuery(query, new String[] {iCustGroup, iDate, iItem, iCustCode, iCustGroup});
        	if (c.moveToFirst())
        		pgList = getPromo(c, iNsQty);
        }
        return pgList;
	}
	
	public List<List<String>> getPromo(Cursor iCursor, int iNsQty) {
		int nsQty = iNsQty, pgQty = 0, rows = 0;
		String getItem = null;
		Cursor c = iCursor;
		List<List<String>> pgList = new ArrayList<List<String>>();

        while (nsQty > 0) {
        	getItem = null;
            int buyQty = 0, getQty = 0;
            c.moveToFirst();
            do {
            	if (nsQty >= c.getInt(c.getColumnIndex("buy_qty"))) {
            		getItem = c.getString(c.getColumnIndex("get_item_code"));
            		buyQty = c.getInt(c.getColumnIndex("buy_qty"));
            		getQty = c.getInt(c.getColumnIndex("get_qty"));
            		break;
            	}            	
            } while (c.moveToNext());
            if (getQty > 0) {
            	boolean inserted = false;
            	nsQty = nsQty - buyQty;            	
        		for (int i=0;i<pgList.size();i++) {
        			if (pgList.get(i).get(0).equals(getItem)) {            				
        				pgQty = Integer.valueOf(pgList.get(i).get(1));
        				pgQty = pgQty + getQty;
        				pgList.get(i).set(1, String.valueOf(pgQty));
        				inserted = true;
        				break;
        			}
        		}
            	if (!inserted) {
            		rows++;
            		List<String> itemQty = new ArrayList<String>();
            		pgList.add(itemQty);
            		itemQty.add(getItem);
            		itemQty.add(String.valueOf(getQty));
            	}
            } else
            	break;
        }
        return pgList;
	}
		
	////////////////////////////////////////////////////
	//    To delete the entire file directory         //
	//    (including fileOrDirectory)                 //
	////////////////////////////////////////////////////

	void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()){
			for (File child : fileOrDirectory.listFiles()){
				DeleteRecursive(child);
			}	
		}
		
		fileOrDirectory.delete();
	}

	////////////////////////////////////////////////////
	// To check whether EditText view is empty or not //
	////////////////////////////////////////////////////

	boolean EditTextisEmpty(EditText etText) {
		if (etText.getText().toString().trim().length() > 0) {
			return false;
		} else {
			return true;
		}
	}

	////////////////////////////////////////////////////
	//     To concatenate string a and string b       //
	////////////////////////////////////////////////////

	public String[] concat(String[] a, String[] b) {
		int aLen = a.length;
		int bLen = b.length;
		String[] c= new String[aLen+bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}
	
	
}
