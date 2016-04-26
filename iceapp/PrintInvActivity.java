package com.iceapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.*;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;



import java.util.List;

public abstract class PrintInvActivity extends Activity {
 
	private static final int MAXROWS = 13; //15
	private static final int LINESTART = 399;
	
    private DatabaseHelper dbh;
    private SQLiteDatabase db;
    private IceUtil util;
    private boolean fourDecPrint;
    private File myFile;
    private String orderNo;
    
    protected void printInvoice(String iOrderNo) {
    	Cursor c;
    	String query;
    	String sPhoneNo = "";
    	String sCustCode = "";
    	String sOrderDate = "";
    	String sTotalAmt = "";
    	String sCustRefNo = "";
    	String sCustGroup = "";
    	String sName = "";
    	String sPnRef = "";
    	String sVendorCode = "";
    	String sRoute = "";
    	String sPayTerm = "";
    	String sAcc = "";
    	String sItemAlias, sEnglishDesc, sChineseDesc, sPrintTel, sItemDesc, sUPrice, sLineAmt;
    	int nNsQty, nPgQty, nRpQty, nRecCnt, cols;
    	//double nUPrice, nLineAmt;
    	float nUPrice, nLineAmt;
    	double nTotalRP = 0;
    	double nTotalPG = 0;    	
    	boolean newPage = true;
    	boolean firstPage = true;
    	int rows = 0; 
    	int line = LINESTART;

    	try {    		
    		orderNo = iOrderNo;
    		File externalStorageDir = Environment.getExternalStorageDirectory();
    		File invDir = new File(externalStorageDir, "ICEAPP/INVOICES");
    		myFile = new File(invDir, orderNo + ".pdf");
    		if (!invDir.exists()) {
    		    invDir.mkdirs();
    		}

    		FileOutputStream fileout=new FileOutputStream(myFile);
    		OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
    		
          	Document objDocument = new Document();
          	PdfWriter pdfWriter = PdfWriter.getInstance(objDocument, new FileOutputStream(myFile));

            //pagesize 8.25" X 8"
    		//1 inch = 72pt :  Rectangle(pt, pt)
    		Rectangle ps = new Rectangle(594f, 576f);    		          	
          	objDocument.setPageSize(ps);        	
        	objDocument.open();
        	PdfContentByte cb = pdfWriter.getDirectContent();

        	BaseFont bfont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        	BaseFont nfont = BaseFont.createFont(BaseFont.COURIER , BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        	//BaseFont cfont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        	BaseFont cfont = BaseFont.createFont("MSungStd-Light", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);

        	SQLiteDatabase db; 
            query = "select "
            		+ "a.cust_code "
            		+ ",a.order_date "
            		+ ",a.total_amt "
            		+ ",a.customer_ref_no "
            		+ ",b.vendor_code "
            		+ ",b.cust_group "
            		+ ",b.route "
            		+ ",b.pay_term "
            		+ ",(case when b.print_name = 'E' then english_name "
            		+ " else chinese_name end) name "
            		+ "from sales_order_hdr a, customer_file b "
            		+ "where a.cust_code = b.cust_code "
            		+ "and order_no = ?";
            
    		dbh = new DatabaseHelper(this);
            db = dbh.getReadableDatabase();
            c = db.rawQuery(query, new String[] {orderNo});
            if (c.moveToFirst()) {
            	sCustCode = c.getString(c.getColumnIndex("cust_code"));
            	sOrderDate = c.getString(c.getColumnIndex("order_date"));
            	sTotalAmt = c.getString(c.getColumnIndex("total_amt"));
            	sCustRefNo = c.getString(c.getColumnIndex("customer_ref_no"));
            	sVendorCode = c.getString(c.getColumnIndex("vendor_code"));
            	sCustGroup = c.getString(c.getColumnIndex("cust_group"));
            	sRoute = c.getString(c.getColumnIndex("route"));
            	sPayTerm = c.getString(c.getColumnIndex("pay_term"));
            	sName = c.getString(c.getColumnIndex("name"));
        		if (sVendorCode.length() <= 0)
        			sAcc = sCustCode;
        		else
            		sAcc = sCustCode+"("+sVendorCode+")";
        		//20-Apr-2015
        		sPnRef = "";
        		if (sCustGroup.equals("70")||sCustGroup.equals("72")) {
        			if (sName.matches(".*K4131.*")) {
        				sPnRef = sName.substring(sName.indexOf("K4131"));
        				sName = sName.substring(0, sName.indexOf("K4131"));
        			} else if (sName.matches(".*PO:.*")) {
        				sPnRef = sName.substring(sName.indexOf("PO:"));       				
        				sName = sName.substring(0, sName.indexOf("PO:"));
        			}        		
        		}
            }
            c.close();
            
            query = "select "
            		+ "phone "
            		+ "from pda "
            		+ "where default_route = ?";
            c = db.rawQuery(query, new String[] {"Y"});
            if (c.moveToFirst()) {
            	sPhoneNo = c.getString(c.getColumnIndex("phone"));
            }
            c.close();

            query = "select"
            		+ " sum((case when a.tran_code = 'SA' or a.tran_code = 'PG' then a.order_qty else 0 end)) pg_qty "
            		+ ",sum((case when a.tran_code = 'RP' then a.order_qty else 0 end)) rp_qty "
            		+ ",count(distinct a.item_code) record_cnt "
            		+ ",max(case when round(a.unit_price,2) <> round(a.unit_price,4) then 1 else 0 end) four_decimal "
            		+ "from sales_order_dtl a "
            		+ "where a.order_no = ? ";
            c = db.rawQuery(query, new String[] {orderNo});
            if (c.moveToFirst()) {
            	nTotalPG = c.getDouble(c.getColumnIndex("pg_qty"));
            	nTotalRP = c.getDouble(c.getColumnIndex("rp_qty"));
            	nRecCnt = c.getInt(c.getColumnIndex("record_cnt"));
            	fourDecPrint = (c.getInt(c.getColumnIndex("four_decimal"))==1) ? true : false; 
            }
            c.close();

            query = "select (case when c.alias is NULL then "
            		+ " (case when d.alias is NULL then b.item_code else d.alias end) else c.alias end) itemalias "
            		+ ",max(b.english_desc) english_desc "
            		+ ",max(b.long_desc) chinese_desc "
            		+ ",sum((case when a.tran_code = 'NS' then a.order_qty else 0 end)) ns_qty "
            		+ ",sum((case when a.tran_code = 'SA' or a.tran_code = 'PG' then a.order_qty else 0 end)) pg_qty "
            		+ ",sum((case when a.tran_code = 'RP' then a.order_qty else 0 end)) rp_qty "
            		+ ",max(a.unit_price) unit_price"
            		+ ",sum(a.unit_price * a.order_qty) line_amt"
            		+ ",max(b.print_tel) print_tel "
            		+ "from sales_order_dtl a "
            		+ "inner join item_file b "
            		+ "on a.item_code = b.item_code "
            		+ "left outer join item_alias_for_cust c "
            		+ "on a.item_code = c.item_code "
            		+ "and c.cust_code = ? "
            		+ "left outer join item_alias_for_group d "
            		+ "on a.item_code = d.item_code "
            		+ "and d.cust_group = ? "
            		+ "where a.order_no = ? "
            		+ "group by (case when c.alias is NULL then "
            		+ "(case when d.alias is NULL then b.item_code else d.alias end) else c.alias end)";
            
            c = db.rawQuery(query, new String[] {sCustCode, sCustGroup, orderNo});
            if (c.moveToFirst()) {
            	util = new IceUtil();    	

                //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.kdlletterhead);
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);                
                //Image image = Image.getInstance(stream.toByteArray());
                //image.scaleAbsolute(520, 60);
                                
                String qrCodeData = "#" + util.strToDateFmt(sOrderDate, "dd/MM/yyyy") +
                		"#" + sRoute +
                		"#" + sCustCode +
                		"#" + orderNo +
                		"#$" + sTotalAmt + "#";
                
                BarcodeQRCode qrcode = new BarcodeQRCode(qrCodeData, 1, 1, null);
                Image qrcodeImage = qrcode.getImage();
                Image qrcodeImage2 = qrcode.getImage();
                qrcodeImage.setAbsolutePosition(25,430);    //20, 430            
                qrcodeImage.scalePercent(110);
                qrcodeImage2.setAbsolutePosition(520,425);                
                qrcodeImage2.scalePercent(110);
                
                if ((nTotalRP == 0) && (nTotalPG == 0))
                	cols = 4;
                else if ((nTotalRP != 0) && (nTotalPG != 0))
                	cols = 6;
                else
                	cols = 5;

            	do {            		
            		sPrintTel = c.getString(c.getColumnIndex("print_tel"));
            		sItemAlias = c.getString(c.getColumnIndex("itemalias"));
            		sEnglishDesc = c.getString(c.getColumnIndex("english_desc"));
            		sChineseDesc = c.getString(c.getColumnIndex("chinese_desc"));
            		nNsQty = c.getInt(c.getColumnIndex("ns_qty"));
            		nPgQty = c.getInt(c.getColumnIndex("pg_qty"));
            		nRpQty = c.getInt(c.getColumnIndex("rp_qty"));
            		//nUPrice = c.getDouble(c.getColumnIndex("unit_price"));
            		//nLineAmt = c.getDouble(c.getColumnIndex("line_amt"));
            		nUPrice = c.getFloat(c.getColumnIndex("unit_price"));
            		nLineAmt = c.getFloat(c.getColumnIndex("line_amt"));
            		            		           	
            		if (rows >= MAXROWS)
            			newPage = true;
            		if (newPage) {
            			if (!firstPage) {
                			printData(cb, 25, 140, bfont, 12, "To be continued ......");  //20, 140
                			printFooter(cb, bfont, cfont, sRoute, sTotalAmt, sPrintTel, sPhoneNo, sPayTerm, "N");
            			}            			
            			objDocument.newPage();
            			objDocument.add(qrcodeImage);
            			objDocument.add(qrcodeImage2);
            			printData(cb, 80, 468, cfont, 12, sName);
            			printData(cb, 342, 468, bfont, 12, sAcc); //338, 468
            			printData(cb, 505, 468, bfont, 12, util.strToDateFmt(sOrderDate, "yyyy/MM/dd")); //500, 468
            			if (sPnRef.length() > 0)
            				printData(cb, 80, 453, bfont, 12, sPnRef);
            			if (sCustRefNo.length() > 0)
            				printData(cb, 342, 453, bfont, 12, sCustRefNo); //338, 453
            			
            			if (firstPage) {
            				if (cols == 6) {
            					printData(cb, 346, 402, cfont, 10, "免 費");
            					printData(cb, 384, 402, cfont, 10, "換 貨");
            				} else if (cols == 5) {
            					if (nTotalPG > 0)
            						printData(cb, 384, 402, cfont, 10, "免 費");
            					else
            						printData(cb, 384, 402, cfont, 10, "換 貨");
            				}
                            firstPage = false;
            			}
            			newPage = false;
            			line = LINESTART;
            			rows = 0;
            		}
            		line = line - 18;  //16
            		if (sItemAlias.length()==0)
            			sItemDesc = sChineseDesc;
            		else {
                        String q1 = "select a.label_desc "
                        		+ "from vendor_item_desc a "
                        		+ "where a.cust_group = ? "
                        		+ "and a.alias = ?";
                        Cursor c1 = db.rawQuery(q1, new String[]{sCustGroup, sItemAlias});
                        if (c1.moveToFirst())
                        	sItemDesc = sItemAlias + "  " + c1.getString(c.getColumnIndex("label_desc"));
                        else
                        	sItemDesc = sItemAlias + "  " + sChineseDesc;
                        c1.close();            			
            		}
            		printData(cb, 25, line, cfont, 12, sItemDesc);  // 20
            		if (cols == 5) {
            			if (nRpQty > 0)
            				printData(cb, 373, line, bfont, 12, String.format("%" + getPrintLen("Qty", nRpQty) + "s", nRpQty));
            			else if (nPgQty > 0) 
            				printData(cb, 373, line, bfont, 12, String.format("%" + getPrintLen("Qty", nPgQty) + "s", nPgQty));
            		} else if (cols == 6) {
            			if (nPgQty > 0)
            				printData(cb, 336, line, bfont, 12, String.format("%" + getPrintLen("Qty", nPgQty) + "s", nPgQty));            				
            			if (nRpQty > 0)
            				printData(cb, 373, line, bfont, 12, String.format("%" + getPrintLen("Qty", nRpQty) + "s", nRpQty));
            		}
            		printData(cb, 410, line, bfont, 12, String.format("%" + getPrintLen("Qty", nNsQty) + "s", nNsQty));
            		if (fourDecPrint)
            			printData(cb, 454, line, bfont, 12, String.format("%" + getPrintLen("UPrice", nUPrice) + ".4f", nUPrice));
            		else
            			printData(cb, 454, line, bfont, 12, String.format("%" + getPrintLen("UPrice", nUPrice) + ".2f", nUPrice));
            		printData(cb, 527, line, bfont, 12, String.format("%" + getPrintLen("Amt", nLineAmt) + ".2f", nLineAmt));
            		cb.moveTo(25, line-4);  //cb.moveTo(20, line-2);
            		cb.lineTo(578, line-4); //cb.lineTo(578, line-2);            	
            		cb.stroke();
            		rows++;            		
            	}while (c.moveToNext());
            	printFooter(cb, bfont, cfont, sRoute, sTotalAmt, sPrintTel, sPhoneNo, sPayTerm, "Y");
            }            
        	db.close();
        	objDocument.close();
        	outputWriter.close();        	
        	Toast.makeText(getBaseContext(), "Sales Invoice created successfully!",Toast.LENGTH_SHORT).show();
        	
        	Uri filepath =  Uri.fromFile(myFile);
        	Intent it = new Intent(Intent.ACTION_VIEW);
        	it.setDataAndType(filepath, "application/pdf");
        	//it.setFlags(it.FLAG_ACTIVITY_CLEAR_TOP);
        	it.setFlags(it.FLAG_ACTIVITY_CLEAR_TASK|it.FLAG_ACTIVITY_NEW_TASK);

        	boolean found = false;
        	PackageManager packageManager = getPackageManager();
        	List<ResolveInfo> activities = packageManager.queryIntentActivities(it, 0);
        	if (!activities.isEmpty()) {
        		for (ResolveInfo act : activities) {
        			if (act.activityInfo.packageName.toLowerCase().toString().equals("com.brother.ptouch.sdk.printdemo")) {
        				it.setPackage(act.activityInfo.packageName);
        				found = true;
        				break;
        			}
        		}
        	}
        	if (found) {
        		//startActivity(it.createChooser(it, "Select"));
        		startActivityForResult(it.createChooser(it, "Select"),0);
        	} else {
        		util.showDialog("找不到 BROTHER 印表機的列印程式", PrintInvActivity.this);
        	}
        	//startActivity(it);
        	
        } catch (Exception e) {
        	//Toast.makeText(getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();
        	Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        	e.printStackTrace();
        }
    };
    
