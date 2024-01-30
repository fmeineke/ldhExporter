package imise;

import jakarta.servlet.http.HttpServletResponse;

public class HttpException extends Exception {
	private static final long serialVersionUID = 1L;
	private int code;
	public HttpException(int code,String msg) {
		super(msg);		
		this.code=code;
	}
	public int getCode() {
		return code == 0? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : code;
	}
}