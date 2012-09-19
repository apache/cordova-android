package org.apache.cordova;

/** 
 * @description Exception class representing defined Globalization error codes
 * @Globalization error codes:
 * 		GlobalizationError.UNKNOWN_ERROR = 0;
 * 		GlobalizationError.FORMATTING_ERROR = 1;   
 * 		GlobalizationError.PARSING_ERROR = 2;   
 * 		GlobalizationError.PATTERN_ERROR = 3;
 */
public class GlobalizationError extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
	public static final String FORMATTING_ERROR = "FORMATTING_ERROR";
	public static final String PARSING_ERROR = "PARSING_ERROR";
	public static final String PATTERN_ERROR = "PATTERN_ERROR";
	
	int error = 0;	//default unknown error thrown
	/**
     * Default constructor        
     */
    public GlobalizationError() {}
	/**
     * Create an exception returning an error code 
     *    
     * @param   s           
     */
    public GlobalizationError(String s) {      	
    	if (s.equalsIgnoreCase(FORMATTING_ERROR)){
        	error = 1;
        }else if (s.equalsIgnoreCase(PARSING_ERROR)){
        	error = 2;
        }else if (s.equalsIgnoreCase(PATTERN_ERROR)){
        	error = 3;
        }      	
    }
    /**
     * get error string based on error code 
     *    
     * @param   String msg           
     */
    public String getErrorString(){
    	String msg = "";
    	switch (error){
    	case 0:
    		msg = UNKNOWN_ERROR;
    		break;
    	case 1:
    		msg =  FORMATTING_ERROR;
    		break;
    	case 2:
    		msg =  PARSING_ERROR;
    		break;
    	case 3:
    		msg =  PATTERN_ERROR;
    		break;
    	}
    	return msg;
    }
    /**
     * get error code 
     *    
     * @param   String msg           
     */
    public int getErrorCode(){    	
    	return error;
    }
    
}