    private void printData(PdfContentByte cb, float x, float y, BaseFont f, int fsize, String text){
    	 
    	  cb.beginText();
    	  cb.setFontAndSize(f, fsize);
    	  cb.setTextMatrix(x,y);
    	  cb.showText(text);
    	  //cb.showText(text.trim());
    	  cb.endText();
    	 
    }    
    
    private int getPrintLen(String iField, double iValue) {
    	//For the font HELVETICA, the length padded may be variance for different digits
    	//so, we need to calculate the padded length
    	int len=0;
    	if (iField == "Qty")
    		len = 5 + (5 - String.valueOf(Math.round(iValue)).length());
    	else if (iField == "UPrice")
    		len = 9 + ((fourDecPrint ? 4 : 6) - String.valueOf(Math.round(iValue)).length());
    	else  // iField == "Amt" 
    		len = 8 + (5 - String.valueOf(Math.round(iValue)).length());   
    	return (len);
    }
    
    private void printFooter(PdfContentByte cb, BaseFont bf, BaseFont cf, 
    		String iRoute, String iTotalAmt, String iPrintTel, String iPhoneNo, String iPayTerm, String iPrintTot) {
    	Double ttlamt;
    	
		printData(cb, 247, 110, cf, 12, "路線：" + iRoute);		
		if (iPrintTot.equals("Y")) {
			if (iPayTerm.equals("00")) {
				ttlamt = Math.floor(Double.valueOf(iTotalAmt)*10)/10;
				printData(cb, 527, 110, bf,  12, 
						String.format("%" + getPrintLen("Amt", ttlamt) + ".2f", Double.valueOf(ttlamt)));											
			} else {
				printData(cb, 527, 110, bf,  12, 
						String.format("%" + getPrintLen("Amt", Double.valueOf(iTotalAmt)) + ".2f", Double.valueOf(iTotalAmt)));							
			}
		}
    	if (iPrintTel.equals("Y"))
    		printData(cb, 247, 75, cf, 12, "送貨銷售熱線：" + iPhoneNo);
		if (iPayTerm.equals("00")) {
			printData(cb, 440, 64, bf, 12, orderNo);
			printData(cb, 505, 51, bf, 8, "XXXXXXXXXXXXXX");
			//changed backed on 04-feb-2016
			//printData(cb, 514, 64, bf, 12, orderNo);
			//printData(cb, 473, 51, bf, 10, "XXXXXXX            XXXXX");
			////printData(cb, 473, 51, bf, 8, "XXXXXXXX                 XXXXXX");			
		}
		else {
        	printData(cb, 514, 64, bf, 12, orderNo);
			printData(cb, 432, 51, bf, 8, "XXXXXXXXXXXXXX");
			//changed backed on 04-feb-2016
			//printData(cb, 474, 64, bf, 12, orderNo);
			//printData(cb, 426, 51, bf, 10, "XXXXXXX                 XXXXX");
			////printData(cb, 423, 51, bf, 8, "XXXXXXXX                      XXXXXX");			
		}    	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {    	
    	myFile.delete();
    }
    
}