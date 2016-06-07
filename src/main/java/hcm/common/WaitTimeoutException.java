package hcm.common;

public class WaitTimeoutException extends Exception {

	public WaitTimeoutException(){}
	
	public WaitTimeoutException(String msg){
		super(msg);
	}
}
