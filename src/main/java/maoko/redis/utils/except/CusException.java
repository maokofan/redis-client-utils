package maoko.redis.utils.except;


/**
 * 自定义异常
 * 
 * @author fanpei
 *
 */
public class CusException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 错误编号
	 */
	private ErrorCode errorCode;

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public CusException() {
		errorCode = ErrorCode.UnknownError;
	}

	public CusException(String message) {
		super(message);
		errorCode = ErrorCode.UnknownError;

	}

	public CusException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public CusException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public CusException(ErrorCode errorCode, Throwable e) {
		super(e);
		this.errorCode = errorCode;
	}

	public CusException(String message, Throwable e) {
		super(message, e);
		errorCode = ErrorCode.UnknownError;
	}

	public CusException(ErrorCode errorCode, String message, Throwable e) {
		super(message, e);
		this.errorCode = errorCode;
	}
}
