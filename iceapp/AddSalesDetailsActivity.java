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
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class AddSalesDetailsActivity extends Activity {
 
	private static final String ACT_TITLE = "加入銷售貨物";
	
	private Button searchBtn, clearBtn, addBtn, backBtn;
	private EditText itemTxt, qtyTxt;
	private TextView itemDescTxt;
	private Spinner itemCatSpinner, itemOptSpinner, tcSpinner;
    private ListView mListView;
    private ArrayList<HashMap<String, String>> addAList, itemAList, chkAList, delAList;
    private String orderDate, custCode, custGroup, actName;
	private int pos, tc_ns, tc_rp, tc_sa;
	private boolean inputItemCode;
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private Intent intent;
    private int i=0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_sales_details);
        this.setTitle(ACT_TITLE);
        initViews();
        setListensers();
        getParam();
        setting();
    }

    private void initViews() {

        itemCatSpinner = (Spinner)findViewById(R.id.spItemCat);    	    	
        mListView = (ListView)findViewById(R.id.salesDetailsList);
        itemOptSpinner = (Spinner)findViewById(R.id.spSearch);
        itemTxt = (EditText)findViewById(R.id.editItem);
        itemDescTxt = (TextView)findViewById(R.id.txtItemDesc);
        tcSpinner = (Spinner)findViewById(R.id.spTranCode);
        qtyTxt = (EditText)findViewById(R.id.editQty);
        searchBtn = (Button)findViewById(R.id.butSearch);
        clearBtn = (Button)findViewById(R.id.butClear);
        addBtn = (Button)findViewById(R.id.butAdd);
        backBtn = (Button)findViewById(R.id.butBack);        
    	dbh = new DatabaseHelper(this);
    	util = new IceUtil();
    	addAList = new ArrayList<HashMap<String, String>>();
    	chkAList = new ArrayList<HashMap<String, String>>();
    	delAList = new ArrayList<HashMap<String, String>>();    	
    }
    
    private void setListensers() {
    	mListView.setOnItemClickListener(detailsClick);
    	itemTxt.addTextChangedListener(new TextWatcher() {
    		@Override
    	    public void afterTextChanged(Editable s) {

    		}

    	    @Override
    	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    	    }

    	    @Override
    	    public void onTextChanged(final CharSequence s, int start, int before, int count){              
    	    	//disableButton();
    	    	disableButton(addBtn);
    	    }
    	});
    	tcSpinner.setOnItemSelectedListener(tranCodeSelected);
    	itemCatSpinner.setOnItemSelectedListener(itemCatSelected);
    	itemOptSpinner.setOnItemSelectedListener(itemOptSelected);
    	searchBtn.setOnClickListener(searchClick);
    	clearBtn.setOnClickListener(clearClick);
    	backBtn.setOnClickListener(backClick);
    	addBtn.setOnClickListener(addClick);
    }

    private void getParam() {
    	intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	actName = bundle.getString("ActName");
    	orderDate = bundle.getString("order_date");
    	custCode = bundle.getString("cust_code");
    	custGroup = bundle.getString("cust_group");    	
    	chkAList = (ArrayList<HashMap<String, String>>)bundle.getSerializable("order_list");
    	if (actName.equals("SalesDetailsActivity"))
    		delAList = (ArrayList<HashMap<String, String>>)bundle.getSerializable("del_list");
    }
    
    private void setting() {
    	createItemCatList();
    	createItemSearchOpt();        	
    	createTranCodeOpt();
    	tc_ns = 0;
    	tc_sa = 1;        	
    	tc_rp = 2;  

    	//disableButton();
    	disableButton(addBtn);
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
    	
    	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
    		
    		clear();
    		String filter = "";
    		String opt = adapterView.getItemAtPosition(position).toString();    	
    		if (!opt.equals("ALL")) {
    			filter = "and b.item_cat = '" + opt + "' ";
    		}
            createItemList(filter, "");
    	}
		public void onNothingSelected(AdapterView arg0) {
			//
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
    
    private void createTranCodeOpt() {
    	List<String> list = new ArrayList<String>();
    	list.add("NS");
    	list.add("SA");
    	if (!actName.equals("AdvSalesActivity"))
    		list.add("RP");
    	
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
    
    private void createItemList(String ifilter1, String ifilter2) {
    	
    	HashMap<String, String> itemHmap;
    	
        String query = "select "
        		+ "a.item_code, "
        		+ "b.short_key, "
        		+ "b.chinese_desc, "
        		+ "d.order_qty last_order_qty, "
        		+ "a.opening_qty-a.sales_qty os_qty "
        		+ "from truck_open a "
        		+ "INNER JOIN item_file b "
        		+ "ON a.item_code = b.item_code "
        		+ "INNER JOIN pda c "
        		+ "ON a.route = c.route "
        		+ "AND c.default_route = 'Y' "
        		+ "LEFT OUTER JOIN sales_order_dtl d "
        		+ "ON a.item_code = d.item_code "
        		+ "and d.tran_code = 'NS' "
        		+ "and d.order_no in "
        		+ "(select max(order_no) "
        		+ "from sales_order_hdr "
        		+ "where order_date < ? "
        		+ "and cust_code = ?) "
        		+ "where a.item_code not in "
        		+ "(select item_code "
        		+ "from not_sell_for_group "
        		+ "where cust_group = ? "
        		+ "and item_code not in "
        		+ "(select item_code "
        		+ "from not_sell_for_cust "
        		+ "where cust_code = ? "
        		+ "and except_this = 'Y')) "
        		+ "and a.item_code not in "
        		+ "(select item_code "
        		+ "from not_sell_for_cust "
        		+ "where cust_code = ? "
        		+ "and except_this = 'N') "
        		+ ifilter1
        		+ ifilter2
        		+ "order by b.chinese_desc";
    	        
        db = dbh.getWritableDatabase();
        Cursor c = db.rawQuery(query, new String[] {util.dateToStrFmt(orderDate)
        		, custCode, custGroup, custCode, custCode});
        int i=0;        
        if (c.moveToFirst()) {
        	itemAList = new ArrayList<HashMap<String, String>>();
        	do {
        		i++;
        		itemHmap = new HashMap<String, String>();
        		itemHmap.put("itemCode", c.getString(c.getColumnIndex("item_code")));
        		itemHmap.put("itemDesc", c.getString(c.getColumnIndex("chinese_desc")));
        		itemHmap.put("shortKey", c.getString(c.getColumnIndex("short_key")));
        		itemHmap.put("lOrderQty", c.getString(c.getColumnIndex("last_order_qty")));
        		itemHmap.put("onQty", c.getString(c.getColumnIndex("os_qty")));
        		itemAList.add(itemHmap);
        	}while (c.moveToNext());        
        } else {
        	util.showDialog("找不到與 " + itemTxt.getText().toString() + " 近似的貨號", AddSalesDetailsActivity.this);
        }
        try {
        	if (actName.equals("SalesDetailsActivity")) {
                refreshOnHandQty();
            	ListAdapter adapter = new SimpleAdapter(this, itemAList, R.layout.add_sales_details_list_item,
            			new String[] {"itemDesc", "lOrderQty", "onQty"}, 
            			new int[] {R.id.sitemDesc, R.id.slastOrderQty, R.id.sonHandQty});
            	mListView.setAdapter(adapter);
        	} else { // AdvSalesActivity
        		//change the column heading and pos
        		TextView col1Txt = (TextView)findViewById(R.id.txtCol1);
        		TextView col3Txt = (TextView)findViewById(R.id.txtCol3);
        		col1Txt.setEms(13);
        		col3Txt.setText("");
            	ListAdapter adapter = new SimpleAdapter(this, itemAList, R.layout.add_sales_details_list_item1,
            			new String[] {"itemDesc", "lOrderQty"}, 
            			new int[] {R.id.sitemDesc, R.id.slastOrderQty});
            	mListView.setAdapter(adapter);
        	}        	        		
        } catch (Exception e) {}
        c.close();
        db.close();
    }
    
    private ListView.OnItemClickListener detailsClick = new ListView.OnItemClickListener() {
    	@Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	pos = position;
        	itemTxt.setText(itemAList.get(pos).get("itemCode").toString());
        	itemDescTxt.setText(itemAList.get(pos).get("itemDesc").toString());
        	if (!itemOptSpinner.getSelectedItem().toString().equals("貨物編號"))
        		itemOptSpinner.setSelection(0);
        	//enableButton();
        	enableButton(addBtn);
        }    	
    };
    
    private void refreshOnHandQty() {
		//check order list
		if (!chkAList.isEmpty()) {
			Iterator<HashMap<String, String>> ic = chkAList.iterator();
			while (ic.hasNext()) {
				HashMap<String, String> icHm = ic.next();
				//match item
				int line=0;
				Iterator<HashMap<String, String>> it = itemAList.iterator();
				while (it.hasNext()) {
					HashMap<String, String> itHm = it.next();
					if (itHm.get("itemCode").equals(icHm.get("itemCode"))) {
						int qty = Integer.parseInt(itHm.get("onQty")) +
								Integer.parseInt(icHm.get("savedQty")) -
								Integer.parseInt(icHm.get("ordQty"));						
						itemAList.get(line).put("onQty", String.valueOf(qty)); 
						break;
					}
					line++;
				}    									
			}    					
		}    	
    	
		//check del item list
		if (!delAList.isEmpty()) {
			Iterator<HashMap<String, String>> id = delAList.iterator();
			while (id.hasNext()) {
				HashMap<String, String> idHm = id.next();
				//match item
				int line=0;
				Iterator<HashMap<String, String>> it = itemAList.iterator();
				while (it.hasNext()) {
					HashMap<String, String> itHm = it.next();
					if (itHm.get("itemCode").equals(idHm.get("itemCode"))) {
						int qty = Integer.parseInt(itHm.get("onQty")) +
								Integer.parseInt(idHm.get("delQty"));
						itemAList.get(line).put("onQty", String.valueOf(qty)); 
						break;
					}
					line++;
				}    									
			}    					
		}
		
		//check new added item list
		if (!addAList.isEmpty()) {
			Iterator<HashMap<String, String>> ia = addAList.iterator();
			while (ia.hasNext()) {
				HashMap<String, String> iaHm = ia.next();
				//match item
				int line=0;
				Iterator<HashMap<String, String>> it = itemAList.iterator();
				while (it.hasNext()) {
					HashMap<String, String> itHm = it.next();
					if (itHm.get("itemCode").equals(iaHm.get("itemCode"))) {
						int qty = Integer.parseInt(itHm.get("onQty")) -
								Integer.parseInt(iaHm.get("ordQty"));
						itemAList.get(line).put("onQty", String.valueOf(qty)); 
						break;
					}
					line++;
				}    									
			}    					
		}    			    	
		
    }

    private Button.OnClickListener clearClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		clear();
    		//disableButton();
    		disableButton(addBtn);

    	}    	
    };

    private Button.OnClickListener backClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		returnToCallingAct();
    	}    	
    };
    
    private Button.OnClickListener addClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String itemCode = itemTxt.getText().toString();
    		String itemDesc = itemDescTxt.getText().toString();
    		String tc = tcSpinner.getSelectedItem().toString();
    		String qty = qtyTxt.getText().toString();
    		
    		//validate the input
    		if (itemCode.length()==0)
    			util.showDialog("請輸入貨物編號", AddSalesDetailsActivity.this);
    		else if (qty.length()==0)
    			util.showDialog("請輸入數量", AddSalesDetailsActivity.this);
    		else if (Integer.parseInt(qty)<=0)
    			util.showDialog("數量必需大於0", AddSalesDetailsActivity.this);
    		else {
        		//check whether the item inputed or not
        		boolean duplicate=false;
        		if (chkAList != null) {
        			Iterator<HashMap<String, String>> ic = chkAList.iterator();
        			while (ic.hasNext()) {
        				HashMap<String, String> hm = ic.next();
        				if (hm.get("itemCode").equals(itemCode) && hm.get("tranCode").equals(tc)) {
        					util.showDialog("此貨已輸入, 不能重覆", AddSalesDetailsActivity.this);
        					duplicate=true;
        					break;
        				}
        			}
        		}
    			if (!duplicate) {
    				if (!addAList.isEmpty()) {
    	    			Iterator<HashMap<String, String>> io = addAList.iterator();
    	    			while (io.hasNext()) {
    	    				HashMap<String, String> hm = io.next();
    	    				if (hm.get("itemCode").equals(itemCode) && hm.get("tranCode").equals(tc)) {
    	    					util.showDialog("此貨已輸入, 不能重覆", AddSalesDetailsActivity.this);
    	    					duplicate=true;
    	    					break;
    	    				}
    	    			}    					
    				}
    			}
        			
    			//check whether the item existed or not
    			if (!duplicate) {
    				boolean found=false;
    				int pos=0;
    				Iterator<HashMap<String, String>> it = itemAList.iterator();
    				while (it.hasNext()) {
    					HashMap<String, String> hm = it.next();
    					if (hm.get("itemCode").equals(itemCode)) {    						
    						found=true;
    						if (actName.equals("SalesDetailsActivity")) {
        						if (Integer.parseInt(qty) > Integer.parseInt(hm.get("onQty"))) {
        							util.showDialog("數量大於存貸量 "+hm.get("onQty"), AddSalesDetailsActivity.this);
        							break;
        						}
        						else {
        							//deduct onhand qty
        							int onhand  = Integer.parseInt(hm.get("onQty"));
        							onhand = onhand - Integer.parseInt(qty);
        							itemAList.get(pos).put("onQty", String.valueOf(onhand));
        							mListView.invalidateViews();
        						}
    						}
							//add item into the add list
							HashMap<String, String> addHmap = new HashMap<String, String>();
							addHmap.put("tranCode", tc);
							addHmap.put("itemCode", itemCode);
							addHmap.put("itemDesc", itemDesc);
							addHmap.put("ordQty", String.valueOf(qty));
							addAList.add(addHmap);
							disableButton(addBtn);
							Toast toast = Toast.makeText(AddSalesDetailsActivity.this, "已加入", Toast.LENGTH_SHORT);				
							toast.show();
							break;							
						}
						pos++;												
					}
    				if (!found) {
    					util.showDialog("找不到此貨號資料", AddSalesDetailsActivity.this);
    				}
    			}    			
    			//disableButton();
    			
    		}
    	}    	
    };

    private Button.OnClickListener searchClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		String itemCode = itemTxt.getText().toString();    		
    		if (itemCode.length()>0) {
        		String filter1 = "", filter2="";
        		String opt = itemCatSpinner.getSelectedItem().toString(); 
        		if (!opt.equals("ALL")) {
        			filter1 = "and b.item_cat = '" + opt + "' ";
        		}
        		filter2 = "and a.item_code like '" + itemCode + "%' ";
                createItemList(filter1, filter2);
                //
                clear();
                if (itemAList.size() == 1) {
                	if (itemAList.get(0).get("itemCode").toString().equals(itemCode)) {
                		itemTxt.setText(itemCode);
                		itemDescTxt.setText(itemAList.get(0).get("itemDesc").toString());
                    	//enableButton();
                		enableButton(addBtn);
                	}
                }
    		}
    	}    	
    };
           
    private void clear() {
		itemTxt.setText(null);
		itemDescTxt.setText(null);
		qtyTxt.setText(null);
    }
 
    private void returnToCallingAct() {
    	intent.putExtra("order_list", addAList);
    	AddSalesDetailsActivity.this.setResult(RESULT_OK, intent);
    	AddSalesDetailsActivity.this.finish();    	
    }
    
    /**
    private void disableButton(){
    	
    	clearBtn.setEnabled(false);
    	clearBtn.setClickable(false);
    	addBtn.setEnabled(false);
    	addBtn.setClickable(false);
    	backBtn.setEnabled(false);
    	backBtn.setClickable(false);
    }
    
    private void enableButton(){
    	
    	clearBtn.setEnabled(true);
    	clearBtn.setClickable(true);
    	addBtn.setEnabled(true);
    	addBtn.setClickable(true);
    	backBtn.setEnabled(true);
    	backBtn.setClickable(true);
    	
    }
    
    */

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
    	returnToCallingAct();
    }
}