package com.iceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

public class CustEnqDetailsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cust_enq_details);
        this.setTitle("«È¤á¸Ô²Ó¸ê®Æ");
        initViews();
        setListensers();
        getParaAndShowResults();
    }
    
    private TextView custCodeTxt, payTermTxt, chineseNameTxt, englishNameTxt, contactTxt, tel1Txt, tel2Txt;
    private TextView address1Txt, address2Txt, address3Txt, address4Txt, lastOrderDateTxt;
    private String custGroup;
    private Button salesInvBtn, advSalesBtn;
    private IceUtil util;
    
    private void initViews() {
    	custCodeTxt  = (TextView)findViewById(R.id.txtCust_code);
    	payTermTxt = (TextView)findViewById(R.id.txtPay_term);
    	chineseNameTxt = (TextView)findViewById(R.id.txtChinese_name);
    	englishNameTxt = (TextView)findViewById(R.id.txtEnglish_name);
    	address1Txt = (TextView)findViewById(R.id.txtAddress1);
    	address2Txt = (TextView)findViewById(R.id.txtAddress2);
    	address3Txt = (TextView)findViewById(R.id.txtAddress3);
    	address4Txt = (TextView)findViewById(R.id.txtAddress4);
    	contactTxt = (TextView)findViewById(R.id.txtContact);    	
    	tel1Txt = (TextView)findViewById(R.id.txtTel1);
    	tel2Txt = (TextView)findViewById(R.id.txtTel2);
    	lastOrderDateTxt = (TextView)findViewById(R.id.txtLastOrderDate);
    	salesInvBtn = (Button)findViewById(R.id.butSalesInv);
    	advSalesBtn = (Button)findViewById(R.id.butAdvSalesInv);
    	util = new IceUtil();
    }
    
    private void getParaAndShowResults () {
    	Intent intent = getIntent();
    	Bundle bundle = intent.getExtras();
    	custCodeTxt.setText(bundle.getString("cust_code"));
    	payTermTxt.setText(bundle.getString("pay_term"));
    	chineseNameTxt.setText(bundle.getString("chinese_name"));
    	englishNameTxt.setText(bundle.getString("english_name"));
    	address1Txt.setText(bundle.getString("address_1"));
    	address2Txt.setText(bundle.getString("address_2"));
    	address3Txt.setText(bundle.getString("address_3"));
    	address4Txt.setText(bundle.getString("address_4"));
    	contactTxt.setText(bundle.getString("contact"));
    	tel1Txt.setText(bundle.getString("tel_1"));
    	tel2Txt.setText(bundle.getString("tel_2"));
    	lastOrderDateTxt.setText(util.strToDateFmt(bundle.getString("last_order_date"), "yyyy/MM/dd"));
    	custGroup = bundle.getString("cust_group");
    }
    
    private void setListensers() {
    	salesInvBtn.setOnClickListener(salesInvClick);
    	advSalesBtn.setOnClickListener(advSalesClick);
    }
    
    private Button.OnClickListener salesInvClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
        	//Pass data to another Activity 
        	Intent it = new Intent(CustEnqDetailsActivity.this, SalesDetailsActivity.class);
        	Bundle bundle = new Bundle();
        	bundle.putString("numOrcode", "cust_code");
        	bundle.putString("value", custCodeTxt.getText().toString());
        	it.putExtras(bundle);
        	startActivity(it);    		
    	}
    };
    
    private Button.OnClickListener advSalesClick = new Button.OnClickListener() {
    	@Override
    	public void onClick(View v) {
        	//Pass data to another Activity
        	Intent it = new Intent(CustEnqDetailsActivity.this, AdvSalesActivity.class);
        	Bundle bundle = new Bundle();
        	bundle.putString("cust_code", custCodeTxt.getText().toString());
        	bundle.putString("cust_name", chineseNameTxt.getText().toString());
        	bundle.putString("cust_group", custGroup);
        	it.putExtras(bundle);
        	startActivity(it);
    	}
    };    
    
}
