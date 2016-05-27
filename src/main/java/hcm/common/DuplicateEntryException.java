package hcm.common;

/**
 * A customized Exception which is
 * thrown when input related errors
 * occurs.
 * 
 * @author jerrick.m.falogme
 */
public class DuplicateEntryException extends Exception{

	public DuplicateEntryException(){}
	
	public DuplicateEntryException(String msg){
		super(msg);
	}
}