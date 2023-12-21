package imise;

public class HttpException extends Exception {
	private static final long serialVersionUID = 1L;
	int code;
	public HttpException(int code,String msg) {
		super(msg);
		this.code=code;
	}
}