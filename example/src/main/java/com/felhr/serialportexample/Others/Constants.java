package com.felhr.serialportexample.Others;

/**
 * Created by Ho Dong Trieu on 08/30/2018
 */
public interface Constants {

    public String TAG = "HDT";

    public String START = "mdb-state\r";
    public String ENABLE = "ENABLED";

    public String START_SESSION = "start-session\r";

    public String VEND_REQUEST = "VREQ";

    public String APPROVE = "approve-vend ";

    public String DIVINP = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

    public static final String ALREADY_START = "SESSIONIDLE";

    public static final String SUCCESS = "VSUCC";

    public static final String FAIL = "VFAIL";

    public static final String CANCEL = "VCAN"; //cancel input..

    //BE
    public static final String MTI_BALANCE = "0100";
    public static final String MTI_SETTLEMENT = "0500";
    public static final String MTI_TOPUP_SALE = "0200";
    public static final String ProCode_BALANCE_PAYACC = "310000";
    public static final String ProCode_BALANCE_POCACC = "315100";
    public static final String ProCode_TOPUP_POCACC = "575100";
    public static final String ProCode_SALTE = "005100";

    //    public static final String URL_MCP = "http://18.206.248.77:8080/api/authorize";
    public static final String URL_MCP = "http://54.87.241.159:8080/api/authorize";

    //    public static final String URL_MCP_ACCESS_TOKEN = "http://18.206.248.77:8080/login";
    public static final String URL_MCP_ACCESS_TOKEN = "http://54.87.241.159:8080/login";


    //Card APP
    public static final String APP_CREDIT = "0200DF";

    //TYPE TRANSACTION
    public static final int SALE_TYPE = 1;
    public static final int SALE_TYPE_KIOT = 2;

    //status transaction
    public static final int IS_SYNC = 1;
    public static final int IS_NOT_SYNC = 0;

    //state tap card:
    public static final int TAP_FOR_START_VEND = 0;
    public static final int TAP_FOR_PAY_VEND = 1;
    public static final int TAP_FOR_CONFIG_VEND = 2;
    public static final int TAP_FOR_SETTLEMENT = 3;

    public static final String CARD_CONFIG = "9704990000005027";

    //load sqlite
    public String[] productCode = {"VEND01","VEND02","VEND03","VEND04","VEND05","VEND06","VEND07","VEND08","VEND09","VEND10"};
}
