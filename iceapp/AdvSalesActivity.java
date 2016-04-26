package com.iceapp;

import android.app.Activity;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import android.app.DatePickerDialog; 
import android.widget.DatePicker;

import java.util.Calendar;

import android.text.Editable;
import android.text.TextWatcher;
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

public class AdvSalesActivity extends Activity {
 
	private static final String ACT_TITLE = "預售單詳情";
	
	private Button changeDateBtn, saveBtn, modifyBtn, delBtn, itemAddBtn, copyBtn;
	private EditText custRefNoTxt, qtyTxt;
	private TextView orderDateTxt, custNameTxt, itemDescTxt, preQtyTxt;
	private Spinner tcSpinner;
    private ListView mListView;
    private ArrayList<HashMap<String, String>> advAList;
	private String custCode, minDate, custGroup;
	private boolean modified;
	private int pos, tc_ns, tc_sa;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private Calendar calendar;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adv_sales);
        this.setTitle(ACT_TITLE);
        initViews();
        setListensers();
        getParam();
        btnShow();
    }

    private void initViews() {
        orderDateTxt = (TextView)findViewById(R.id.txtOrderDate);
    	changeDateBtn = (Button)findViewById(R.id.butChangeDate);
        custRefNoTxt = (EditText)findViewById(R.id.editCustRefNo);
        custNameTxt = (TextView)findViewById(R.id.txtCustName);
        mListView = (ListView)findViewById(R.id.salesDetailsList);
        itemDescTxt = (TextView)findViewById(R.id.txtItemDesc);        
        preQtyTxt = (TextView)findViewById(R.id.txtPreQty);
        tcSpinner = (Spinner)findViewById(R.id.spTranCode);
        qtyTxt = (EditText)findViewById(R.id.editQty);
        copyBtn = (Button)findViewById(R.id.butCopy);
        saveBtn = (Button)findViewById(R.id.butSave);
        modifyBtn = (Button)findViewById(R.id.butModify);
        delBtn = (Button)findViewById(R.id.butDel);        
        itemAddBtn = (Button)findViewById(R.id.butItemAdd);
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    	advAList = new ArrayList<HashMap<String, String>>();

    	createTranCodeOpt();
    	modified = false;
    	tc_ns = 0;
    	tc_sa = 1;
    }

    private void setListensers() {
    	calendar=Calendar.getInstance();
    	changeDateBtn.setOnClickListener(changeDateClick);
    	mListView.setOnItemClickListener(detailsClick);
    	tcSpinner.setOnItemSelectedListener(tranCodeSelected);
    	copyBtn.setOnClickListener(copyClick);
    	saveBtn.setOnClickListener(saveClick);
    	modifyBtn.setOnClickListener(modifyClick);
    	delBtn.setOnClickListener(delClick);
    	itemAddBtn.setOnClickListener(itemAddClick);
    	custRefNoTxt.setOnClickListener(custRefNoClick);
    }
    
    private Button.OnClickListener changeDateClick = new Button.OnClickListener() {
        @Override  
        public void onClick(View v) {
            // TODO Auto-generated method stub  
            new DatePickerDialog(AdvSalesActivity.this,  
                    new DatePickerDialog.OnDateSetListener() {                            
                        @Override  
                        public void onDateSet(DatePicker view, int year, int monthOfYear,  
                                int dayOfMonth) {          
                        	String ymd = String.valueOf(year)+                                
                        			String.format("%2s", String.valueOf(monthOfYear+1)).replace(' ', '0')+
                        			String.format("%2s", String.valueOf(dayOfMonth)).replace(' ', '0');
                        	if (Integer.valueOf(ymd) < Integer.valueOf(minDate)) 
                        		util.showDialog("日期必須 >= "+util.strToDateFmt(minDate, "yyyy/MM/dd"), AdvSalesActivity.this);
                        	else
                        		orderDateTxt.setText(util.strToDateFmt(ymd, "yyyy/MM/dd"));
                        }  
            		}, Integer.valueOf(orderDateTxt.getText().toString().substring(0,4)),
            		   Integer.valueOf(orderDateTxt.getText().toString().substring(5, 7))-1,
            		   Integer.valueOf(orderDateTxt.getText().toString().substring(8,10))).show();
                    //},calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
        }      	
    };
        
    private void getParam() {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	custCode = bundle.getString("cust_code");
    	custNameTxt.setText(bundle.getString("cust_name"));
    	custGroup = bundle.getString("cust_group");
    	showResults();    	
    }
    
    private void showResults() {
		HashMap<String, String> advHmap;

		String query = "select "
				+ "b.line_no as _id, "
				+ "b.customer_ref_no, "
				+ "b.order_date, "
				+ "b.item_code, "
				+ "c.chinese_desc, "
				+ "b.tran_code, "
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

		int i=0;
    	db = dbh.getWritableDatabase();
    	Cursor c = db.rawQuery(query, new String[] {custCode, custCode, util.getCurDate()});
    	if (c.moveToFirst()) {
    		orderDateTxt.setText(util.strToDateFmt(c.getString(c.getColumnIndex("order_date")), "yyyy/MM/dd"));
    		custRefNoTxt.setText(c.getString(c.getColumnIndex("customer_ref_no")));
        	do {
        		i++;
        		advHmap = new HashMap<String, String>();
        		advHmap.put("tranCode", c.getString(c.getColumnIndex("tran_code")));
        		advHmap.put("itemCode", c.getString(c.getColumnIndex("item_code")));
        		advHmap.put("itemDesc", c.getString(c.getColumnIndex("chinese_desc")));
        		advHmap.put("ordQty", c.getString(c.getColumnIndex("order_qty")));
        		advAList.add(advHmap);
        	}while (c.moveToNext());
        	disableButton(changeDateBtn);
        	copyBtn.setText("消單");
    	} else {
    		calendar.add(Calendar.DATE, 1);
    		minDate = util.getDateStr(calendar);
    		orderDateTxt.setText(util.strToDateFmt(minDate, "yyyy/MM/dd"));
    	}
        try {
        	ListAdapter adapter = new SimpleAdapter(this, advAList, R.layout.adv_sales_list_item,
        			new String[] {"tranCode", "itemDesc", "ordQty"}, 
        			new int[] {R.id.stranCode, R.id.sitemDesc, R.id.sqty});
        	mListView.setAdapter(adapter);
        } catch (Exception e) {
        	//
        }		
		db.close();        	
    }
    
    private void getPreAndOnHandQty(String iItem) {
    	
    	String preQty = getPreQty(iItem);
    	preQtyTxt.setText(String.valueOf(preQty));    	    	
    }
    
    private String getPreQty(String iItem) {
    	String preQty = "0";
    	//get last order qty
    	String query = "select b.order_qty "
    			+ "from sales_order_hdr a, sales_order_dtl b "
    			+ "where a.order_no = b.order_no "
    			+ "and a.cust_code = ? "
    			+ "and a.order_date < ? "
    			+ "and b.item_code = ? "
    			+ "and b.tran_code = 'NS'";
    	
    	if (!db.isOpen()) { db = dbh.getWritableDatabase(); }
    	Cursor c = db.rawQuery(query, new String[] {
    			custCode, util.dateToStrFmt(orderDateTxt.getText().toString()), iItem});
    	if (c.moveToFirst()) {
    		preQty = c.getString(c.getColumnIndex("order_qty"));
    	}
    	if (!db.isOpen()) { db.close(); }
    	
    	return (preQty);
    }
    
    private EditText.OnClickListener custRefNoClick = new EditText.OnClickListener() {
    	@Override
    	public void onClick(View v) {
        	custRefNoTxt.addTextChangedListener(new TextWatcher() {
        		public void afterTextChanged(Editable s) {}
        		public void beforeTextChanged(CharSequence s, int start,
        			     int count, int after) {}
        		public void onTextChanged(CharSequence s, int start,
        			     int before, int count) {
        			modified = true;        			
        			if (saveBtn.isEnabled()==false)
        				enableButton(saveBtn);
        		}    		
        	});    		
    	}
    };
    
    private ListView.OnItemClickListener detailsClick = new ListView.OnItemClickListener() {
    	@Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	pos = position;
        	String tc = advAList.get(pos).get("tranCode");
        	String itemCode = advAList.get(pos).get("itemCode");
        	itemDescTxt.setText(advAList.get(pos).get("itemDesc"));
        	qtyTxt.setText(advAList.get(pos).get("ordQty"));
        	if (tc.equals("NS"))
        		tcSpinner.setSelection(tc_ns);
        	else
        		tcSpinner.setSelection(tc_sa);
        	getPreAndOnHandQty(itemCode);
        	
        	if (modifyBtn.isEnabled() == false)
        		enableButton(modifyBtn);
        	if (delBtn.isEnabled() == false)
        		enableButton(delBtn);        		
        }    	
    };
    
    private void createTranCodeOpt() {
    	List<String> list = new ArrayList<String>();
    	list.add("NS");
    	list.add("SA");
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
    			this, android.R.layout.simple_spinner_item, list);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    	tcSpinner.setAdapter(dataAdapter);
    }

    private Spinner.OnItemSelectedListener tranCodeSelected = new Spinner.OnItemSelectedListener() {
    	
    	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
    		//
    	}
		public void onNothingSelected(AdapterView arg0) {
			//
		}
    };

    private Button.OnClickListener copyClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		if (copyBtn.getText().toString().equals("消單")) {
        		AlertDialog.Builder ab = new Builder(AdvSalesActivity.this);
        		ab.setTitle(ACT_TITLE);
        		ab.setMessage("確認取消此預售單資料?");
        		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        				cancelOrder();
        				dialog.dismiss();
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
    		} else { //複製
        		Intent it = new Intent(AdvSalesActivity.this, CopyInvoicesActivity.class);
        		Bundle bundle = new Bundle();
        		bundle.putString("ActName", "AdvSalesActivity");
        		bundle.putString("cust_code", custCode);
        		it.putExtras(bundle);    		
        		startActivityForResult(it, 0);    			
    		}
    	}    	
    };
    
    private Button.OnClickListener saveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		saveOrders();
    	}    	
    };
    
    private Button.OnClickListener modifyClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		int qty = Integer.parseInt(qtyTxt.getText().toString());
    		String tc = tcSpinner.getSelectedItem().toString();

			modified = true;
			advAList.get(pos).put("tranCode", tc);
			advAList.get(pos).put("ordQty", qtyTxt.getText().toString());
			mListView.invalidateViews();
			enableButton(saveBtn);
			Toast toast = Toast.makeText(AdvSalesActivity.this, "已修改", Toast.LENGTH_SHORT);				
			toast.show();    					    		
    	}
    };
    
    private Button.OnClickListener delClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {

    		String itemCode = advAList.get(pos).get("itemCode");
    		String itemDesc = advAList.get(pos).get("itemDesc");
    		
    		AlertDialog.Builder ab = new Builder(AdvSalesActivity.this);
    		ab.setTitle(ACT_TITLE);
    		ab.setMessage("確認刪除 " + itemDesc + " (" + itemCode + ")?");
    		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				//remove items from order list
    				modified = true;
    				advAList.remove(pos);
    				mListView.invalidateViews();
    				
    				if (advAList.isEmpty()) {
    					copyBtn.setText("複製");
    					enableButton(copyBtn);
    					disableButton(saveBtn);
    				} else {
    					enableButton(saveBtn);
    				}
    				clear();
    				dialog.dismiss();
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
    	}
    };
    
    private Button.OnClickListener itemAddClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
        	Intent it = new Intent(AdvSalesActivity.this, AddSalesDetailsActivity.class);            		
        	Bundle bundle = new Bundle();
        	bundle.putString("ActName", "AdvSalesActivity");
        	bundle.putString("order_date", orderDateTxt.getText().toString());
        	bundle.putString("cust_code", custCode);
        	bundle.putString("cust_group", custGroup);
        	bundle.putSerializable("order_list", advAList);
        	it.putExtras(bundle);                    
        	startActivityForResult(it, 1);            		
    	}
    };

    
    private void cancelOrder() {
    	try {
    		db = dbh.getWritableDatabase();
    		db.beginTransaction();
    		
    		delAdvSales();
    		
    		db.setTransactionSuccessful();
    		
    		modified = false;
			Toast toast = Toast.makeText(AdvSalesActivity.this, "已消單", Toast.LENGTH_SHORT);				
			toast.show();
			AdvSalesActivity.this.finish();			
    	} catch (SQLException e) {
    		util.showDialog("Deletion Error!!!", AdvSalesActivity.this);
    	} finally {
    		db.endTransaction();
    		db.close();
    	}
    }
    
    private void saveOrders() {
    	try {
    		db = dbh.getWritableDatabase();
    		db.beginTransaction();
    		   
    		delAdvSales();
    		insAdvSales();
    		copyBtn.setText("消單");
    		enableButton(copyBtn);
    		
    		db.setTransactionSuccessful();    		
    		modified = false;
			Toast toast = Toast.makeText(AdvSalesActivity.this, "已儲存", Toast.LENGTH_SHORT);				
			toast.show();    					
    	} catch (SQLException e) {
    		util.showDialog("Deletion Error!!!", AdvSalesActivity.this);
    	} catch (Exception e) {
    		util.showDialog(e.getMessage(), AdvSalesActivity.this);
    	} finally {
    		db.endTransaction();
    		db.close();
    	}
    }
    
    private void delAdvSales() {
    	db.delete("adv_sales_order"
    			, "order_date = '" + util.dateToStrFmt(orderDateTxt.getText().toString()) + "' and "+
    	          "cust_code = '" + custCode + "'"    	          
    			, null);
    }
    
    private void insAdvSales() throws Exception {
		int line = 0, qty;
		String itemCode, tranCode;
		boolean error = false;
		
		Iterator<HashMap<String, String>> it = advAList.iterator();
		while (it.hasNext()) {
			HashMap<String, String> hm = it.next();
			line++;
			
			itemCode = hm.get("itemCode");
			tranCode = hm.get("tranCode");
			if (!tranCode.equals("PG")) {
				qty = Integer.parseInt(hm.get("ordQty"));			
				insAdvSalesRecord(line, itemCode, tranCode, qty);												
			}
		}		
		if (error)
			throw new Exception("Insertion Error!!!");
		else
			insPromotion(line);		
    }
    
    private void insAdvSalesRecord(int iLine, String iItem, String iTranCode, int iQty) {
    	ContentValues cv = new ContentValues();
    	cv.put("order_date", util.dateToStrFmt(orderDateTxt.getText().toString()));
    	cv.put("cust_code", custCode);
    	cv.put("customer_ref_no", custRefNoTxt.getText().toString());
		cv.put("line_no", iLine);
		cv.put("item_code", iItem);
		cv.put("tran_code", iTranCode);
		cv.put("order_qty", iQty);
		db.insert("adv_sales_order", null, cv);
    }
    
    private void insPromotion(int iLine) throws Exception {
    	int line = iLine;
    	String itemCode;
    	int pgQty, ohq;
    	boolean error = false;
    	List<List<String>> promoList = new ArrayList<List<String>>();
    	
    	String query = "select a.item_code, "
    			+ "b.chinese_desc, "
    			+ "a.order_qty "
    			+ "from adv_sales_order a, item_file b "
    			+ "where a.item_code = b.item_code "
    			+ "and a.order_date = ? "
    			+ "and a.cust_code = ? "
    			+ "and a.tran_code = ? "
    			+ "order by a.line_no";
    	Cursor c = db.rawQuery(query, new String[] {orderDateTxt.getText().toString(), custCode, "NS"});
    	if (c.moveToFirst()) {
    		do {
    			promoList = util.getPromoItemQty(AdvSalesActivity.this
    					, dbh
    					, db
    					, custCode
    					, custGroup
    					, util.dateToStrFmt(orderDateTxt.getText().toString())
    					, c.getString(c.getColumnIndex("item_code"))
    					, c.getInt(c.getColumnIndex("order_qty")));
    			if (promoList.size() > 0) {
            		for (int i=0;i<promoList.size();i++) {
        				line++;
            			itemCode = promoList.get(i).get(0);
            			pgQty = Integer.parseInt(promoList.get(i).get(1));
        				insAdvSalesRecord(line, itemCode, "PG", pgQty);
            		}    				
    			}
    			
    			if (error)
    				throw new Exception("");
    		} while (c.moveToNext());
    	}
    }
    
    private void copyInvoices(String iDate) {
		HashMap<String, String> ordHmap;
		String itemCode, tranCode, qty;

		String query = "select b.line_no as _id, "
				+ "b.tran_code, "
				+ "b.item_code, "
				+ "c.chinese_desc, "
				+ "b.order_qty "
				+ "from sales_order_hdr a, sales_order_dtl b, item_file c "
				+ "where a.order_no = b.order_no "
				+ "and b.item_code = c.item_code "
				+ "and a.order_date = ? "
				+ "and a.cust_code = ? "
				+ "and a.order_status <> 'D' "
				+ "and b.tran_code not in ('RP','PG') "
				+ "order by b.line_no ";			
		
		db  = dbh.getWritableDatabase();
		Cursor c = db.rawQuery(query, new String[] {iDate, custCode});
        int i=0;        
        if (c.moveToFirst()) {
        	//advAList = new ArrayList<HashMap<String, String>>();
        	do {
        		i++;
        		itemCode = c.getString(c.getColumnIndex("item_code"));
        		qty = c.getString(c.getColumnIndex("order_qty"));
        		tranCode = c.getString(c.getColumnIndex("tran_code"));
        		ordHmap = new HashMap<String, String>();
        		ordHmap.put("tranCode", tranCode);
        		ordHmap.put("itemCode", itemCode);
        		ordHmap.put("itemDesc", c.getString(c.getColumnIndex("chinese_desc")));
        		ordHmap.put("ordQty", qty);
        		advAList.add(ordHmap);
        	}while (c.moveToNext());
        	mListView.invalidateViews();
        }        
        db.close();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Bundle bundle = data.getExtras();
    	if (requestCode == 0) {
    		String orderDate = bundle.getString("order_date");
    		if (orderDate != null) {
    			copyInvoices(orderDate);
    			if (!advAList.isEmpty()) {
    				modified = true;
    				disableButton(copyBtn);
    				enableButton(saveBtn);
    			}
    		}
    	} else if (requestCode == 1) {
    		ArrayList<HashMap<String, String>> addAList = 
    				(ArrayList<HashMap<String, String>>) bundle.getSerializable("order_list");

    		boolean addItem=false;
    		Iterator<HashMap<String, String>> it = addAList.iterator();
    		while (it.hasNext()) {
    			HashMap<String, String> hm = it.next();
				//add items into the order list
				HashMap<String, String> ordHmap = new HashMap<String, String>();
        		ordHmap.put("tranCode", hm.get("tranCode"));
        		ordHmap.put("itemCode", hm.get("itemCode"));
        		ordHmap.put("itemDesc", hm.get("itemDesc"));
        		ordHmap.put("ordQty", hm.get("ordQty"));
        		addItem = true;
				advAList.add(ordHmap);
    		}
    		if (addItem) {
				mListView.invalidateViews();
				modified = true;
				disableButton(copyBtn);
				enableButton(saveBtn);
    		}
    	}
    }
        
    private void clear() {
    	itemDescTxt.setText(null);
    	preQtyTxt.setText(null);
    	qtyTxt.setText(null);
    	disableButton(modifyBtn);
    	disableButton(delBtn);
    }
 
    private void btnShow() {
    	enableButton(copyBtn);
    	disableButton(saveBtn);
    	disableButton(modifyBtn);
    	disableButton(delBtn);
    	enableButton(itemAddBtn);
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
    }
 
    @Override
    public void onBackPressed() {

    	if (modified) {
    		AlertDialog.Builder ab = new Builder(AdvSalesActivity.this);
    		ab.setTitle(ACT_TITLE);
    		ab.setMessage("確認取消所有未儲存的資料及離開?");
    		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
    				dialog.dismiss();
    				AdvSalesActivity.this.finish();
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
    		this.finish();
    	}
    }
}
 