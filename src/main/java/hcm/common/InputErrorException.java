package hcm.common;
/**
 * A customized Exception which is
 * thrown when input related errors
 * occurs.
 * 
 * @author jerrick.m.falogme
 */
public class InputErrorException extends Exception{

	public InputErrorException(){}
	
	public InputErrorException(String msg){
		super(msg);
	}
}
