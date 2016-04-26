package com.iceapp;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.GridView;
import android.content.DialogInterface;
import android.content.Intent;

public class MainActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        initViews();
        setListensers();
    }

    
    
	private Button RouteEnqBtn, CustEnqBtn, ItemEnqBtn, SalesEnqBtn, SalesSumEnqBtn, ReprintBtn;
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
        ReprintBtn = (Button)findViewById(R.id.butReprint);
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
    	ReprintBtn.setOnClickListener(reprint);
    	DataImpBtn.setOnClickListener(importCsv);
    	DataExpBtn.setOnClickListener(exportCsv);
    }

    private Button.OnClickListener routeEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, RouteEnqActivity.class);
    		startActivity(intent);
    	}
    };
    
    
    private Button.OnClickListener custEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, CustEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "Main");
    		intent.putExtras(bundle);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener itemEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, ProductEnqActivity.class);
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "Main");
    		intent.putExtras(bundle);
    		startActivity(intent);
    	}
    };

    private Button.OnClickListener salesEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, SalesEnqActivity.class);
    		startActivity(intent);
    	}
    };

    private Button.OnClickListener salesSumEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, SalesSummaryActivity.class);
    		startActivity(intent);
    	}
    };

    private Button.OnClickListener itemReplenish = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, ReplenishActivity.class);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener productPriceEnquiry = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		    		                                              
    		Intent intent = new Intent(MainActivity.this, ProductPriceEnqActivity.class);    		
    		Bundle bundle = new Bundle();
    		bundle.putString("ActName", "Main");
    		intent.putExtras(bundle);    		    		    		
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener systemSetting = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, ToolsActivity.class);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener reprint = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, ReprintActivity.class);
    		startActivity(intent);
    	}
    };
        
    private Button.OnClickListener importCsv = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, ImportCsvActivity.class);
    		startActivity(intent);
    	}
    };
    
    private Button.OnClickListener exportCsv = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		
    		Intent intent = new Intent(MainActivity.this, ExportCsvActivity.class);
    		startActivity(intent);
    	}
    };
    
    @Override
    public void onBackPressed() {
    	setResult(2);
    	finish();
    }
    
    
    
}
