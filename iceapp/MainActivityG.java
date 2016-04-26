package com.iceapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.os.Build;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.GridView;
import android.content.DialogInterface;
import android.content.Intent;


public class MainActivityG extends ActionBarActivity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu2);
        GridView gridview = (GridView) findViewById(R.id.main_page_gridview);
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();  
        
        /*HashMap<String, Object> map = new HashMap<String, Object>();  
        map.put("ItemImage", R.drawable.route_data);//11
        map.put("ItemText", "路線資料");//按序号做ItemText  
        lstImageItem.add(map);  
  
        map.put("ItemImage", R.drawable.cust_data);//12
        map.put("ItemText", "客戶資料");//按序号做ItemText  
        lstImageItem.add(map);  
  
        map.put("ItemImage", R.drawable.item_data);//21
        map.put("ItemText", "貨物資料");//按序号做ItemText  
        lstImageItem.add(map);  
        
        map.put("ItemImage", R.drawable.sales_order);//22
        map.put("ItemText", "銷售資料");//按序号做ItemText  
        lstImageItem.add(map);  
        
        map.put("ItemImage", R.drawable.sales_sum);//31
        map.put("ItemText", "銷售總結");//按序号做ItemText  
        lstImageItem.add(map);  
        
        map.put("ItemImage", R.drawable.replenish);//32
        map.put("ItemText", "貨物補充");//按序号做ItemText  
        lstImageItem.add(map);  
        
        map.put("ItemImage", R.drawable.item_price);//41
        map.put("ItemText", "貨物價表");//按序号做ItemText  
        lstImageItem.add(map);  
        
        map.put("ItemImage", R.drawable.in_out);//42
        map.put("ItemText", "資料設定");//按序号做ItemText  
        lstImageItem.add(map);  
        
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		int gridSize = display.getSize();
        int count = gridSize/ 160; // image has 160x160 px
        int colWidth = (gridSize / count) - PADDING;

        gridview.setColumnWidth(colWidth);
        gridview.setNumColumns(count);*/
        
        initViews();
        setListensers();
    }

    
    
	private Button RouteEnqBtn, CustEnqBtn, ItemEnqBtn, SalesEnqBtn, SalesSumEnqBtn;
	private Button ReplenishBtn, ProductPriceBtn, SystemSetBtn, DataImpBtn, DataExpBtn;
    	
    private void initViews() {
        RouteEnqBtn = (Button)findViewById(R.id.butRouteEnq);
        CustEnqBtn = (Button)findViewById(R.id.butCustEnq);
        ItemEnqBtn = (Button)findViewById(R.id.butItemEnq);
        SalesEnqBtn = (Button)findViewById(R.id.butSalesEnq);
        SalesSumEnqBtn = (Button)findViewById(R.id.butSalesSumEnq);
        ReplenishBtn = (Button)findViewById(R.id.butReplenish);
        ProductPriceBtn = (Button)findViewById(R.id.butItemPrice);
        SystemSetBtn = (Button)findViewById(R.id.butSystemSet);
        DataImpBtn = (Button)findViewById(R.id.butDataImp);
        DataExpBtn = (Button)findViewById(R.id.butDataExp);    	
    }
    
    private void setListensers() {
    	RouteEnqBtn.setOnClickListener(routeEnquiry);
    	CustEnqBtn.setOnClickListener(custEnquiry);
    	ItemEnqBtn.setOnClickListener(itemEnquiry);
    	SalesEnqBtn.setOnClickListener(salesEnquiry);
    	SalesSumEnqBtn.setOnClickListener(salesSumEnquiry);
    	ReplenishBtn.setOnClickListener(itemReplenish);
    	ProductPriceBtn.setOnClickListener(productPriceEnquiry);
    	SystemSetBtn.setOnClickListener(systemSetting);
    	DataImpBtn.setOnClickListener(importCsv);
    	DataExpBtn.setOnClickListener(exportCsv);
    }

    private Button.OnClickListener routeEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, RouteEnqActivity.class);
    		startActivity(intent);
    	}
    };
    
    
    private Button.OnClickListener custEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, CustEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "Main");
    		intent.putExtras(bundle);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener itemEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, ProductEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "Main");
    		intent.putExtras(bundle);
    		startActivity(intent);
    	}
    };

    private Button.OnClickListener salesEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, SalesEnqActivity.class);
    		startActivity(intent);
    	}
    };

    private Button.OnClickListener salesSumEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, SalesSummaryActivity.class);
    		startActivity(intent);
    	}
    };

    private Button.OnClickListener itemReplenish = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, ReplenishActivity.class);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener productPriceEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		    		                                              
    		Intent intent = new Intent(MainActivityG.this, ProductPriceEnqActivity.class);    		
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "Main");
    		intent.putExtras(bundle);    		    		    		
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener systemSetting = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, ToolsActivity.class);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener importCsv = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, ImportCsvActivity.class);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener exportCsv = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivityG.this, ExportCsvActivity.class);
    		startActivity(intent);
    	}
    };

}
