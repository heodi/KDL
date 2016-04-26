package com.iceapp;

import android.app.Activity;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

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
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.Date; 

public class SalesDetailsActivity extends Activity {
 
	private static final String ACT_TITLE = "銷售單詳情";
	
	private Button saveBtn, modifyBtn, delBtn, itemAddBtn, copyBtn;
	private EditText custRefNoTxt, qtyTxt;
	private TextView orderDateTxt, orderNoTxt, statusTxt;
	private TextView custNameTxt, itemDescTxt, onHandTxt, preQtyTxt;
	private Spinner tcSpinner;
    private ListView mListView;
    private ArrayList<HashMap<String, String>> ordAList, delAList;
	private String custCode, custGroup, priceBk, invalidItemDesc;
	private boolean modified;
	private int pos, tc_ns, tc_rp, tc_sa;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales_details);
        this.setTitle(ACT_TITLE);
        initViews();
        setListensers();
        getParam();
        btnShow();
    }

    private void initViews() {

        orderDateTxt = (TextView)findViewById(R.id.txtOrderDate);
        orderNoTxt = (TextView)findViewById(R.id.txtOrderNo);
        custRefNoTxt = (EditText)findViewById(R.id.editCustRefNo);
        statusTxt = (TextView)findViewById(R.id.txtStatus);
        custNameTxt = (TextView)findViewById(R.id.txtCustName);
        mListView = (ListView)findViewById(R.id.salesDetailsList);
        itemDescTxt = (TextView)findViewById(R.id.txtItemDesc);        
        preQtyTxt = (TextView)findViewById(R.id.txtPreQty);
        onHandTxt = (TextView)findViewById(R.id.txtOnHand);
        tcSpinner = (Spinner)findViewById(R.id.spTranCode);
        qtyTxt = (EditText)findViewById(R.id.editQty);
        copyBtn = (Button)findViewById(R.id.butCopy);
        saveBtn = (Button)findViewById(R.id.butSave);
        modifyBtn = (Button)findViewById(R.id.butModify);
        delBtn = (Button)findViewById(R.id.butDel);        
        itemAddBtn = (Button)findViewById(R.id.butItemAdd);
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    	ordAList = new ArrayList<HashMap<String, String>>();
    	delAList = new ArrayList<HashMap<String, String>>();

    	createTranCodeOpt();
    	modified = false;
    	tc_ns = 0;
    	tc_rp = 1;
    	tc_sa = 2;
    }

    private void setListensers() {
    	mListView.setOnItemClickListener(detailsClick);
    	tcSpinner.setOnItemSelectedListener(tranCodeSelected);
    	copyBtn.setOnClickListener(copyClick);
    	saveBtn.setOnClickListener(saveClick);
    	modifyBtn.setOnClickListener(modifyClick);
    	delBtn.setOnClickListener(delClick);
    	itemAddBtn.setOnClickListener(itemAddClick);
    	custRefNoTxt.setOnClickListener(custRefNoClick);
    }

    private void getParam() {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	String numOrcode = bundle.getString("numOrcode");
    	String value = bundle.getString("value");
    	showResults(numOrcode, value);    	
    }
    
    private void showResults(String numOrcode, String value) {
    	String query;
    	Cursor c;
    	
		//show sales order header data
    	if (numOrcode.equals("order_no")) {
    		query = "select "
    				+ "b.order_no, "
    				+ "b.customer_ref_no, "
    				+ "b.order_status, "
    				+ "a.cust_code, "
    				+ "a.chinese_name, "
    				+ "a.cust_group, "
    				+ "a.price_book_code "
    				+ "from customer_file a, sales_order_hdr b "
    				+ "where a.cust_code = b.cust_code "
    				+ "and b.order_date = ? "
    				+ "and b.order_no = ?";
    	} else {
    		query = "select "
    				+ "b.order_no, "
    				+ "b.customer_ref_no, "
    				+ "b.order_status, "
    				+ "a.cust_code, "
    				+ "a.chinese_name, "
    				+ "a.cust_group, "
    				+ "a.price_book_code "
    				+ "from customer_file a "
    				+ "LEFT OUTER JOIN sales_order_hdr b "
    				+ "ON a.cust_code = b.cust_code "
    				+ "and b.order_date = ? "
    				+ "and b.order_status <> 'D' "
    				+ "WHERE a.cust_code = ? ";    		
    	}
		    	
    	db = dbh.getWritableDatabase();    	
    	c = db.rawQuery(query, new String[] {util.getCurDate(), value});
    	
    	c.moveToFirst();    	
		custCode = c.getString(c.getColumnIndex("cust_code"));
		custGroup = c.getString(c.getColumnIndex("cust_group"));
		priceBk = c.getString(c.getColumnIndex("price_book_code"));
		custNameTxt.setText(c.getString(c.getColumnIndex("chinese_name")));
		orderDateTxt.setText(util.strToDateFmt(util.getCurDate(), "yyyy/MM/dd"));
		
		String status = c.getString(c.getColumnIndex("order_status"));
		if (status != null) {
			orderNoTxt.setText(c.getString(c.getColumnIndex("order_no")));
			custRefNoTxt.setText(c.getString(c.getColumnIndex("customer_ref_no")));
			if (status.equals("N"))
				statusTxt.setText("新單");
			else if (status.equals("C"))
				statusTxt.setText("確認");
			else if (status.equals("D"))
				statusTxt.setText("取消");

			showDetails();
		}
        try {
        	ListAdapter adapter = new SimpleAdapter(this, ordAList, R.layout.sales_details_list_item,
        			new String[] {"tranCode", "itemDesc", "ordQty","uPrice"}, 
        			new int[] {R.id.stranCode, R.id.sitemDesc, R.id.sqty, R.id.suPrice});
        	mListView.setAdapter(adapter);
        } catch (Exception e) {
        	//
        }
		db.close();
    }
    
    private void showDetails() {
		ordAList.clear();
		HashMap<String, String> ordHmap;
		String query = "select a.line_no as _id, "
				+ "a.tran_code, "
				+ "a.item_code, "
				+ "b.chinese_desc, "
				+ "a.order_qty, "
				+ "a.unit_price "
				+ "from sales_order_dtl a, item_file b "
				+ "where a.item_code = b.item_code "
				+ "and a.order_no = ? "
				+ "order by a.line_no ";
		Cursor d = db.rawQuery(query, new String[] {orderNoTxt.getText().toString()});
        int i=0;        
        if (d.moveToFirst()) {
        	do {
        		i++;
        		ordHmap = new HashMap<String, String>();
        		ordHmap.put("tranCode", d.getString(d.getColumnIndex("tran_code")));
        		ordHmap.put("itemCode", d.getString(d.getColumnIndex("item_code")));
        		ordHmap.put("itemDesc", d.getString(d.getColumnIndex("chinese_desc")));
        		ordHmap.put("savedQty", d.getString(d.getColumnIndex("order_qty")));
        		ordHmap.put("ordQty", d.getString(d.getColumnIndex("order_qty")));
        		ordHmap.put("uPrice",  d.getString(d.getColumnIndex("unit_price")));
        		ordAList.add(ordHmap);
        	}while (d.moveToNext());
        }    	
    }
    
    private void getPreAndOnHandQty(String iItem) {
    	
    	String onHand = getOnHandQty(orderNoTxt.getText().toString(), iItem);
    	String preQty = getPreQty(iItem);
    	onHandTxt.setText(String.valueOf(onHand));
    	preQtyTxt.setText(String.valueOf(preQty));    	    	
    }
    
    private String getOnHandQty(String iOrderNo, String iItem) {
    	//get onhand qty
    	String query;
    	String onHand="0";
    	
    	if (iOrderNo.equals("")) {
        	query = "select opening_qty-sales_qty onhand "
        			+ "from truck_open a, pda b "
        			+ "where a.route = b.route "
        			+ "and b.default_route = 'Y' "
        			+ "and item_code = ? ";    		    		
    	} else {
    		query = "select a.opening_qty-a.sales_qty+IFNULL(c.order_qty,0) onhand "
        			+ "from truck_open a "
        			+ "INNER JOIN pda b "
        			+ "on a.route = b.route "
        			+ "and b.default_route = 'Y' "
        			+ "LEFT OUTER JOIN sales_order_dtl c "
        			+ "on c.order_no = '" + iOrderNo + "' "
        			+ "and c.item_code = '" + iItem + "' "
        			+ "and c.tran_code = 'NS' "
        			+ "where a.item_code = ? ";        			
    	}

    	if (!db.isOpen()) { db = dbh.getWritableDatabase(); }    	
    	Cursor c = db.rawQuery(query, new String[] {iItem});
    	if (c.moveToFirst())
    		onHand = c.getString(c.getColumnIndex("onhand"));    	    	
    	if (!db.isOpen()) { db.close(); }
    	
    	return (onHand);    	    	
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
        			if (!modified) {
        				if (!statusTxt.getText().toString().equals("取消")) {
                			modified = true;
                			saveBtn.setText("儲存");    				        					
        				}
        			}
        		}    		
        	});    		
    	}
    };
    
    private ListView.OnItemClickListener detailsClick = new ListView.OnItemClickListener() {
    	@Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	pos = position;
        	String tc = ordAList.get(pos).get("tranCode");
        	if (tc.equals("PG")) {
        		itemDescTxt.setText("");
            	onHandTxt.setText("");
            	preQtyTxt.setText("");    	    	        	
        		qtyTxt.setText("");
        		btnShow();
        		util.showDialog("不過更改 PG 類別的資料", SalesDetailsActivity.this);
        	}
        	else {
            	String status = statusTxt.getText().toString();
            	String itemCode = ordAList.get(pos).get("itemCode");
            	itemDescTxt.setText(ordAList.get(pos).get("itemDesc"));
            	qtyTxt.setText(ordAList.get(pos).get("ordQty"));
            	if (tc.equals("NS"))
            		tcSpinner.setSelection(tc_ns);
            	else if (tc.equals("RP"))
            		tcSpinner.setSelection(tc_rp);
            	else
            		tcSpinner.setSelection(tc_sa);
            	getPreAndOnHandQty(itemCode);
            	
            	if (status.equals("新單") || status.equals("")) {
                	if (modifyBtn.isEnabled() == false)
                		enableButton(modifyBtn);
                	if (delBtn.isEnabled() == false)
                		enableButton(delBtn);        		
            	}        		
        	}
        }    	
    };
    
    private void createTranCodeOpt() {
    	List<String> list = new ArrayList<String>();
    	list.add("NS");
    	list.add("RP");
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
        		AlertDialog.Builder ab = new Builder(SalesDetailsActivity.this);
        		ab.setTitle(ACT_TITLE);
        		ab.setMessage("確認取消此銷售單?");
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
        		Intent it = new Intent(SalesDetailsActivity.this, CopyInvoicesActivity.class);
        		Bundle bundle = new Bundle();
        		bundle.putString("ActName", "SalesDetailsActivity");
        		bundle.putString("cust_code", custCode);
        		it.putExtras(bundle);    		
        		startActivityForResult(it, 0);
    		}
    	}    	
    };
    
    private Button.OnClickListener saveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		if (saveBtn.getText().toString().equals("儲存")) {
    			saveOrders();
    		} else {//總計
            	//Pass data to another Activity 
            	Intent it = new Intent(SalesDetailsActivity.this, SalesInvTotalActivity.class);
            	Bundle bundle = new Bundle();
            	bundle.putString("order_no", orderNoTxt.getText().toString());
            	it.putExtras(bundle);
            	startActivityForResult(it, 2);
            	//startActivity(it);    			
    		}
    	}    	
    };
    
    private Button.OnClickListener modifyClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		int qty = Integer.parseInt(qtyTxt.getText().toString());
    		int onHand = Integer.parseInt(onHandTxt.getText().toString());
    		String tc = tcSpinner.getSelectedItem().toString();
    		/**
    		if (qty > onHand) {
    			util.showDialog("所輸入之數量大於存貨數量", SalesDetailsActivity.this);
    			qtyTxt.requestFocus();
    		} else if (qty <= 0) { 
    			util.showDialog("所輸入之數量必需大於0", SalesDetailsActivity.this);
    			qtyTxt.requestFocus();
    		} else {
    			if (!tc.equals(ordAList.get(pos).get("tranCode").toString())) {
    				ordAList.get(pos).put("tranCode", tc);
    				if (tc.equals("NS")) {    					
    					ordAList.get(pos).put("uPrice", util.getUprice(SalesDetailsActivity.this
    							, dbh
    							, db
    							, priceBk
    							, ordAList.get(pos).get("itemCode")
    							, util.dateToStrFmt(orderDateTxt.getText().toString())
    							, qtyTxt.getText().toString()));
    				} else
    					ordAList.get(pos).put("uPrice", "0");
    			}
    			modified = true;
    			ordAList.get(pos).put("ordQty", qtyTxt.getText().toString());
				mListView.invalidateViews();
				saveBtn.setText("儲存");
				Toast toast = Toast.makeText(SalesDetailsActivity.this, "已修改", Toast.LENGTH_SHORT);				
				toast.show();    					
    		}
    		*/
    		if (qty > onHand) {
    			util.showDialog("所輸入之數量大於存貨數量", SalesDetailsActivity.this);
    			qtyTxt.requestFocus();
    		} else if (qty <= 0) { 
    			util.showDialog("所輸入之數量必需大於0", SalesDetailsActivity.this);
    			qtyTxt.requestFocus();
    		} else {
    			if (!tc.equals(ordAList.get(pos).get("tranCode").toString())) {
    				ordAList.get(pos).put("tranCode", tc);
    			}
				if (tc.equals("NS")) {    					
					ordAList.get(pos).put("uPrice", util.getUprice(SalesDetailsActivity.this
							, dbh
							, db
							, priceBk
							, ordAList.get(pos).get("itemCode")
							, util.dateToStrFmt(orderDateTxt.getText().toString())
							, qtyTxt.getText().toString()));
				} else {
					ordAList.get(pos).put("uPrice", "0");
				}
    			modified = true;
    			ordAList.get(pos).put("ordQty", qtyTxt.getText().toString());
				mListView.invalidateViews();
				saveBtn.setText("儲存");
				Toast toast = Toast.makeText(SalesDetailsActivity.this, "已修改", Toast.LENGTH_SHORT);				
				toast.show();    					
    		}    		
    	}
    };
    
    private Button.OnClickListener delClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {

    		String itemCode = ordAList.get(pos).get("itemCode");
    		String itemDesc = ordAList.get(pos).get("itemDesc");
    		
    		AlertDialog.Builder ab = new Builder(SalesDetailsActivity.this);
    		ab.setTitle(ACT_TITLE);
    		ab.setMessage("確認刪除 " + itemDesc + " (" + itemCode + ")?");
    		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				//update the remove details list
    				if (Integer.parseInt(ordAList.get(pos).get("savedQty")) > 0) {
        				HashMap<String, String> delHmap = new HashMap<String, String>();
        				delHmap.put("itemCode", ordAList.get(pos).get("itemCode"));
        				delHmap.put("delQty", ordAList.get(pos).get("savedQty"));
        				delAList.add(delHmap);    					
    				}

    				//remove items from order list
    				modified = true;
    				ordAList.remove(pos);
    				mListView.invalidateViews();
    				saveBtn.setText("儲存");
    				if (statusTxt.getText().toString().equals("") && ordAList.isEmpty()) {
    					copyBtn.setText("複製");
    					enableButton(copyBtn);
    					disableButton(saveBtn);
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
        	Intent it = new Intent(SalesDetailsActivity.this, AddSalesDetailsActivity.class);            		
        	Bundle bundle = new Bundle();
        	bundle.putString("ActName", "SalesDetailsActivity");
        	bundle.putString("order_date", orderDateTxt.getText().toString());
        	bundle.putString("cust_code", custCode);
        	bundle.putString("cust_group", custGroup);
        	bundle.putSerializable("order_list", ordAList);
        	bundle.putSerializable("del_list", delAList);
        	it.putExtras(bundle);                    
        	startActivityForResult(it, 1);            		
    	}
    };

    
    private void cancelOrder() {
    	try {
    		db = dbh.getWritableDatabase();
    		db.beginTransaction();
    		
    		//reverse truck_open sales qty
    		updTruckOpen("minus");

    		//mark sales_order_hdr's status to "D"    	
    		ContentValues cv = new ContentValues();
    		cv.put("order_status", "D");
    		db.update("sales_order_hdr", cv, 
    				"order_no = ?",
    				new String[] {orderNoTxt.getText().toString()}); 
    		
    		//update route_cust -> 未去
    		updRouteCust("0");
    		
    		db.setTransactionSuccessful();
    		statusTxt.setText("取消");
    		btnShow();        				    	    		
    	} catch (SQLException e) {
    		util.showDialog("Update Error!!!", SalesDetailsActivity.this);
    	} finally {
    		db.endTransaction();
    		db.close();
    	}
    }
    
    private void saveOrders() {
    	try {
    		invalidItemDesc = "";
    		db = dbh.getWritableDatabase();
    		db.beginTransaction();
    		    		
    		double totalAmt=0;
    		    		
    		if (statusTxt.getText().equals("")) {
    			getOrderNum();
    			totalAmt = insSalesDtl();
    			insSalesHdr(totalAmt);
    			updRouteCust("1");
    			statusTxt.setText("新單");
    		} 
    		else {
    			updTruckOpen("minus");
    			delSalesDtl();
    			totalAmt = insSalesDtl();
    			updSalesHdr(totalAmt);
    		}    		    		    		
    		saveBtn.setText("總計");
    		btnShow();    		
    		db.setTransactionSuccessful();
    		showDetails();
	        mListView.invalidateViews();
    		modified = false;
    		refreshOrdAList();
			Toast toast = Toast.makeText(SalesDetailsActivity.this, "已儲存", Toast.LENGTH_SHORT);				
			toast.show();    					
    	} catch (SQLException e) {
    		util.showDialog("Update Error!!!", SalesDetailsActivity.this);
    	} catch (Exception e) {
    		util.showDialog(e.getMessage(), SalesDetailsActivity.this);
    	} finally {
    		db.endTransaction();
    		db.close();
    	}
    }
    
    private void getOrderNum() throws Exception {
    	String query = "select prefix, "
    			+ "last_seq_no+1 onum "
    			+ "from sales_order_num";
    	Cursor c = db.rawQuery(query, null);
    	if (c.moveToFirst()) {
    		String prefix = c.getString(c.getColumnIndex("prefix"));
    		String num = c.getString(c.getColumnIndex("onum"));
    		
    		//Check whether the orderNo is or not greater than the previous order no.
    		String q = "select order_no "
    				+ "from sales_order_hdr "
    				+ "where substr(order_no,1,2) = ? "
    				+ "and substr(order_no,3) >= ?";
    		Cursor c1 = db.rawQuery(q, new String[] {prefix, num});
    		if (c1.moveToFirst()) {
    			throw new Exception("已設定之銷售單號小於現有之銷售單號，請重新設定");
    		} else {
        		ContentValues cv = new ContentValues();
        		cv.put("last_seq_no", Integer.parseInt(num));
        		db.update("sales_order_num", cv, null, null);
        		
        		orderNoTxt.setText(prefix + String.format("%6s", num).replace(' ', '0'));
    		}
    	} else {
    		throw new Exception("請預設銷售單號");
    	}
    }

    private void delSalesDtl() {
    	db.delete("sales_order_dtl"
    			, "order_no = '" + orderNoTxt.getText().toString() + "'"
    			, null);
    }
    
    private double insSalesDtl() throws Exception {
		int line = 0, qty, ohq;
		//Use the float type instead of double to solve the rounding problem
		//double uamt, namt, totalAmt = 0;
		float uamt, namt, totalAmt = 0; 
		String itemCode;
		boolean error = false, changed = false;
		
		Iterator<HashMap<String, String>> it = ordAList.iterator();
		while (it.hasNext()) {
			HashMap<String, String> hm = it.next();
			line++;
			itemCode = hm.get("itemCode");
			qty = Integer.parseInt(hm.get("ordQty"));
			uamt = Float.parseFloat(hm.get("uPrice"));
			if (!statusTxt.getText().equals("")) {
				if (hm.get("tranCode").equals("NS")) {
					//retrieve the unit price once again
					//prevent the unit price mismatch the price book
					//this case will occurs while uploading the csv file after invoice created
					namt = Float.parseFloat(util.getUprice(SalesDetailsActivity.this
							, dbh
							, db
							, priceBk
							, itemCode
							, util.dateToStrFmt(orderDateTxt.getText().toString())
							, hm.get("ordQty")));
					
					if (namt != uamt) {
						uamt = namt;
						ordAList.get(line-1).put("uPrice", String.valueOf(namt));
						changed = true;
					}
				}
			}
			
			if (!hm.get("tranCode").equals("PG")) {
				ohq = Integer.parseInt(getOnHandQty("", itemCode));
				if (ohq < qty) {
					error = true;
					invalidItemDesc = hm.get("itemDesc"); 
					break;
				} else  {
					insSalesDtlRecord(line, itemCode, hm.get("tranCode"), qty, uamt);			
					updTruckOpenSales("add", itemCode, qty);
					//Round the line amount into 2 decimal places
					float lamt =  uamt * qty * 100;
					lamt = Math.round(lamt);
					lamt = lamt / 100;
					totalAmt = totalAmt + lamt;
				}				
			}
		}
		if (changed)
			mListView.invalidateViews();
		if (error)
			throw new Exception("存貨量不足  (" + invalidItemDesc + ")");
		else
			insPromotion(line);
		
		return totalAmt;    	
    }
    
    private void insSalesDtlRecord(int iLine, String iItem, String iTranCode, int iQty, double iPrice) {
    	ContentValues cv = new ContentValues();
		cv.put("order_no", orderNoTxt.getText().toString());
		cv.put("line_no", iLine);
		cv.put("item_code", iItem);
		cv.put("tran_code", iTranCode);
		cv.put("order_qty", iQty);
		cv.put("unit_price", iPrice);
		db.insert("sales_order_dtl", null, cv);
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
    			+ "from sales_order_dtl a, item_file b "
    			+ "where a.item_code = b.item_code "
    			+ "and a.order_no = ? "
    			+ "and a.tran_code = ? "
    			+ "order by a.line_no";
    	Cursor c = db.rawQuery(query, new String[] {orderNoTxt.getText().toString(), "NS"});
    	if (c.moveToFirst()) {
    		do {
    			promoList = util.getPromoItemQty(SalesDetailsActivity.this
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
        				ohq = Integer.parseInt(getOnHandQty("", itemCode));
            			if (ohq < pgQty) {
            				error = true;
            				invalidItemDesc = c.getString(c.getColumnIndex("chinese_desc")); 
            				break;
            			} else  {
            				insSalesDtlRecord(line, itemCode, "PG", pgQty, 0);
            				updTruckOpenSales("add", itemCode, pgQty);        				
            			}
            		}    				
    			}
    			if (error)
    				throw new Exception("");
    		} while (c.moveToNext());
    	}
    }
    
    private void insSalesHdr(double iTotalAmt) {
		ContentValues cv = new ContentValues();
		cv.put("order_date", util.dateToStrFmt(orderDateTxt.getText().toString()));		
		cv.put("cust_code", custCode);
		cv.put("order_no", orderNoTxt.getText().toString());
		cv.put("customer_ref_no", custRefNoTxt.getText().toString());
		cv.put("order_status", "N");
		cv.put("total_amt", iTotalAmt);
		cv.put("create_system_date", util.getCurDateTime());
		cv.put("last_mod_system_date", util.getCurDateTime());
		db.insert("sales_order_hdr", null, cv);
    }
    
    private void updSalesHdr(double iTotalAmt) {
    	ContentValues cv = new ContentValues();
    	cv.put("customer_ref_no", custRefNoTxt.getText().toString());
    	cv.put("total_amt", iTotalAmt);
    	cv.put("last_mod_system_date", util.getCurDateTime());
		db.update("sales_order_hdr", cv, 
				"order_no = ?",
				new String[] {orderNoTxt.getText().toString()}); 
    }
    
    private void updTruckOpen(String iSign) {
    	int qty;
    	String itemCode;   
    	String query = "select item_code, "
    			+ "sum(order_qty) order_qty "
    			+ "from sales_order_dtl "
    			+ "where order_no = ? "
    			+ "group by item_code";
    	
    	Cursor c = db.rawQuery(query, new String[] {orderNoTxt.getText().toString()});
    	if (c.moveToFirst()) {
    		do {
    			itemCode = c.getString(c.getColumnIndex("item_code"));
    			qty = Integer.parseInt(c.getString(c.getColumnIndex("order_qty")));
    			updTruckOpenSales(iSign, itemCode, qty);
    		} while (c.moveToNext());
    	}
    }
    
    private void updTruckOpenSales(String iSign, String iItem, int iQty) {
    	int qty = iQty;

    	if (iSign.equals("minus"))    	
    		qty = qty * -1;
    	
    	String query = "update truck_open "
    			+ "set sales_qty = sales_qty + " + qty + " "
    			+ "where item_code = '" + iItem + "' "
    			+ "and route in (select route from pda where default_route = 'Y')";
    	db.execSQL(query);
    }
    
    private void updRouteCust(String iStatus) {
    	ContentValues cv = new ContentValues();
    	String visitDate=null;
    	String defaultRoute, query;
    	Cursor c;
    	    	
    	query = "select route from pda "
    			+ "where default_route = 'Y'";
    	c = db.rawQuery(query, null);
    	c.moveToFirst();
    	defaultRoute = c.getString(c.getColumnIndex("route"));
    	    	
    	//update Route_Cust table
    	if (iStatus.equals("1")) {
    		visitDate = util.getCurDateTime();
    		cv.put("last_order_date", util.dateToStrFmt(orderDateTxt.getText().toString()));
    	}
    	cv.put("status", iStatus);
    	cv.put("visit_date", visitDate);
		db.update("route_cust", cv, 
				"route = ? and cust_code = ?", 
				new String[] {defaultRoute, custCode} );
		c.close();
    }
    
 
    private void refreshOrdAList() {
		Iterator<HashMap<String, String>> it = ordAList.iterator();
		int line=0;
		while (it.hasNext()) {
			HashMap<String, String> hm = it.next();
			String qty = ordAList.get(line).get("ordQty");
			ordAList.get(line).put("savedQty", qty);
			line++;
		}		    	
    }
    
    private void copyInvoices(String iInvoiceFm, String iDate) {
		HashMap<String, String> ordHmap;
		String query, itemCode, tranCode, qty;

		if (iInvoiceFm.equals("OLD")) {
			query = "select b.line_no as _id, "
					+ "a.customer_ref_no, "
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
		} else {
    		query = "select "
    				+ "b.line_no as _id, "
    				+ "b.customer_ref_no, "
    				+ "b.tran_code, "
    				+ "b.item_code, "
    				+ "c.chinese_desc, "
    				+ "b.order_qty "
    				+ "from adv_sales_order b, item_file c "
    				+ "where b.item_code = c.item_code "
    				+ "and b.order_date = ? "
    				+ "and b.cust_code = ? "
    				+ "and b.tran_code not in ('RP','PG') "
    				+ "order by b.line_no";			
		}
		db  = dbh.getWritableDatabase();
		Cursor d = db.rawQuery(query, new String[] {iDate, custCode});
        int i=0;        
        if (d.moveToFirst()) {
        	//ordAList = new ArrayList<HashMap<String, String>>();
        	if (!iInvoiceFm.equals("OLD")) {
            	custRefNoTxt.setText(d.getString(d.getColumnIndex("customer_ref_no")));
        	}
        	do {
        		i++;
        		itemCode = d.getString(d.getColumnIndex("item_code"));
        		qty = d.getString(d.getColumnIndex("order_qty"));
        		tranCode = d.getString(d.getColumnIndex("tran_code"));
        		ordHmap = new HashMap<String, String>();
        		ordHmap.put("tranCode", tranCode);
        		ordHmap.put("itemCode", itemCode);
        		ordHmap.put("itemDesc", d.getString(d.getColumnIndex("chinese_desc")));
        		ordHmap.put("savedQty", "0");
        		ordHmap.put("ordQty", qty);
        		if (tranCode.equals("NS")) {
    				ordHmap.put("uPrice", util.getUprice(SalesDetailsActivity.this
    						, dbh
    						, db
    						, priceBk
    						, itemCode
    						, util.dateToStrFmt(orderDateTxt.getText().toString())
    						, qty));        			
        		} else {
        			ordHmap.put("uPrice", "0");
        		}
        		ordAList.add(ordHmap);
        	}while (d.moveToNext());
        	mListView.invalidateViews();
        }
        db.close();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 0) {
    		Bundle bundle = data.getExtras();
    		String invoiceFm = bundle.getString("invoice_from");
    		String orderDate = bundle.getString("order_date");
    		if (invoiceFm != null) {
    			copyInvoices(invoiceFm, orderDate);
    			if (!ordAList.isEmpty()) {
    				modified = true;
    				disableButton(copyBtn);
    				enableButton(saveBtn);
    			}
    		}
    	} else if (requestCode == 1) {
    		Bundle bundle = data.getExtras();
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
        		ordHmap.put("savedQty", "0");
        		ordHmap.put("ordQty", hm.get("ordQty"));
        		if (hm.get("tranCode").equals("NS")) {
    				ordHmap.put("uPrice", util.getUprice(SalesDetailsActivity.this
    						, dbh
    						, db
    						, priceBk
    						, hm.get("itemCode")
    						, util.dateToStrFmt(orderDateTxt.getText().toString())
    						, hm.get("ordQty")));        			
        		} else {
        			ordHmap.put("uPrice", "0");
        		}
        		addItem = true;
				ordAList.add(ordHmap);
    		}
    		if (addItem) {
				mListView.invalidateViews();
				modified = true;
				saveBtn.setText("儲存");
				disableButton(copyBtn);
				enableButton(saveBtn);
    		}
    	} else if (requestCode == 2) {
    		db = dbh.getWritableDatabase();
    		String query = "select order_status from sales_order_hdr "
    				+ "where order_no = ? ";
    		Cursor c = db.rawQuery(query, new String[]{orderNoTxt.getText().toString()});
    		if (c.moveToFirst()) {
    			if (c.getString(c.getColumnIndex("order_status")).equals("C")) {
    	    		statusTxt.setText("確認");
    	    		btnShow();    				
    			}
    		}
    		c.close();
    		db.close();
    	}
    }
        
    private void clear() {
    	itemDescTxt.setText(null);
    	preQtyTxt.setText(null);
    	onHandTxt.setText(null);
    	qtyTxt.setText(null);
    	disableButton(modifyBtn);
    	disableButton(delBtn);
    }
 
    private void btnShow() {
    	String status = statusTxt.getText().toString();
    	if (status.equals("新單")) {
    		copyBtn.setText("消單");
    		//saveBtn.setText("總計");
    		enableButton(copyBtn);
    		enableButton(saveBtn);
    		disableButton(modifyBtn);
    		disableButton(delBtn);
    		enableButton(itemAddBtn);
    	}
    	else if (status.equals("確認")) {
    		copyBtn.setText("消單");
    		//saveBtn.setText("總計");    		
    		enableButton(copyBtn);
    		enableButton(saveBtn);
    		disableButton(modifyBtn);
    		disableButton(delBtn);
    		disableButton(itemAddBtn);    		
    	}
    	else if (status.equals("取消")) {
    		disableButton(copyBtn);
    		disableButton(saveBtn);
    		disableButton(modifyBtn);
    		disableButton(delBtn);
    		disableButton(itemAddBtn);    		
    	}
    	else {
    		enableButton(copyBtn);
    		disableButton(saveBtn);
    		disableButton(modifyBtn);
    		disableButton(delBtn);    		
    		enableButton(itemAddBtn);
    	}
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
    		AlertDialog.Builder ab = new Builder(SalesDetailsActivity.this);
    		ab.setTitle(ACT_TITLE);
    		ab.setMessage("確認取消所有未儲存的資料及離開?");
    		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
    				dialog.dismiss();
    				SalesDetailsActivity.this.finish();
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