package com.iceapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "icedb.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String ADV_SALES_ORDER = "adv_sales_order";
	private static final String CONVERSION_FILE = "conversion_file";
	private static final String CUSTOMER_FILE = "customer_file";
	private static final String ITEM_ALIAS_FOR_CUST = "item_alias_for_cust";
	private static final String ITEM_ALIAS_FOR_GROUP = "item_alias_for_group";
	private static final String ITEM_FILE = "item_file";
	private static final String NOT_SELL_FOR_CUST = "not_sell_for_cust";
	private static final String NOT_SELL_FOR_GROUP = "not_sell_for_group";
	private static final String PDA = "pda";
	private static final String PRICE_BOOK = "price_book";
	private static final String PROMO_BUY = "promo_buy";
	private static final String PROMO_EXCEPTION = "promo_exception";
	private static final String PROMO_FOR_CUST = "promo_for_cust";
	private static final String PROMO_FOR_GROUP = "promo_for_group";
	private static final String PROMO_GET = "promo_get";
	private static final String PROMO_HEADER = "promo_header";
	private static final String REPLENISH = "replenish";
	private static final String ROUTE_CUST = "route_cust";
	private static final String SALES_ORDER_DTL = "sales_order_dtl";
	private static final String SALES_ORDER_HDR = "sales_order_hdr";
	private static final String SALES_ORDER_NUM = "sales_order_num";
	private static final String TRUCK_OPEN = "truck_open";
	private static final String VENDOR_ITEM_DESC = "vendor_item_desc";
	private static final String COMPANY = "company";
	
	public DatabaseHelper (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
		
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
		String CREATE_TB;
		
		CREATE_TB = "CREATE TABLE "
				+ ADV_SALES_ORDER + " ("
				+ "order_date text not null, "
				+ "cust_code text not null, "
				+ "customer_ref_no text, "
				+ "line_no integer not null, "
				+ "item_code text not null, "
				+ "tran_code text not null, "
				+ "order_qty real not null, "
				+ "constraint pk_adv_sales_order "
				+ "primary key (order_date, "
				+ "cust_code, "
				+ "item_code, "
				+ "tran_code))";
		db.execSQL(CREATE_TB);
				
		CREATE_TB = "CREATE TABLE "
				+ CONVERSION_FILE + " ("
				+ "item_code text not null, "
				+ "unit_code text not null, "
				+ "conversion_rate real not null, "
				+ "last_mod_system_date text, "
				+ "constraint pk_conversion_file "
				+ "primary key (item_code, "
				+ "unit_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ CUSTOMER_FILE + " ("
				+ "cust_code text not null primary key, "
				+ "english_name text, "
				+ "chinese_name text not null, "
				+ "address_1 text not null, "
				+ "address_2 text, "
				+ "address_3 text, "
				+ "address_4 text, "
				+ "tel_1 text, "
				+ "tel_2 text, "
				+ "fax_no text, "
				+ "contact text, "
				+ "pay_term text not null, "
				+ "route text, "
				+ "delivery_seq text, "
				+ "cust_group text, "
				+ "no_of_copy integer, "
				+ "vendor_code text, "
				+ "price_book_code text, "
				+ "print_name text, "
				+ "cust_status text, "
				+ "last_mod_system_date text)";
		db.execSQL(CREATE_TB);

		CREATE_TB = "CREATE TABLE "
				+ ITEM_ALIAS_FOR_CUST + " ("
				+ "cust_code text not null, "
				+ "item_code text not null, "
				+ "alias text, "
				+ "last_mod_system_date text, "
				+ "constraint pk_item_alias_for_cust "
				+ "primary key (cust_code, "
				+ "item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ ITEM_ALIAS_FOR_GROUP + " ("
				+ "cust_group text not null, "
				+ "item_code text not null, "
				+ "alias text, "
				+ "last_mod_system_date text, "
				+ "constraint pk_item_alias_for_group "
				+ "primary key (cust_group, "
				+ "item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE VIRTUAL TABLE "
				+ ITEM_FILE + " USING fts3 ("
				+ "item_code text not null primary key, "
				+ "english_desc text not null, "
				+ "long_desc text not null, "
				+ "chinese_desc text not null, "
				+ "unit_code text, "
				+ "item_cat text not null, "
				+ "short_key text, "
				+ "whtt_unit_code text, "
				+ "item_status text not null, "
				+ "item_size text, "
				+ "item_group text, "
				+ "print_tel text not null, "
				+ "last_mod_system_date text)";
		db.execSQL(CREATE_TB);
				
		CREATE_TB = "CREATE TABLE "
				+ NOT_SELL_FOR_CUST + " ("
				+ "cust_code text not null, "
				+ "item_code text not null, "
				+ "except_this text, "
				+ "last_mod_system_date text, "
				+ "constraint pk_not_sell_for_cust "
				+ "primary key (cust_code, "
				+ "item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ NOT_SELL_FOR_GROUP + " ("
				+ "cust_group text not null, "
				+ "item_code text not null, "
				+ "last_mod_system_date text, "
				+ "constraint pk_not_sell_for_group "
				+ "primary key (cust_group, "
				+ "item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ PDA + " ("
				+ "route text not null primary key, "
				+ "print_server_ip text, "
				+ "print_queue text, "
				+ "lpr_log_file text, "
				+ "ftp_server_ip text, "
				+ "ftp_folder text, "
				+ "csv_local_folder text, "
				+ "upload_folder text, "
				+ "download_folder text, "
				+ "phone text, "
				+ "default_route text)";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ PRICE_BOOK + " ("
				+ "price_book_code text not null, "
				+ "priority integer not null, "
				+ "item_code text not null, "
				+ "effective_date text not null, "
				+ "qty_level real not null, "
				+ "unit_price real, "
				+ "last_mod_system_date text, "
				+ "constraint pk_price_book "
				+ "primary key (price_book_code, "
				+ "item_code, "
				+ "priority, "
				+ "effective_date, "
				+ "qty_level))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ PROMO_BUY + " ("
				+ "promo_code text not null, "
				+ "buy_item_code text not null, "
				+ "buy_qty real, "
				+ "last_mod_system_date, "
				+ "constraint pk_promo_buy "
				+ "primary key (promo_code, "
				+ "buy_item_code))";
		db.execSQL(CREATE_TB);

		CREATE_TB = "CREATE TABLE "
				+ PROMO_EXCEPTION + " ("
				+ "promo_code text not null primary key, "
				+ "cust_group text, "
				+ "cust_code text)";		
		db.execSQL(CREATE_TB);

		CREATE_TB = "CREATE TABLE "
				+ PROMO_FOR_CUST + " ("
				+ "promo_code text not null, "
				+ "cust_code text not null, "
				+ "last_mod_system_date text, "
				+ "constraint pk_promo_for_cust "
				+ "primary key (promo_code, "
				+ "cust_code))";		
		db.execSQL(CREATE_TB);

		CREATE_TB = "CREATE TABLE "
				+ PROMO_FOR_GROUP + " ("
				+ "promo_code text not null, "
				+ "cust_group text not null, "
				+ "last_mod_system_date text, "
				+ "constraint pk_promo_for_group "
				+ "primary key (promo_code, "
				+ "cust_group))";		
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ PROMO_GET + " ("
				+ "promo_code text not null, "
				+ "get_item_code text not null, "
				+ "get_qty real, "
				+ "last_mod_system_date text, "
				+ "constraint pk_promo_get "
				+ "primary key (promo_code, "
				+ "get_item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ PROMO_HEADER + " ("
				+ "promo_code text not null, "
				+ "promo_date_from text not null, "
				+ "promo_date_to text, "
				+ "last_mod_system_date text, "
				+ "constraint pk_promo_header "
				+ "primary key (promo_code, "
				+ "promo_date_from))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ REPLENISH + " ("
				+ "route text not null, "
				+ "replenish_date text not null, "
				+ "item_code text not null, "
				+ "qty real not null, "
				+ "constraint pk_replenish "
				+ "primary key (route, "
				+ "replenish_date, "
				+ "item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ ROUTE_CUST + " ("
				+ "route text not null, "
				+ "cust_code text not null, "
				+ "chinese_name text, "
				+ "delivery_seq integer, "
				+ "last_order_date text, "
				+ "status text, "
				+ "visit_date text, "
				+ "constraint pk_route_cust "
				+ "primary key (route, "
				+ "cust_code))";
		db.execSQL(CREATE_TB);

		CREATE_TB = "CREATE TABLE "
				+ SALES_ORDER_DTL + " ("
				+ "order_no text not null, "
				+ "line_no integer not null, "
				+ "item_code text not null, "
				+ "tran_code text not null, "
				+ "order_qty real not null, "
				+ "unit_price real, "
				+ "constraint pk_sales_order_dtl "
				+ "primary key (order_no, "
				+ "item_code, "
				+ "tran_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ SALES_ORDER_HDR + " ("
				+ "order_date text not null, "
				+ "cust_code text not null, "
				+ "order_no text not null primary key, "
				+ "customer_ref_no text, "
				+ "order_status text, "
				+ "total_amt real, "
				+ "create_system_date text, "
				+ "last_mod_system_date text)";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ SALES_ORDER_NUM + " ("
				+ "prefix text not null, "
				+ "last_seq_no integer not null)";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ TRUCK_OPEN + " ("
				+ "route text not null, "
				+ "item_code text not null, "
				+ "opening_qty real not null, "
				+ "sales_qty real, "
				+ "last_mod_system_date text, "
				+ "constraint pk_truck_open "
				+ "primary key (route, "
				+ "item_code))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ VENDOR_ITEM_DESC + " ("
				+ "cust_group text not null, "
				+ "alias text not null, "
				+ "label_desc text, "
				+ "constraint pk_vendor_item_desc "
				+ "primary key (cust_group, "
				+ "alias))";
		db.execSQL(CREATE_TB);
		
		CREATE_TB = "CREATE TABLE "
				+ COMPANY + " ("
				+ "office_date text not null)";
		db.execSQL(CREATE_TB);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		db.execSQL("DROP TABLE IF EXISTS " + ADV_SALES_ORDER); 
		db.execSQL("DROP TABLE IF EXISTS " + CONVERSION_FILE);
		db.execSQL("DROP TABLE IF EXISTS " + CUSTOMER_FILE);
		db.execSQL("DROP TABLE IF EXISTS " + ITEM_ALIAS_FOR_CUST);
		db.execSQL("DROP TABLE IF EXISTS " + ITEM_ALIAS_FOR_GROUP);
		db.execSQL("DROP TABLE IF EXISTS " + ITEM_FILE);
		db.execSQL("DROP TABLE IF EXISTS " + NOT_SELL_FOR_CUST);
		db.execSQL("DROP TABLE IF EXISTS " + NOT_SELL_FOR_GROUP);
		db.execSQL("DROP TABLE IF EXISTS " + PDA);
		db.execSQL("DROP TABLE IF EXISTS " + PRICE_BOOK);
		db.execSQL("DROP TABLE IF EXISTS " + PROMO_BUY);
		db.execSQL("DROP TABLE IF EXISTS " + PROMO_EXCEPTION);
		db.execSQL("DROP TABLE IF EXISTS " + PROMO_FOR_CUST);
		db.execSQL("DROP TABLE IF EXISTS " + PROMO_FOR_GROUP);
		db.execSQL("DROP TABLE IF EXISTS " + PROMO_GET);
		db.execSQL("DROP TABLE IF EXISTS " + PROMO_HEADER);
		db.execSQL("DROP TABLE IF EXISTS " + REPLENISH);
		db.execSQL("DROP TABLE IF EXISTS " + ROUTE_CUST);
		db.execSQL("DROP TABLE IF EXISTS " + SALES_ORDER_DTL);
		db.execSQL("DROP TABLE IF EXISTS " + SALES_ORDER_HDR);
		db.execSQL("DROP TABLE IF EXISTS " + SALES_ORDER_NUM);
		db.execSQL("DROP TABLE IF EXISTS " + TRUCK_OPEN);
		db.execSQL("DROP TABLE IF EXISTS " + VENDOR_ITEM_DESC);
		db.execSQL("DROP TABLE IF EXISTS " + COMPANY);
		onCreate(db);		
	}

}
