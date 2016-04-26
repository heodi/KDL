package com.iceapp;

import android.app.Activity;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class ReplenishActivity extends Activity {
 
	private static final String ACT_TITLE = "貨物補充";
	private static final String ITEM_DESC_TXT = "貨物名稱 : ";
	
	private Button clearBtn, addBtn, cancelBtn, saveBtn, searchBtn;
	private EditText itemTxt, qtyTxt;
	private TextView descTxt;
	private Spinner itemCatSpinner, itemOptSpinner;
    private ListView mListView;
    private ArrayList<HashMap<String, String>> itemAList;
	private boolean inputItemCode, cancelMode, modified;
	private int pos, maxLines;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replenish);        
        initViews();
        setListensers();
              
        this.setTitle(ACT_TITLE);
    }

    private void initViews() {
    	
        itemCatSpinner = (Spinner)findViewById(R.id.spItemCat);    	
        mListView = (ListView)findViewById(R.id.replenishList);
        itemOptSpinner = (Spinner)findViewById(R.id.spSearch);        
        itemTxt = (EditText)findViewById(R.id.editItem);
        descTxt = (TextView)findViewById(R.id.itemDescTxt);        
        qtyTxt = (EditText)findViewById(R.id.editQty);
        clearBtn = (Button)findViewById(R.id.butClear);
        addBtn = (Button)findViewById(R.id.butAdd);
        cancelBtn = (Button)findViewById(R.id.butCancel);
        saveBtn = (Button)findViewById(R.id.butSave);
        searchBtn = (Button)findViewById(R.id.butSearch);
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    	
    	createItemCatList();
    	createItemSearchOpt();    
    	cancelMode = false;
    	modified = false;
    }

    private void setListensers() {
    	itemCatSpinner.setOnItemSelectedListener(itemCatSelected);
    	itemOptSpinner.setOnItemSelectedListener(itemOptSelected);
    	clearBtn.setOnClickListener(clearClick);
    	addBtn.setOnClickListener(addClick);
    	cancelBtn.setOnClickListener(cancelClick);
    	saveBtn.setOnClickListener(saveClick);
    	searchBtn.setOnClickListener(searchClick);
    	itemTxt.addTextChangedListener(new TextWatcher() {
    		@Override
    	    public void afterTextChanged(Editable s) {}

    	    @Override
    	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    	    @Override
    	    public void onTextChanged(final CharSequence s, int start, int before, int count){    	    	
    	    	disableButton(addBtn);
    	    }
    	});
    	
    }
           
    private void createItemCatList() {
    	
    	List<String> itemCatList = new ArrayList<String>();    	
    	
    	String query = "select 'ALL' "
    			+ "union "
    			+ "select distinct item_cat "
    			+ "from item_file "
    			+ "order by 1 ";
    	
    	db = dbh.getWritableDatabase();
    	Cursor c = db.rawQuery(query, null);
    	if (c.moveToFirst()) {
    		do {
    			itemCatList.add(c.getString(0));
    		} while (c.moveToNext());
    	}
    	c.close();
    	db.close();
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String> (
    			this,android.R.layout.simple_spinner_item, itemCatList);    	    
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	itemCatSpinner.setAdapter(dataAdapter);    	
    }

    private Spinner.OnItemSelectedListener itemCatSelected = new Spinner.OnItemSelectedListener() {    	
    	String opt;
    	public void onItemSelected(AdapterView adapterView, View view, int position, long id){

    		opt = adapterView.getItemAtPosition(position).toString();
    		if (modified) {
        		AlertDialog.Builder ab = new Builder(ReplenishActivity.this);
        		ab.setTitle(ACT_TITLE);
        		ab.setMessage("確認取消所有未儲存的資料?");
        		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        				dialog.dismiss();
        				activateChanges();
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
    			activateChanges();
    		}
    	}
		public void onNothingSelected(AdapterView arg0) {
			//
		}		
		private void activateChanges() {
    		String filter = "";
    		if (!opt.equals("ALL")) {
    			filter = "where b.item_cat = '" + opt + "' ";
    		}
            createItemList(filter);
            modified = false;
		}
    };

    private Spinner.OnItemSelectedListener itemOptSelected = new Spinner.OnItemSelectedListener() {
    	
    	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
    		
    		if (adapterView.getItemAtPosition(position).toString().equals("貨物編號")) 
    			inputItemCode = true;
    		else
    			inputItemCode = false;
    	}
		public void onNothingSelected(AdapterView arg0) {
			//
		}
    };
    
    private void createItemSearchOpt() {
    	List<String> list = new ArrayList<String>();
    	list.add("貨物編號");
    	list.add("貨物快速鍵");
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
    			this,android.R.layout.simple_spinner_item, list);
    	
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	itemOptSpinner.setAdapter(dataAdapter);    	
    }
    
    private void createItemList(String ifilter) {
        String query = "select "
        		+ "a.item_code item_code, "
        		+ "b.item_cat item_cat, "
        		+ "b.short_key short_key, "
        		+ "b.unit_code unit_code, "
        		+ "b.whtt_unit_code whtt_unit_code, "
        		+ "b.chinese_desc chinese_desc, "
        		+ "a.opening_qty-a.sales_qty os_qty, "
        		+ "d.qty replenish_qty "
        		+ "from truck_open a "
        		+ "inner join item_file b on a.item_code = b.item_code "
        		+ "inner join pda c on a.route = c.route "
        		+ "and c.default_route = 'Y' "
        		+ "left join replenish d "
        		+ "on a.item_code = d.item_code "
        		+ "and d.replenish_date = ? "
        		+  ifilter
        		+ "order by b.chinese_desc";
        
        db = dbh.getWritableDatabase();
        Cursor c = db.rawQuery(query, new String[] {util.getCurDate()});
        maxLines = 0;
        if (c.moveToFirst()) {
        	itemAList = new ArrayList<HashMap<String, String>>();
        	do {
        		insItemIntoArrayList(c.getString(c.getColumnIndex("item_code"))
        				,c.getString(c.getColumnIndex("chinese_desc"))
        				,c.getString(c.getColumnIndex("short_key"))
        				,c.getString(c.getColumnIndex("unit_code"))
        				,c.getString(c.getColumnIndex("whtt_unit_code"))
        				,c.getString(c.getColumnIndex("os_qty"))
        				,c.getString(c.getColumnIndex("replenish_qty")));
        	}while (c.moveToNext());
        	
            try {
            	ListAdapter adapter = new SimpleAdapter(this, itemAList, R.layout.replenish_list_item,
            			new String[] {"itemDesc", "onQty", "repQty"}, 
            			new int[] {R.id.sitemDesc, R.id.sonHandQty, R.id.sreplenishQty});
            	mListView.setAdapter(adapter);
            	
                mListView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    	pos = position;                    	
                    	itemTxt.setText(itemAList.get(pos).get("itemCode"));
                    	descTxt.setText(ITEM_DESC_TXT + itemAList.get(pos).get("itemDesc"));
                    	if (itemAList.get(pos).get("repQty") != null)
                    		qtyTxt.setText(itemAList.get(pos).get("repQty"));
                    		
                    	if (!itemOptSpinner.getSelectedItem().toString().equals("貨物編號"))
                    		itemOptSpinner.setSelection(0);                        	
                    	                    	
                    	if (cancelMode) {
                    		AlertDialog.Builder ab = new Builder(ReplenishActivity.this);
                    		ab.setTitle(ACT_TITLE);
                    		ab.setMessage("確認取消 " + itemTxt.getText() + " 之補貨數量?");
                    		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    			@Override
                    			public void onClick(DialogInterface dialog, int which) {
                    				// TODO Auto-generated method stub
                    				modified = true;
                    				itemAList.get(pos).put("repQty", null);
                    				mListView.invalidateViews();
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
                    	} else {
                        	enableButton(addBtn);
                    	}
                    }
                });            	            	
            } catch (Exception e) {
            	
            }
        }        
        c.close();
        db.close();
    }

    private void insItemIntoArrayList(String iItem, String iDesc, String iShortKey
    		, String iUnit, String iWUnit,String iOnQty, String iReqQty) {
    	
    	HashMap<String, String> itemHmap = new HashMap<String, String>();
		itemHmap.put("itemCode", iItem);
		itemHmap.put("itemDesc", iDesc);
		itemHmap.put("shortKey", iShortKey);
		itemHmap.put("unitCode", iUnit);
		itemHmap.put("wunitCode",  iWUnit);
		itemHmap.put("onQty", iOnQty);
		itemHmap.put("repQty", iReqQty);
		itemAList.add(itemHmap);
		maxLines++;
    }
    
    private void validateInput() {
		String licode;
		String item = itemTxt.getText().toString();
		String qty = qtyTxt.getText().toString();
		boolean ok = false, insItem = false;
		boolean validQty = true;
		
		if (item.equals("")) {
			if (inputItemCode)
				util.showDialog("請輸入貨物編號", ReplenishActivity.this);
			else
				util.showDialog("請輸入貨物快速鍵", ReplenishActivity.this);
			itemTxt.requestFocus();
		} else if (qty.equals("")) {
			util.showDialog("請輸入數量", ReplenishActivity.this);
			qtyTxt.requestFocus();
		} else if (Integer.parseInt(qty) <= 0) { 
			util.showDialog("數量必須大於 0", ReplenishActivity.this);
			qtyTxt.requestFocus();
		} else {
			int pos=0;
			Iterator<HashMap<String, String>> it = itemAList.iterator();
			while (it.hasNext()) {
				if (inputItemCode) 
					licode = it.next().get("itemCode").toString();
				else
					licode = it.next().get("shortKey").toString();
				
				if (licode.equals(item)) {
					ok = true;
					break;
				}
				pos++;
			}
			
			if (!ok) {
				ok = checkItemMaster(item);
				if (ok)
					insItem = true;
			}
			
			if (ok) {
				descTxt.setText(ITEM_DESC_TXT + itemAList.get(pos).get("itemDesc"));
    			String query = "select conversion_rate "
    					+ "from conversion_file "
    					+ "where item_code = ? "
    					+ "and unit_code = ? ";
    			db = dbh.getWritableDatabase();
    			Cursor c = db.rawQuery(query, 
    					new String[] {itemAList.get(pos).get("itemCode"), itemAList.get(pos).get("wunitCode")});
    			if (c.moveToFirst()) {
    				int cRate = c.getInt(c.getColumnIndex("conversion_rate"));
    				if (cRate != 1) {
    					if (Math.floor(Integer.parseInt(qty)/cRate)*cRate != Integer.parseInt(qty)) {
    						validQty = false;
    						util.showDialog("補充數量必須為貨倉單位數量之倍數", ReplenishActivity.this);
    						qtyTxt.requestFocus();
    					}
    				}
    			}
    			c.close();
				
				if (validQty) {
					modified = true;
					itemAList.get(pos).put("repQty", qty);
					mListView.invalidateViews();					
					if (insItem) {
						String q = "select route "
								+ "from pda "
								+ "where default_route = 'Y'";
						Cursor c1 = db.rawQuery(q, null);
						c1.moveToFirst();
						
				    	ContentValues cv = new ContentValues();
				    	cv.put("route", c1.getString(c1.getColumnIndex("route")));
				    	cv.put("item_code", itemAList.get(pos).get("itemCode"));
				    	cv.put("opening_qty", 0);
				    	cv.put("sales_qty", 0);
				    	cv.put("last_mod_system_date", util.getCurDate());
						db.insert("truck_open", null, cv);
						
					}
					Toast toast = Toast.makeText(ReplenishActivity.this, "已加入", Toast.LENGTH_SHORT);
					toast.show();    					
				}
				db.close();
			} else {
        		if (inputItemCode) 
        			util.showDialog("沒有此貨物編號", ReplenishActivity.this);
        		else
        			util.showDialog("沒有此貨物快速鍵", ReplenishActivity.this);
        		itemTxt.requestFocus();					
			}			
		}    	
    }
    
    private boolean checkItemMaster(String iItem) {
    	boolean validItem = false;
    	
        String query = "select "
        		+ "a.item_code item_code, "
        		+ "a.item_cat item_cat, "
        		+ "a.short_key short_key, "
        		+ "a.unit_code unit_code, "
        		+ "a.whtt_unit_code whtt_unit_code, "
        		+ "a.chinese_desc chinese_desc, "
        		+ "0 os_qty, "
        		+ "0 replenish_qty "
        		+ "from item_file a "
        		+ "where a.item_code = ? "
        		+ "or a.short_key = ?";
        
        db = dbh.getWritableDatabase();
        Cursor c = db.rawQuery(query, new String[] {iItem, iItem});
        if (c.moveToFirst()) {
        	validItem = true;
    		insItemIntoArrayList(c.getString(c.getColumnIndex("item_code"))
    				,c.getString(c.getColumnIndex("chinese_desc"))
    				,c.getString(c.getColumnIndex("short_key"))
    				,c.getString(c.getColumnIndex("unit_code"))
    				,c.getString(c.getColumnIndex("whtt_unit_code"))
    				,c.getString(c.getColumnIndex("os_qty"))
    				,c.getString(c.getColumnIndex("replenish_qty")));
    		pos = maxLines-1;
        }
        db.close();
    	return validItem;
    }
 
    private Button.OnClickListener clearClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		itemTxt.setText(null);
    		descTxt.setText(ITEM_DESC_TXT);
    		qtyTxt.setText(null);
    		itemTxt.requestFocus();
    	}
    };
    	        
    private Button.OnClickListener addClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		validateInput();
    	}
    };

    private Button.OnClickListener cancelClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		itemTxt.setText(null);
    		descTxt.setText(ITEM_DESC_TXT);
    		qtyTxt.setText(null);
    		if (!cancelMode) {
    			ReplenishActivity.this.setTitle(ACT_TITLE + " (取消模式)");
    			cancelBtn.setText("返回");
    			disableButton(clearBtn);
    			disableButton(addBtn);
    			disableButton(saveBtn);
    			cancelMode = true;
    		} else {
    			ReplenishActivity.this.setTitle(ACT_TITLE);
    			cancelBtn.setText("取消");
    			enableButton(clearBtn);
    			enableButton(addBtn);
    			enableButton(saveBtn);
    			cancelMode = false;
    		}
    	}
    };
    
    private Button.OnClickListener saveClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		String query, whereClause, route;
    		
    		query = "select route "
    				+ "from pda "
    				+ "where default_route = 'Y';";
    		db = dbh.getWritableDatabase();
    		Cursor c = db.rawQuery(query, null);
    		c.moveToFirst();
    		route = c.getString(0);
    		db.close();

    		whereClause = "route = '" + route + "' and replenish_date = '" + util.getCurDate() + "'";
    		db = dbh.getWritableDatabase();
    		db.delete("replenish", whereClause, null);
    		db.close();
    		
    		db = dbh.getWritableDatabase();    		
    		ContentValues cv = new ContentValues();
			Iterator<HashMap<String, String>> it = itemAList.iterator();
			while (it.hasNext()) {
				HashMap<String, String> hm = it.next();
				if (hm.get("repQty") != null) {
					cv.clear();
					cv.put("route", route);
					cv.put("replenish_date", util.getCurDate());
					cv.put("item_code", hm.get("itemCode"));
					cv.put("qty", Integer.parseInt(hm.get("repQty")));
					db.insert("replenish", null, cv);
				}
			}
			db.close();
			modified = false;
			Toast toast = Toast.makeText(ReplenishActivity.this, "已儲存", Toast.LENGTH_SHORT);
			toast.show();    								
    	}
    };

    private Button.OnClickListener searchClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String item = itemTxt.getText().toString();
            String query = "select "
            		+ "chinese_desc "
            		+ "from item_file "
            		+ "where item_code = ? "
            		+ "or short_key = ?";
            db = dbh.getWritableDatabase();
            Cursor c = db.rawQuery(query, new String[]{item, item});
            if (c.moveToFirst()) {            	
            	descTxt.setText(ITEM_DESC_TXT + c.getString(c.getColumnIndex("chinese_desc")));
            	enableButton(addBtn);
            }
            else {
            	descTxt.setText(ITEM_DESC_TXT);
        		if (inputItemCode) 
        			util.showDialog("沒有此貨物編號", ReplenishActivity.this);
        		else
        			util.showDialog("沒有此貨物快速鍵", ReplenishActivity.this);
        		itemTxt.requestFocus();					            	
            }
    	}
    };
    
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
    		AlertDialog.Builder ab = new Builder(ReplenishActivity.this);
    		ab.setTitle(ACT_TITLE);
    		ab.setMessage("確認取消所有未儲存的資料及離開?");
    		ab.setPositiveButton("確認", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
    				dialog.dismiss();
    				ReplenishActivity.this.finish();
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